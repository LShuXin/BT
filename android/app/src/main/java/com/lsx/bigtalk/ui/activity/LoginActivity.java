package com.lsx.bigtalk.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.lsx.bigtalk.DB.sp.LoginSp;
import com.lsx.bigtalk.DB.sp.SystemConfigSp;
import com.lsx.bigtalk.R;
import com.lsx.bigtalk.config.IntentConstant;
import com.lsx.bigtalk.config.UrlConstant;
import com.lsx.bigtalk.utils.IMUIHelper;
import com.lsx.bigtalk.imservice.event.LoginEvent;
import com.lsx.bigtalk.imservice.event.SocketEvent;
import com.lsx.bigtalk.imservice.manager.IMLoginManager;
import com.lsx.bigtalk.imservice.service.IMService;
import com.lsx.bigtalk.ui.base.BTBaseActivity;
import com.lsx.bigtalk.imservice.support.IMServiceConnector;
import com.lsx.bigtalk.utils.Logger;

import de.greenrobot.event.EventBus;


/**
 * 一、 IMService 连接成功之后，从 loginSp 判断是否直接登陆
 * 直接登陆
 * 1. 从DB中获取历史的状态
 * 2. 建立长连接，请求最新的数据状态 【网络断开没有这个状态】
 * 3. 完成
 * 不直接登陆，
 * 1. 跳转到登陆页面
 * 2. 请求消息服务器地址，链接，验证，触发loginSuccess
 * 3. 保存登陆状态
 */
public class LoginActivity extends BTBaseActivity {
    private final Logger logger = Logger.getLogger(LoginActivity.class);
    private final Handler uiHandler = new Handler();
    private EditText mNameView;
    private EditText mPasswordView;
    private View loginPage;
    private View splashPage;
    private View mLoginStatusView;
    private InputMethodManager inputManager;

    private IMService imService;
    private boolean autoLogin = true;
    private boolean loginSuccess = false;

    private final IMServiceConnector imServiceConnector = new IMServiceConnector() {
        @Override
        public void onServiceDisconnected() {
            logger.d("LoginActivity#onServiceDisconnected");
        }

        @Override
        public void onIMServiceConnected() {
            logger.d("LoginActivity#onIMServiceConnected");
            imService = imServiceConnector.getIMService();
            try {
                do {
                    if (imService == null) {
                        logger.d("LoginActivity#imService is null");
                        break;
                    }
                    IMLoginManager loginManager = imService.getLoginManager();
                    LoginSp loginSp = imService.getLoginSp();
                    if (loginManager == null || loginSp == null) {
                        logger.d("LoginActivity#loginManager == null || loginSp == null");
                        break;
                    }

                    LoginSp.SpLoginIdentity loginIdentity = loginSp.getLoginIdentity();
                    if (loginIdentity == null) {
                        logger.d("LoginActivity#loginIdentity == null");
                        break;
                    }

                    mNameView.setText(loginIdentity.getLoginName());
                    if (TextUtils.isEmpty(loginIdentity.getPwd())) {
                        logger.d("LoginActivity#pwd is empty");
                        break;
                    }
                    mPasswordView.setText(loginIdentity.getPwd());

                    if (!autoLogin) {
                        break;
                    }

                    autoLogin(loginIdentity);
                    return;
                } while (false);

                // 异常分支都会执行这个
                manualLogin();
            } catch (Exception e) {
                // 任何未知的异常
                logger.w("LoginActivity#load login Identity failed: %s", e.getMessage());
                manualLogin();
            }
        }
    };

    private void manualLogin() {
        logger.d("LoginActivity#manualLogin");
        uiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                showLoginPage();
            }
        }, 1000);
    }

    private void autoLogin(final LoginSp.SpLoginIdentity loginIdentity) {
        logger.i("LoginActivity#prepare to auto login");

        uiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                logger.d("LoginActivity#auto login...");
                if (imService == null || imService.getLoginManager() == null) {
                    Toast.makeText(LoginActivity.this, getString(R.string.login_failed), Toast.LENGTH_SHORT).show();
                    showLoginPage();
                }
                imService.getLoginManager().login(loginIdentity);
            }
        }, 500);
    }

    private void showLoginPage() {
        splashPage.setVisibility(View.GONE);
        loginPage.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        logger.d("LoginActivity#onCreate");

        inputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        SystemConfigSp.instance().init(getApplicationContext());
        if (TextUtils.isEmpty(SystemConfigSp.instance().getStrConfig(SystemConfigSp.SysCfgDimension.LOGINSERVER))) {
            SystemConfigSp.instance().setStrConfig(SystemConfigSp.SysCfgDimension.LOGINSERVER, UrlConstant.ACCESS_MSG_ADDRESS);
        }

        imServiceConnector.connect(LoginActivity.this);
        EventBus.getDefault().register(this);

        setContentView(R.layout.login_activity);

        mNameView = findViewById(R.id.name);
        mPasswordView = findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });
        mLoginStatusView = findViewById(R.id.login_status);
        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputManager.hideSoftInputFromWindow(mPasswordView.getWindowToken(), 0);
                attemptLogin();
            }
        });
        initAutoLogin();
    }

    private void initAutoLogin() {
        logger.i("LoginActivity#initAutoLogin");

        splashPage = findViewById(R.id.splash_page);
        loginPage = findViewById(R.id.login_page);
        autoLogin = shouldAutoLogin();

        splashPage.setVisibility(autoLogin ? View.VISIBLE : View.GONE);
        loginPage.setVisibility(autoLogin ? View.GONE : View.VISIBLE);

        loginPage.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (mPasswordView != null) {
                    inputManager.hideSoftInputFromWindow(mPasswordView.getWindowToken(), 0);
                }

                if (mNameView != null) {
                    inputManager.hideSoftInputFromWindow(mNameView.getWindowToken(), 0);
                }

                return false;
            }
        });

        if (autoLogin) {
            Animation splashAnimation = AnimationUtils.loadAnimation(this, R.anim.splash_fade_in);
            if (splashAnimation == null) {
                logger.e("LoginActivity#loadAnimation 'login_splash' failed");
                return;
            }

            splashPage.startAnimation(splashAnimation);
        }
    }

    // 主动退出的时候， 这个地方会有值,更具pwd来判断
    private boolean shouldAutoLogin() {
        Intent intent = getIntent();
        if (intent != null) {
            boolean notAutoLogin = intent.getBooleanExtra(IntentConstant.KEY_LOGIN_NOT_AUTO, false);
            logger.d("LoginActivity#notAutoLogin:%s", notAutoLogin);
            return !notAutoLogin;
        }
        return true;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        imServiceConnector.disconnect(LoginActivity.this);
        EventBus.getDefault().unregister(this);
        splashPage = null;
        loginPage = null;
    }

    public void attemptLogin() {
        String loginName = mNameView.getText().toString();
        String mPassword = mPasswordView.getText().toString();
        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(mPassword)) {
            Toast.makeText(this, getString(R.string.error_pwd_required), Toast.LENGTH_SHORT).show();
            focusView = mPasswordView;
            cancel = true;
        }

        if (TextUtils.isEmpty(loginName)) {
            Toast.makeText(this, getString(R.string.error_name_required), Toast.LENGTH_SHORT).show();
            focusView = mNameView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            if (imService != null) {
                loginName = loginName.trim();
                mPassword = mPassword.trim();
                imService.getLoginManager().login(loginName, mPassword);
            }
        }
    }

    private void showProgress(final boolean show) {
        if (show) {
            mLoginStatusView.setVisibility(View.VISIBLE);
        } else {
            mLoginStatusView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        logger.d("LoginActivity#onBackPressed");
        super.onBackPressed();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    /**
     * ----------------------------event 事件驱动----------------------------
     */
    public void onEventMainThread(LoginEvent event) {
        switch (event) {
            case LOCAL_LOGIN_SUCCESS:
            case LOGIN_OK:
            {
                onLoginSuccess();
                break;
            }
            case LOGIN_AUTH_FAILED:
            case LOGIN_INNER_FAILED:
            {
                if (!loginSuccess) {
                    onLoginFailure(event);
                }
                break;
            }
        }
    }

    public void onEventMainThread(SocketEvent event) {
        switch (event) {
            case CONNECT_MSG_SERVER_FAILED:
            case REQ_MSG_SERVER_ADDRS_FAILED:
            {
                if (!loginSuccess) {
                    onSocketFailure(event);
                }
                break;
            }
        }
    }

    private void onLoginSuccess() {
        logger.i("LoginActivity#onLoginSuccess");
        loginSuccess = true;
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        LoginActivity.this.finish();
    }

    private void onLoginFailure(LoginEvent event) {
        logger.e("LoginActivity#onLoginFailure errorCode:%s", event.name());
        showLoginPage();
        String errorTip = getString(IMUIHelper.getLoginErrorTip(event));
        logger.d("LoginActivity#onLoginFailure errorTip:%s", errorTip);
        mLoginStatusView.setVisibility(View.GONE);
        Toast.makeText(this, errorTip, Toast.LENGTH_SHORT).show();
    }

    private void onSocketFailure(SocketEvent event) {
        logger.e("LoginActivity#onSocketFailure errorCode:%s", event.name());
        showLoginPage();
        String errorTip = getString(IMUIHelper.getSocketErrorTip(event));
        logger.d("LoginActivity#onSocketFailure errorTip:%s", errorTip);
        mLoginStatusView.setVisibility(View.GONE);
        Toast.makeText(this, errorTip, Toast.LENGTH_SHORT).show();
    }
}
