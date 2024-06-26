package com.lsx.bigtalk.imservice.manager;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.GeneratedMessageLite;
import com.lsx.bigtalk.DB.sp.SystemConfigSp;
import com.lsx.bigtalk.config.SysConstant;
import com.lsx.bigtalk.imservice.callback.ListenerQueue;
import com.lsx.bigtalk.imservice.callback.Packetlistener;
import com.lsx.bigtalk.imservice.event.SocketEvent;
import com.lsx.bigtalk.imservice.network.MsgServerHandler;
import com.lsx.bigtalk.imservice.network.SocketThread;
import com.lsx.bigtalk.protobuf.IMBaseDefine;
import com.lsx.bigtalk.protobuf.base.DataBuffer;
import com.lsx.bigtalk.protobuf.base.DefaultHeader;
import com.lsx.bigtalk.protobuf.base.Header;
import com.lsx.bigtalk.utils.Logger;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferInputStream;
import org.json.JSONException;
import org.json.JSONObject;

import de.greenrobot.event.EventBus;


public class IMSocketManager extends IMManager {
    private final Logger logger = Logger.getLogger(IMSocketManager.class);
    private final ListenerQueue listenerQueue = ListenerQueue.instance();
    private SocketThread msgServerThread;
    private SocketEvent socketStatus = SocketEvent.NONE;

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
        socketStatus = SocketEvent.NONE;
    }

    @Override
    public void reset() {
        disconnectFromMsgServer();
        socketStatus = SocketEvent.NONE;
    }

    public void triggerEvent(SocketEvent event) {
        setSocketStatus(event);
        EventBus.getDefault().postSticky(event);
    }


    public void sendRequest(GeneratedMessageLite request, int sid, int cid) {
        sendRequest(request, sid, cid, null);
    }

    public void sendRequest(GeneratedMessageLite request, int sid, int cid, Packetlistener packetlistener) {
        int seqNo = 0;
        try {
            Header header = new DefaultHeader(sid, cid);
            int bodySize = request.getSerializedSize();
            header.setLength(SysConstant.PROTOCOL_HEADER_LENGTH + bodySize);
            seqNo = header.getSeqnum();
            listenerQueue.push(seqNo, packetlistener);
            boolean success = msgServerThread.sendRequest(request, header);
            logger.d("IMSocketManager#sendRequest: %d", success);
        } catch (Exception e) {
            if (packetlistener != null) {
                packetlistener.onFaild();
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

        Packetlistener listener = listenerQueue.pop(seqNo);
        if (listener != null) {
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
                logger.e("IMSocketManager#dispatchPacket#unhandled serviceId:%d, commandId:%d", sid, cid);
                break;
        }
    }

    public void connectToMsgServer() {
        logger.e("IMSocketManager#connectToMsgServer(%s:%d)...",
                SysConstant.MSG_SERVER_IP, SysConstant.MSG_SERVER_PORT);

        if (msgServerThread != null) {
            msgServerThread.close();
            msgServerThread = null;
        }

        msgServerThread = new SocketThread(SysConstant.MSG_SERVER_IP, SysConstant.MSG_SERVER_PORT,
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

        listenerQueue.onDestory();
        if (msgServerThread != null) {
            msgServerThread.close();
            msgServerThread = null;
        }
    }

    public boolean isSocketConnected() {
        return msgServerThread != null && !msgServerThread.isClose();
    }

    public void onMsgServerConnected() {
        logger.i("IMSocketManager#onMsgServerConnected");
        listenerQueue.onStart();
        triggerEvent(SocketEvent.CONNECT_MSG_SERVER_SUCCESS);
        logger.i("IMSocketManager#onMsgServerConnected#need to login to msg server");
        IMLoginManager.getInstance().loginToMsgServer();
    }

    /**
     * 1. kickout 被踢出会触发这个状态   -- 不需要重连
     * 2. 心跳包没有收到 会触发这个状态   -- 链接断开，重连
     * 3. 链接主动断开                 -- 重连
     * 之前的长连接状态 connected
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

    public void onConnectMsgServerFail() {
        triggerEvent(SocketEvent.CONNECT_MSG_SERVER_FAILED);
    }

    public SocketEvent getSocketStatus() {
        return socketStatus;
    }

    public void setSocketStatus(SocketEvent socketStatus) {
        this.socketStatus = socketStatus;
    }
}
