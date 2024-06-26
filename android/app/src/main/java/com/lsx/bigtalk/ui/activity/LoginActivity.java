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
import com.lsx.bigtalk.helper.IMUIHelper;
import com.lsx.bigtalk.imservice.event.LoginStatus;
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
    private EditText nameEditText;
    private EditText pwdEditText;
    private View loginPage;
    private View splashPage;
    private View loginStatusView;
    private InputMethodManager inputMethodManager;
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
                        logger.d("LoginActivity#onIMServiceConnected#imService is null");
                        break;
                    }
                    IMLoginManager imLoginManager = imService.getIMLoginManager();
                    LoginSp loginSp = imService.getLoginSp();
                    if (imLoginManager == null || loginSp == null) {
                        logger.d("LoginActivity#imLoginManager == null || loginSp == null");
                        break;
                    }

                    LoginSp.SpLoginIdentity loginIdentity = loginSp.getLoginIdentity();
                    if (loginIdentity == null) {
                        logger.d("LoginActivity#loginIdentity == null");
                        break;
                    }

                    nameEditText.setText(loginIdentity.getLoginName());
                    if (TextUtils.isEmpty(loginIdentity.getPwd())) {
                        logger.d("LoginActivity#pwd is empty");
                        break;
                    }
                    pwdEditText.setText(loginIdentity.getPwd());

                    if (!autoLogin) {
                        break;
                    }

                    autoLogin(loginIdentity);
                    return;
                } while (false);

                manualLogin();
            } catch (Exception e) {
                // 任何未知的异常
                logger.w("LoginActivity#auto/manual login failed: %s", e.getMessage());
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
                if (imService == null || imService.getIMLoginManager() == null) {
                    Toast.makeText(LoginActivity.this, getString(R.string.login_failed), Toast.LENGTH_SHORT).show();
                    showLoginPage();
                }
                imService.getIMLoginManager().login(loginIdentity);
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

        inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        SystemConfigSp.instance().init(getApplicationContext());

        imServiceConnector.connect(LoginActivity.this);
        EventBus.getDefault().register(this);

        setContentView(R.layout.login_activity);

        nameEditText = findViewById(R.id.name);
        pwdEditText = findViewById(R.id.password);
        pwdEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        findViewById(R.id.login_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputMethodManager.hideSoftInputFromWindow(pwdEditText.getWindowToken(), 0);
                attemptLogin();
            }
        });

        loginStatusView = findViewById(R.id.login_status);

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
                v.performClick();
                if (pwdEditText != null) {
                    inputMethodManager.hideSoftInputFromWindow(pwdEditText.getWindowToken(), 0);
                }

                if (nameEditText != null) {
                    inputMethodManager.hideSoftInputFromWindow(nameEditText.getWindowToken(), 0);
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
        imServiceConnector.disconnect(LoginActivity.this);
        EventBus.getDefault().unregister(this);
        splashPage = null;
        loginPage = null;
        super.onDestroy();
    }

    public void attemptLogin() {
        String loginName = nameEditText.getText().toString();
        String mPassword = pwdEditText.getText().toString();
        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(mPassword)) {
            Toast.makeText(this, getString(R.string.error_pwd_required), Toast.LENGTH_SHORT).show();
            focusView = pwdEditText;
            cancel = true;
        }

        if (TextUtils.isEmpty(loginName)) {
            Toast.makeText(this, getString(R.string.error_name_required), Toast.LENGTH_SHORT).show();
            focusView = nameEditText;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            loginStatusView.setVisibility(View.VISIBLE);
            if (imService != null) {
                loginName = loginName.trim();
                mPassword = mPassword.trim();
                imService.getIMLoginManager().login(loginName, mPassword);
            }
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

    public void onEventMainThread(LoginStatus event) {
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
        if (event == SocketEvent.CONNECT_MSG_SERVER_FAILED) {
            if (!loginSuccess) {
                onSocketFailure(event);
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

    private void onLoginFailure(LoginStatus event) {
        logger.e("LoginActivity#onLoginFailure errorCode:%s", event.name());
        showLoginPage();
        String errorTip = getString(IMUIHelper.getLoginErrorTip(event));
        logger.d("LoginActivity#onLoginFailure errorTip:%s", errorTip);
        loginStatusView.setVisibility(View.GONE);
        Toast.makeText(this, errorTip, Toast.LENGTH_SHORT).show();
    }

    private void onSocketFailure(SocketEvent event) {
        logger.e("LoginActivity#onSocketFailure:%s", event.name());
        showLoginPage();
        String errorTip = getString(IMUIHelper.getSocketErrorTip(event));
        logger.d("LoginActivity#onSocketFailure errorTip:%s", errorTip);
        loginStatusView.setVisibility(View.GONE);
        Toast.makeText(this, errorTip, Toast.LENGTH_SHORT).show();
    }
}
