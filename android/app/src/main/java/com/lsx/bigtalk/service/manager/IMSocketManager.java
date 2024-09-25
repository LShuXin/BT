package com.lsx.bigtalk.service.manager;

import android.annotation.SuppressLint;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.GeneratedMessageLite;

import com.lsx.bigtalk.AppConstant;
import com.lsx.bigtalk.service.callback.ListenerQueue;
import com.lsx.bigtalk.service.callback.PacketListener;
import com.lsx.bigtalk.service.event.SocketEvent;
import com.lsx.bigtalk.service.network.MsgServerHandler;
import com.lsx.bigtalk.service.network.SocketThread;
import com.lsx.bigtalk.pb.IMBaseDefine;
import com.lsx.bigtalk.pb.base.DataBuffer;
import com.lsx.bigtalk.pb.base.DefaultHeader;
import com.lsx.bigtalk.pb.base.Header;
import com.lsx.bigtalk.logs.Logger;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferInputStream;

import de.greenrobot.event.EventBus;


public class IMSocketManager extends IMManager {
    private final Logger logger = Logger.getLogger(IMSocketManager.class);
    private final ListenerQueue listenerQueue = ListenerQueue.getInstance();
    private SocketThread msgServerThread;
    private SocketEvent socketEvent = SocketEvent.NONE;

    @SuppressLint("StaticFieldLeak")
    private static IMSocketManager instance = null;

    public static synchronized IMSocketManager getInstance() {
        if (null == instance) {
            instance = new IMSocketManager();
        }
        return instance;
    }

    private IMSocketManager() {

    }

    @Override
    public void doOnStart() {
        socketEvent = SocketEvent.NONE;
    }

    @Override
    public void reset() {
        disconnectFromMsgServer();
        socketEvent = SocketEvent.NONE;
    }

    public void triggerEvent(SocketEvent event) {
        setLatestSocketEvent(event);
        EventBus.getDefault().postSticky(event);
    }

    // no response needed
    public void sendRequest(GeneratedMessageLite request, int sid, int cid) {
        sendRequest(request, sid, cid, null);
    }

    // wait for packet response
    public void sendRequest(GeneratedMessageLite request, int sid, int cid, PacketListener packetlistener) {
        int seqNo = 0;
        try {
            Header header = new DefaultHeader(sid, cid);
            int bodySize = request.getSerializedSize();
            header.setLength(AppConstant.SysConstant.PROTOCOL_HEADER_LENGTH + bodySize);
            seqNo = header.getSeqnum();
            listenerQueue.push(seqNo, packetlistener);
            boolean success = msgServerThread.sendRequest(request, header);
            logger.d("IMSocketManager#sendRequest: %d", success);
        } catch (Exception e) {
            if (null != packetlistener) {
                packetlistener.onFailed();
            }
            listenerQueue.pop(seqNo);
            logger.d("IMSocketManager#sendRequest failed: %s", e.getMessage());
        }
    }

    public void dispatchPacket(ChannelBuffer channelBuffer) {
        DataBuffer buffer = new DataBuffer(channelBuffer);
        Header header = new Header();
        header.decode(buffer);
        int cid = header.getCommandId();
        int sid = header.getServiceId();
        int seqNo = header.getSeqnum();
        logger.d("IMSocketManager#dispatchPacket, serviceId:%d, commandId:%d", sid, cid);
        CodedInputStream codedInputStream = CodedInputStream.newInstance(new ChannelBufferInputStream(buffer.getOrignalBuffer()));

        PacketListener listener = listenerQueue.pop(seqNo);
        if (null != listener) {
            listener.onSuccess(codedInputStream);
            return;
        }

        switch (sid) {
            case IMBaseDefine.ServiceID.SID_LOGIN_VALUE:
                IMPacketDispatcher.dispatchLoginPacket(cid, codedInputStream);
                break;
            case IMBaseDefine.ServiceID.SID_BUDDY_LIST_VALUE:
                IMPacketDispatcher.dispatchBuddyPacket(cid, codedInputStream);
                break;
            case IMBaseDefine.ServiceID.SID_MSG_VALUE:
                IMPacketDispatcher.dispatchMsgPacket(cid, codedInputStream);
                break;
            case IMBaseDefine.ServiceID.SID_GROUP_VALUE:
                IMPacketDispatcher.dispatchGroupPacket(cid, codedInputStream);
                break;
            default:
                logger.e("IMSocketManager#dispatchPacket#unknown packet ====> serviceId: %d, commandId: %d", sid, cid);
                break;
        }
    }

    public void connectToMsgServer() {
        logger.e("IMSocketManager#connectToMsgServer(%s:%d)...",
                AppConstant.SysConstant.MSG_SERVER_IP, AppConstant.SysConstant.MSG_SERVER_PORT);

        if (null != msgServerThread) {
            msgServerThread.close();
            msgServerThread = null;
        }

        msgServerThread = new SocketThread(AppConstant.SysConstant.MSG_SERVER_IP, AppConstant.SysConstant.MSG_SERVER_PORT,
                new MsgServerHandler());
        msgServerThread.start();
    }

    public void reconnectToMsgServer() {
        synchronized (IMSocketManager.class) {
            connectToMsgServer();
        }
    }

    public void disconnectFromMsgServer() {
        logger.i("IMSocketManager#disconnectFromMsgServer");
        listenerQueue.onDestroy();
        if (null != msgServerThread) {
            msgServerThread.close();
            msgServerThread = null;
        }
    }

    public boolean isSocketConnected() {
        return null != msgServerThread && !msgServerThread.isClose();
    }

    public void handleMsgServerConnected() {
        logger.i("IMSocketManager#handleMsgServerConnected");
        listenerQueue.onStart();
        triggerEvent(SocketEvent.CONNECT_MSG_SERVER_SUCCESS);
        logger.i("IMSocketManager#handleMsgServerConnected#login to msg server...");
        IMLoginManager.getInstance().loginToMsgServer();
    }

    /**
     * 1. 被踢出           -- 不需要重连
     * 2. 心跳包没有收到    -- 链接断开，重连
     * 3. 链接主动断开      -- 重连
     * （在此之前的长连接状态为 connected）
     */
    // 先断开链接
    // only 2 threads(ui thread, network thread) would request sending  packet
    // let the ui thread to close the connection
    // so if the ui thread has a sending task, no synchronization issue
    public void handleMsgServerDisconnected() {
        logger.w("IMSocketManager#handleMsgServerDisconnected");
        disconnectFromMsgServer();
        triggerEvent(SocketEvent.MSG_SERVER_DISCONNECTED);
    }

    public void handleConnectMsgServerFailed() {
        triggerEvent(SocketEvent.CONNECT_MSG_SERVER_FAILED);
    }

    public SocketEvent getLatestSocketEvent() {
        return socketEvent;
    }

    public void setLatestSocketEvent(SocketEvent socketEvent) {
        this.socketEvent = socketEvent;
    }
}
