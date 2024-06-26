package com.lsx.bigtalk.ui.fragment;

import java.util.List;
import java.util.Objects;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

import de.greenrobot.event.EventBus;

import com.lsx.bigtalk.config.DBConstant;
import com.lsx.bigtalk.DB.entity.GroupEntity;
import com.lsx.bigtalk.R;
import com.lsx.bigtalk.ui.adapter.SessionAdapter;
import com.lsx.bigtalk.helper.IMUIHelper;
import com.lsx.bigtalk.imservice.SessionInfo;
import com.lsx.bigtalk.imservice.event.GroupEvent;
import com.lsx.bigtalk.imservice.event.LoginStatus;
import com.lsx.bigtalk.imservice.event.ReconnectEvent;
import com.lsx.bigtalk.imservice.event.SessionEvent;
import com.lsx.bigtalk.imservice.event.SocketEvent;
import com.lsx.bigtalk.imservice.event.UnreadEvent;
import com.lsx.bigtalk.imservice.event.UserInfoEvent;
import com.lsx.bigtalk.imservice.manager.IMLoginManager;
import com.lsx.bigtalk.imservice.manager.IMReconnectManager;
import com.lsx.bigtalk.imservice.manager.IMUnreadMsgManager;
import com.lsx.bigtalk.imservice.service.IMService;
import com.lsx.bigtalk.ui.activity.MainActivity;
import com.lsx.bigtalk.imservice.support.IMServiceConnector;
import com.lsx.bigtalk.utils.NetworkUtil;


public class SessionFragment extends MainFragment
        implements
        OnItemSelectedListener,
        OnItemClickListener,
        OnItemLongClickListener {

    private SessionAdapter sessionAdapter;
    private ListView sessionListView;
    private View curView = null;
    private View noNetworkView;
    private View noChatView;
    private ImageView notifyImage;
    private TextView displayView;
    private ProgressBar reconnectingProgressBar;
    private IMService imService;
    
    private volatile boolean isManualReconnect = false;

    private final IMServiceConnector imServiceConnector = new IMServiceConnector() {
        @Override
        public void onServiceDisconnected() {
            if (EventBus.getDefault().isRegistered(SessionFragment.this)) {
                EventBus.getDefault().unregister(SessionFragment.this);
            }
        }
        @Override
        public void onIMServiceConnected() {
            logger.d("ChatFragment#onIMServiceConnected");
            imService = imServiceConnector.getIMService();
            if (imService == null) {
                return;
            }
            // 依赖联系人会话、未读消息、用户的信息三者的状态
            onSessionDataReady();
            // registerSticky 方法用于注册“粘性”事件订阅者，这意味着订阅者会接收到之前发送的
            // 最新事件（如果有的话），然后继续接收后续的事件。
            EventBus.getDefault().registerSticky(SessionFragment.this);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imServiceConnector.connect(getActivity());
        logger.d("ChatFragment#onCreate");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        logger.d("ChatFragment#onCreateView");
        if (null != curView) {
            ((ViewGroup) curView.getParent()).removeView(curView);
        }
        curView = inflater.inflate(R.layout.session_fragment, baseFragmentLayout);
        // 多端登陆也在用这个view
        noNetworkView = curView.findViewById(R.id.layout_no_network);
        noChatView = curView.findViewById(R.id.layout_no_chat);
        reconnectingProgressBar = curView.findViewById(R.id.progressbar_reconnect);
        displayView = curView.findViewById(R.id.disconnect_text);
        notifyImage = curView.findViewById(R.id.imageWifi);

        super.init(curView);
        // 初始化顶部view
        initTitleView();
        // 初始化联系人列表视图
        initSessionListView();
        // 创建时没有数据，显示加载动画
        showProgressBar();
        return curView;
    }

    @Override
    public void onStart() {
        logger.d("ChatFragment#onStart");
        super.onStart();
    }

    @Override
    public void onStop() {
        logger.d("ChatFragment#onStop");
        super.onStop();
    }

    @Override
    public void onPause() {
        logger.d("ChatFragment#onPause");
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (EventBus.getDefault().isRegistered(SessionFragment.this)) {
            EventBus.getDefault().unregister(SessionFragment.this);
        }
        imServiceConnector.disconnect(getActivity());
        super.onDestroy();
    }

    private void initTitleView() {
        setTopCenterTitleTextBold(requireActivity().getString(R.string.chat_title));
    }

    private void initSessionListView() {
        sessionListView = curView.findViewById(R.id.session_listview);
        sessionListView.setOnItemClickListener(this);
        sessionListView.setOnItemLongClickListener(this);
        sessionAdapter = new SessionAdapter(getActivity());
        sessionListView.setAdapter(sessionAdapter);

        // this is critical, disable loading when finger sliding, otherwise
        // you'll find sliding is not very smooth
        sessionListView.setOnScrollListener(
                new PauseOnScrollListener(ImageLoader.getInstance(), true, true));
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    // 这个地方跳转一定要快
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        SessionInfo sessionInfo = sessionAdapter.getItem(position);
        if (sessionInfo == null) {
            return;
        }
        IMUIHelper.openChatActivity(getActivity(), sessionInfo.getSessionKey());
    }

    public void onEventMainThread(SessionEvent sessionEvent) {
        logger.d("ChatFragment#onEventMainThread sessionEvent: %s", sessionEvent);
        switch (sessionEvent) {
            case SESSION_UPDATE:
            case SESSION_LIST_SUCCESS:
            case SET_SESSION_SPIN:
                onSessionDataReady();
                break;
        }
    }

    public void onEventMainThread(GroupEvent event) {
        switch (event.getEvent()) {
            case GROUP_INFO_OK:
            case CHANGE_GROUP_MEMBER_SUCCESS:
            case GROUP_INFO_UPDATED:
                {
                    onSessionDataReady();
                    searchDataReady();
                }
                break;

            case SHIELD_GROUP_OK:
                // 更新最下栏的未读计数、更新session
                onShieldSuccess(event.getGroupEntity());
                break;

            case SHIELD_GROUP_FAIL:
            case SHIELD_GROUP_TIMEOUT:
                onShieldFail();
                break;
        }
    }

    public void onEventMainThread(UnreadEvent event) {
        switch (event.event) {
            case UNREAD_MSG_RECEIVED:
            case UNREAD_MSG_LIST_OK:
            case SESSION_READ_UNREAD_MSG:
                onSessionDataReady();
                break;
        }
    }

    public void onEventMainThread(UserInfoEvent event) {
        switch (event) {
            case USER_INFO_UPDATE:
            case USER_INFO_OK:
                onSessionDataReady();
                searchDataReady();
                break;
        }
    }

    public void onEventMainThread(LoginStatus LoginStatus) {
        logger.d("ChatFragment#onEventMainThread LoginStatus: %s", LoginStatus);
        switch (LoginStatus) {
            case LOCAL_LOGIN_SUCCESS:
            case LOGINING:
                {
                    if (reconnectingProgressBar != null) {
                        reconnectingProgressBar.setVisibility(View.VISIBLE);
                    }
                }
                break;

            case LOCAL_LOGIN_MSG_SERVICE:
            case LOGIN_OK:
                {
                    isManualReconnect = false;
                    noNetworkView.setVisibility(View.GONE);
                }
                break;

            case LOGIN_AUTH_FAILED:
            case LOGIN_INNER_FAILED:
                onLoginFailure(LoginStatus);
                break;

            case PC_OFFLINE:
            case KICK_PC_SUCCESS:
                onPCLoginStatusNotify(false);
                break;

            case KICK_PC_FAILED:
                Toast.makeText(getActivity(), getString(R.string.kick_pc_failed), Toast.LENGTH_SHORT).show();
                break;

            case PC_ONLINE:
                onPCLoginStatusNotify(true);
                break;

            default:
                reconnectingProgressBar.setVisibility(View.GONE);
                break;
        }
    }

    public void onEventMainThread(SocketEvent socketEvent) {
        switch (socketEvent) {
            case MSG_SERVER_DISCONNECTED:
                handleServerDisconnected();
                break;

            case CONNECT_MSG_SERVER_FAILED:
                handleServerDisconnected();
                onSocketFailure(socketEvent);
                break;
        }
    }

    public void onEventMainThread(ReconnectEvent reconnectEvent) {
        if (Objects.requireNonNull(reconnectEvent) == ReconnectEvent.DISABLE) {
            handleServerDisconnected();
        }
    }

    private void onLoginFailure(LoginStatus event) {
        if (!isManualReconnect) {
            return;
        }
        isManualReconnect = false;
        String errorTip = getString(IMUIHelper.getLoginErrorTip(event));
        logger.d("chat_fragment#onLoginFailure errorTip: %s", errorTip);
        reconnectingProgressBar.setVisibility(View.GONE);
        Toast.makeText(getActivity(), errorTip, Toast.LENGTH_SHORT).show();
    }

    private void onSocketFailure(SocketEvent event) {
        if (!isManualReconnect) {
            return;
        }
        isManualReconnect = false;
        String errorTip = getString(IMUIHelper.getSocketErrorTip(event));
        logger.d("chat_fragment#onSocketFailure errorTip: %s", errorTip);
        reconnectingProgressBar.setVisibility(View.GONE);
        Toast.makeText(getActivity(), errorTip, Toast.LENGTH_SHORT).show();
    }

    // 屏蔽群成功，更新页面以内容以及下面的未读总计数
    private void onShieldSuccess(GroupEntity entity) {
        if (entity == null) {
            return;
        }
        sessionAdapter.shieldSession(entity);
        IMUnreadMsgManager unreadMsgManager = imService.getIMUnReadMsgManager();
        int totalUnreadMsgCnt = unreadMsgManager.getTotalUnreadCount();
        ((MainActivity) requireActivity()).setUnreadMessageCnt(totalUnreadMsgCnt);
    }

    private void onShieldFail() {
        Toast.makeText(getActivity(), R.string.req_msg_failed, Toast.LENGTH_SHORT).show();
    }

    /**
     * 搜索数据需要一些前置处理，处理完成后才显示搜索框
     */
    public void searchDataReady() {
        if (imService.getIMContactManager().getIsContactDataReady() &&
                imService.getIMGroupManager().isGroupDataReady()) {
            showTopSearchBarFrameLayout();
        }
    }

    /**
     * 多端，PC端在线状态通知
     *
     * @param isOnline
     */
    public void onPCLoginStatusNotify(boolean isOnline) {
        logger.d("chat_fragment#onPCLoginStatusNotify");
        if (isOnline) {
            reconnectingProgressBar.setVisibility(View.GONE);
            noNetworkView.setVisibility(View.VISIBLE);
            notifyImage.setImageResource(R.drawable.pc_notify);
            displayView.setText(R.string.pc_status_notify);

            noNetworkView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    reconnectingProgressBar.setVisibility(View.VISIBLE);
                    imService.getIMLoginManager().reqKickPCClient();
                }
            });
        } else {
            noNetworkView.setVisibility(View.GONE);
        }
    }

    private void handleServerDisconnected() {
        logger.d("chat_fragment#handleServerDisconnected");

        if (reconnectingProgressBar != null) {
            reconnectingProgressBar.setVisibility(View.GONE);
        }

        if (noNetworkView != null) {
            notifyImage.setImageResource(R.drawable.warning);
            noNetworkView.setVisibility(View.VISIBLE);
            if (imService != null) {
                if (imService.getIMLoginManager().getIsKickedOut()) {
                    displayView.setText(R.string.disconnect_kickout);
                } else {
                    displayView.setText(R.string.no_network);
                }
            }
            /**重连【断线、被其他移动端挤掉】*/
            noNetworkView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    logger.d("chatFragment#noNetworkView clicked");
                    IMReconnectManager manager = imService.getIMReconnectManager();
                    if (NetworkUtil.isNetWorkAvailable((Application) getContext().getApplicationContext())) {
                        isManualReconnect = true;
                        IMLoginManager.getInstance().reLogin();
                    } else {
                        Toast.makeText(getActivity(), R.string.no_network_toast, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    reconnectingProgressBar.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    /**
     * 这个处理有点过于粗暴
     */
    private void onSessionDataReady() {
        boolean isUserData = imService.getIMContactManager().getIsContactDataReady();
        boolean isSessionData = imService.getIMSessionManager().isSessionListReady();
        boolean isGroupData = imService.getIMGroupManager().isGroupDataReady();

        if (!(isUserData && isSessionData && isGroupData)) {
            return;
        }
        IMUnreadMsgManager unreadMsgManager = imService.getIMUnReadMsgManager();

        int totalUnreadMsgCnt = unreadMsgManager.getTotalUnreadCount();
        logger.d("unread#total cnt %d", totalUnreadMsgCnt);
        ((MainActivity) getActivity()).setUnreadMessageCnt(totalUnreadMsgCnt);

        List<SessionInfo> recentSessionList = imService.getIMSessionManager().getRecentListInfo();

        setNoChatView(recentSessionList);
        sessionAdapter.setData(recentSessionList);
        hideProgressBar();
        showTopSearchBarFrameLayout();
    }

    private void setNoChatView(List<SessionInfo> recentSessionList) {
        if (recentSessionList.size() == 0) {
            noChatView.setVisibility(View.VISIBLE);
        } else {
            noChatView.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view,
                                   int position, long id) {

        SessionInfo SessionInfo = sessionAdapter.getItem(position);
        if (SessionInfo == null) {
            logger.e("recent#onItemLongClick null SessionInfo -> position:%d", position);
            return false;
        }
        if (SessionInfo.getSessionType() == DBConstant.SESSION_TYPE_SINGLE) {
            handleMsgContactItemLongPressed(getActivity(), SessionInfo);
        } else {
            handleGroupItemLongClick(getActivity(), SessionInfo);
        }
        return true;
    }

    private void handleMsgContactItemLongPressed(final Context ctx, final SessionInfo sessionInfo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(ctx, android.R.style.Theme_Holo_Light_Dialog));
        builder.setTitle(sessionInfo.getName());
        final boolean isTop = imService.getConfigSp().isTopSession(sessionInfo.getSessionKey());

        int topMessageRes = isTop ? R.string.cancel_top_message : R.string.top_message;
        String[] items = new String[]{ctx.getString(R.string.check_profile),
                ctx.getString(R.string.delete_session),
                ctx.getString(topMessageRes)};

        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        IMUIHelper.openUserProfileActivity(ctx, sessionInfo.getPeerId());
                        break;
                    case 1:
                        imService.getIMSessionManager().reqRemoveSession(sessionInfo);
                        break;
                    case 2: {
                        imService.getConfigSp().setSessionTop(sessionInfo.getSessionKey(), !isTop);
                    }
                    break;
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();
    }
    
    private void handleGroupItemLongClick(final Context ctx, final SessionInfo sessionInfo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(ctx, android.R.style.Theme_Holo_Light_Dialog));
        builder.setTitle(sessionInfo.getName());
        final boolean isSpin = imService.getConfigSp().isTopSession(sessionInfo.getSessionKey());
        final boolean isShield = sessionInfo.getIsShield();
        int spinMessageRes = isSpin ? R.string.cancel_top_message : R.string.top_message;
        int shieldMessageRes = isShield ? R.string.cancel_forbid_group_message : R.string.forbid_group_message;
        String[] items = new String[]{ctx.getString(R.string.delete_session), ctx.getString(spinMessageRes), ctx.getString(shieldMessageRes)};
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        imService.getIMSessionManager().reqRemoveSession(sessionInfo);
                        break;
                    case 1: 
                        imService.getConfigSp().setSessionTop(sessionInfo.getSessionKey(), !isSpin);
                        break;
                    case 2:
                        int shieldType = isShield ? DBConstant.GROUP_STATUS_ONLINE : DBConstant.GROUP_STATUS_SHIELD;
                        imService.getIMGroupManager().shieldGroup(sessionInfo.getPeerId(), shieldType);
                        break;
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();
    }

    @Override
    protected void initHandler() {
        // TODO Auto-generated method stub
    }
    
    public void scrollToUnreadPosition() {
        if (sessionListView != null) {
            int currentPosition = sessionListView.getFirstVisiblePosition();
            int needPosition = sessionAdapter.getUnreadPositionOnView(currentPosition);
            // does not work
            // sessionListView.smoothScrollToPosition(needPosition);
            sessionListView.setSelection(needPosition);
        }
    }
}
