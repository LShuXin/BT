package com.lsx.bigtalk.service.manager;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import com.google.protobuf.CodedInputStream;

import de.greenrobot.event.EventBus;

import com.lsx.bigtalk.service.event.LoginEvent;
import com.lsx.bigtalk.storage.db.BTDB;
import com.lsx.bigtalk.storage.db.entity.UserEntity;
import com.lsx.bigtalk.service.callback.PacketListener;
import com.lsx.bigtalk.pb.IMBaseDefine;
import com.lsx.bigtalk.pb.IMBuddy;
import com.lsx.bigtalk.pb.IMLogin;
import com.lsx.bigtalk.pb.helper.ProtoBuf2JavaBean;
import com.lsx.bigtalk.logs.Logger;
import com.lsx.bigtalk.storage.sp.BTSp;


public class IMLoginManager extends IMManager {
    private final Logger logger = Logger.getLogger(IMLoginManager.class);
    IMSocketManager imSocketManager = IMSocketManager.getInstance();
    private String loginName;
    private String loginPwd;
    private int loginId;
    private UserEntity userEntity;
    private boolean identityChanged = false;
    private boolean isKickedOut = false;
    private boolean isPcOnline = false;
    private boolean isEverLoggedIn = false;
    private boolean isLocalLogin = false;
    private LoginEvent LoginEvent = com.lsx.bigtalk.service.event.LoginEvent.NONE;
    @SuppressLint("StaticFieldLeak")
    private static IMLoginManager instance;

    public static synchronized IMLoginManager getInstance() {
        if (null == instance) {
            instance = new IMLoginManager();
        }
        return instance;
    }

    public IMLoginManager() {

    }
    
    @Override
    public void doOnStart() {
        
    }

    @Override
    public void reset() {
        loginName = null;
        loginPwd = null;
        loginId = -1;
        userEntity = null;
        identityChanged = false;
        isKickedOut = false;
        isPcOnline = false;
        isEverLoggedIn = false;
        LoginEvent = LoginEvent.NONE;
        isLocalLogin = false;
    }

    public void triggerEvent(LoginEvent event) {
        LoginEvent = event;
        EventBus.getDefault().postSticky(event);
    }

    public void logOut() {
        logger.d("IMLoginManager#logOut");
        isEverLoggedIn = false;
        isLocalLogin = false;
        reqLogOut();
    }
    
    private void reqLogOut() {
        IMLogin.IMLogoutReq imLogoutReq = IMLogin.IMLogoutReq.newBuilder()
                .build();
        int sid = IMBaseDefine.ServiceID.SID_LOGIN_VALUE;
        int cid = IMBaseDefine.LoginCmdID.CID_LOGIN_REQ_LOGINOUT_VALUE;
        try {
            imSocketManager.sendRequest(imLogoutReq, sid, cid);
        } catch (Exception e) {
            logger.e("IMLoginManager#reqLoginOut error:" + e);
        } finally {
            BTSp.getInstance().setLoginInfo(loginName, null, loginId);
            triggerEvent(LoginEvent.LOGIN_OUT);
        }
    }
    
    public void handleLogOutResp(IMLogin.IMLogoutRsp imLogoutRsp) {
        int code = imLogoutRsp.getResultCode();
        logger.d("IMLoginManager#handleLogOutResp, code: %d", code);
    }

    public void reLogin() {
        logger.d("IMLoginManager#reLogin");
        if (!TextUtils.isEmpty(loginName) && !TextUtils.isEmpty(loginPwd)) {
            loginToMsgServer();
        } else {
            logger.d("IMLoginManager#reLogin failed: userName or loginPwd is null!!");
            isEverLoggedIn = false;
            triggerEvent(LoginEvent.LOGIN_AUTH_FAILED);
        }
    }
    
    public void login(BTSp.LoginModel loginModel) {
        if (loginModel == null) {
            triggerEvent(LoginEvent.LOGIN_AUTH_FAILED);
            return;
        }
        loginName = loginModel.getUserName();
        loginPwd = loginModel.getPassword();
        identityChanged = false;

        int mLoginId = loginModel.getLoginId();
        BTDB.instance().initDbHelp(ctx, mLoginId);
        UserEntity loginEntity = BTDB.instance().getByLoginId(mLoginId);
        do {
            if (loginEntity == null) {
                break;
            }
            userEntity = loginEntity;
            loginId = loginEntity.getPeerId();
            isLocalLogin = true;
            isEverLoggedIn = true;
            triggerEvent(LoginEvent.LOCAL_LOGIN_SUCCESS);
        } while (false);
       
        imSocketManager.connectToMsgServer();
    }


    public void login(String userName, String password) {
        logger.i("IMLoginManager#login userName: %s, password: %s", userName, password);
        
        BTSp.LoginModel identity = BTSp.getInstance().getLoginInfo();
        if (identity != null && !TextUtils.isEmpty(identity.getPassword())) {
            if (identity.getPassword().equals(password) && identity.getUserName().equals(userName)) {
                login(identity);
                return;
            }
        }

        loginName = userName;
        loginPwd = password;
        identityChanged = true;
        imSocketManager.connectToMsgServer();
    }


    public void loginToMsgServer() {
        logger.i("login#reqLoginMsgServer");
        triggerEvent(LoginEvent.LOGINING);
        /** 加密 */
        String desPwd = new String(com.lsx.bigtalk.Security.getInstance().EncryptPwd(loginPwd));

        IMLogin.IMLoginReq imLoginReq = IMLogin.IMLoginReq.newBuilder()
                .setUserName(loginName)
                .setPassword(desPwd)
                .setOnlineStatus(IMBaseDefine.UserStatType.USER_STATUS_ONLINE)
                .setClientType(IMBaseDefine.ClientType.CLIENT_TYPE_ANDROID)
                .setClientVersion("1.0.0").build();

        int sid = IMBaseDefine.ServiceID.SID_LOGIN_VALUE;
        int cid = IMBaseDefine.LoginCmdID.CID_LOGIN_REQ_USERLOGIN_VALUE;
        imSocketManager.sendRequest(imLoginReq, sid, cid, new PacketListener() {
            @Override
            public void onSuccess(Object response) {
                try {
                    IMLogin.IMLoginRes imLoginRes = IMLogin.IMLoginRes.parseFrom((CodedInputStream) response);
                    onRepMsgServerLogin(imLoginRes);
                } catch (IOException e) {
                    triggerEvent(LoginEvent.LOGIN_INNER_FAILED);
                    logger.e("login failed,cause by %s", e.getCause());
                }
            }

            @Override
            public void onFailed() {
                triggerEvent(LoginEvent.LOGIN_INNER_FAILED);
            }

            @Override
            public void onTimeout() {
                triggerEvent(LoginEvent.LOGIN_INNER_FAILED);
            }
        });
    }

    /**
     * 验证登陆信息结果
     *
     * @param loginRes
     */
    public void onRepMsgServerLogin(IMLogin.IMLoginRes loginRes) {
        logger.i("login#onRepMsgServerLogin");

        if (loginRes == null) {
            logger.e("login#decode LoginResponse failed");
            triggerEvent(LoginEvent.LOGIN_AUTH_FAILED);
            return;
        }

        IMBaseDefine.ResultType code = loginRes.getResultCode();
        switch (code) {
            case REFUSE_REASON_NONE: {
                IMBaseDefine.UserStatType userStatType = loginRes.getOnlineStatus();
                IMBaseDefine.UserInfo userInfo = loginRes.getUserInfo();
                loginId = userInfo.getUserId();
                userEntity = ProtoBuf2JavaBean.getUserEntity(userInfo);
                onLoginOk();
            }
            break;

            case REFUSE_REASON_DB_VALIDATE_FAILED: {
                logger.e("login#login msg server failed, result:%s", code);
                triggerEvent(LoginEvent.LOGIN_AUTH_FAILED);
            }
            break;

            default: {
                logger.e("login#login msg server inner failed, result:%s", code);
                triggerEvent(LoginEvent.LOGIN_INNER_FAILED);
            }
            break;
        }
    }

    public void onLoginOk() {
        logger.i("login#onLoginOk");
        isEverLoggedIn = true;
        isKickedOut = false;

        // 判断登陆的类型
        if (isLocalLogin) {
            triggerEvent(LoginEvent.REMOTE_LOGIN_SUCCESS);
        } else {
            isLocalLogin = true;
            triggerEvent(LoginEvent.NORMAL_LOGIN_SUCCESS);
        }

        // 发送token
//        reqDeviceToken();
        if (identityChanged) {
            BTSp.getInstance().setLoginInfo(loginName, loginPwd, loginId);
            identityChanged = false;
        }
    }


    private void reqDeviceToken() {
//        String token = PushManager.getInstance().getToken();
//        IMLogin.IMDeviceTokenReq req = IMLogin.IMDeviceTokenReq.newBuilder()
//                .setUserId(loginId)
//                .setClientType(IMBaseDefine.ClientType.CLIENT_TYPE_ANDROID)
//                .setDeviceToken(token)
//                .build();
//        int sid = IMBaseDefine.ServiceID.SID_LOGIN_VALUE;
//        int cid = IMBaseDefine.LoginCmdID.CID_LOGIN_REQ_DEVICETOKEN_VALUE;
//
//        imSocketManager.sendRequest(req,sid,cid,new PacketListener() {
//            @Override
//            public void onSuccess(Object response) {
//                //?? nothing to do
////                try {
////                    IMLogin.IMDeviceTokenRsp rsp = IMLogin.IMDeviceTokenRsp.parseFrom((CodedInputStream) response);
////                    int userId = rsp.getUserId();
////                } catch (IOException e) {
////                    e.printStackTrace();
////                }
//            }
//
//            @Override
//            public void onFailed() {}
//
//            @Override
//            public void onTimeout() {}
//        });
    }


    public void onKickout(IMLogin.IMKickUser imKickUser) {
        logger.i("login#onKickout");
        int kickUserId = imKickUser.getUserId();
        IMBaseDefine.KickReasonType reason = imKickUser.getKickReason();
        isKickedOut = true;
        imSocketManager.handleMsgServerDisconnected();
    }

    public void onLoginEventNotify(IMBuddy.IMPCLoginEventNotify statusNotify) {
        int userId = statusNotify.getUserId();
        // todo 由于交互不太友好 暂时先去掉
        if (true) {
            logger.i("login#onLoginEventNotify userId ≠ loginId");
            return;
        }

        if (isKickedOut) {
            logger.i("login#already isKickedOut");
            return;
        }

        switch (statusNotify.getLoginStat()) {
            case USER_STATUS_ONLINE: {
                isPcOnline = true;
                EventBus.getDefault().postSticky(LoginEvent.PC_ONLINE);
            }
            break;

            case USER_STATUS_OFFLINE: {
                isPcOnline = false;
                EventBus.getDefault().postSticky(LoginEvent.PC_OFFLINE);
            }
            break;
        }
    }

    public void reqKickPCClient() {
        IMLogin.IMKickPCClientReq req = IMLogin.IMKickPCClientReq.newBuilder()
                .setUserId(loginId)
                .build();
        int sid = IMBaseDefine.ServiceID.SID_LOGIN_VALUE;
        int cid = IMBaseDefine.LoginCmdID.CID_LOGIN_REQ_KICKPCCLIENT_VALUE;
        imSocketManager.sendRequest(req, sid, cid, new PacketListener() {
            @Override
            public void onSuccess(Object response) {
                triggerEvent(LoginEvent.KICK_PC_SUCCESS);
            }

            @Override
            public void onFailed() {
                triggerEvent(LoginEvent.KICK_PC_FAILED);
            }

            @Override
            public void onTimeout() {
                triggerEvent(LoginEvent.KICK_PC_FAILED);
            }
        });
    }

    public LoginEvent getLoginEvent() {
        return LoginEvent;
    }

    public void setLoginId(int loginId) {
        this.loginId = loginId;
    }

    public int getLoginId() {
        return loginId;
    }

    public void setIsEverLoggedIn(boolean isEverLoggedIn) {
        this.isEverLoggedIn = isEverLoggedIn;
    }

    public boolean getIsEverLoggedIn() {
        return isEverLoggedIn;
    }

    public void setUserEntity(UserEntity userEntity) {
        this.userEntity = userEntity;
    }
    
    public UserEntity getUserEntity() {
        return userEntity;
    }
    
    public void setIsKickedOut(boolean isKickedOut) {
        this.isKickedOut = isKickedOut;
    }

    public boolean getIsKickedOut() {
        return isKickedOut;
    }
    
    public void setIsPcOnline(boolean isPcOnline) {
        this.isPcOnline = isPcOnline;
    }

    public boolean getIsPcOnline() {
        return isPcOnline;
    }
}
