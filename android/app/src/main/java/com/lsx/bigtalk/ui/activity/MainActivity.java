package com.lsx.bigtalk.ui.activity;

import java.util.Objects;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Window;

import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import de.greenrobot.event.EventBus;

import com.lsx.bigtalk.AppConstant;
import com.lsx.bigtalk.R;
import com.lsx.bigtalk.service.event.LoginEvent;
import com.lsx.bigtalk.service.event.UnreadEvent;
import com.lsx.bigtalk.service.service.IMService;
import com.lsx.bigtalk.service.support.IMServiceConnector;
import com.lsx.bigtalk.ui.fragment.SessionFragment;
import com.lsx.bigtalk.ui.fragment.ContactFragment;
import com.lsx.bigtalk.ui.widget.NaviTabButton;
import com.lsx.bigtalk.logs.Logger;


public class MainActivity extends FragmentActivity {
    private Fragment[] mFragments;
    private NaviTabButton[] mTabButtons;
    private final Logger logger = Logger.getLogger(MainActivity.class);
    private IMService imService;
    private final IMServiceConnector imServiceConnector = new IMServiceConnector() {
        @Override
        public void onIMServiceConnected() {
            imService = imServiceConnector.getIMService();
        }

        @Override
        public void onServiceDisconnected() {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        logger.d("MainActivity#onCreate savedInstanceState: %s", savedInstanceState);
        if (null != savedInstanceState) {
            jumpToLoginPage();
            finish();
        }

        EventBus.getDefault().register(this);
        imServiceConnector.connect(this);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main_activity);

        initTab();
        initFragment();
        setFragmentIndicator(0);
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(Intent.ACTION_MAIN);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addCategory(Intent.CATEGORY_HOME);
        startActivity(i);
    }

    // onNewIntent 是在一个活动（Activity）已经存在并处于活动状态（非销毁状态）时，
    // 通过 startActivity 启动该活动的时候调用的。它允许你处理新的 Intent，通常用
    // 于更新活动的内容或执行其他相关操作
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleLocateDepartment(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        logger.d("MainActivity#onDestroy");
        EventBus.getDefault().unregister(this);
        imServiceConnector.disconnect(this);
        super.onDestroy();
    }

    private void initFragment() {
        mFragments = new Fragment[4];
        mFragments[0] = getSupportFragmentManager().findFragmentById(R.id.session_fragment);
        mFragments[1] = getSupportFragmentManager().findFragmentById(R.id.contact_fragment);
        mFragments[2] = getSupportFragmentManager().findFragmentById(R.id.finder_fragment);
        mFragments[3] = getSupportFragmentManager().findFragmentById(R.id.mine_fragment);
    }

    private void initTab() {
        mTabButtons = new NaviTabButton[4];

        mTabButtons[0] = findViewById(R.id.tab_btn_session);
        mTabButtons[1] = findViewById(R.id.tab_btn_contact);
        mTabButtons[2] = findViewById(R.id.tab_btn_finder);
        mTabButtons[3] = findViewById(R.id.tab_btn_mine);

        mTabButtons[0].setTitle(getString(R.string.main_chat));
        mTabButtons[0].setIndex(0);
        mTabButtons[0].setSelectedImage(mGetDrawable(R.drawable.ic_tab_item_session_active));
        mTabButtons[0].setUnselectedImage(mGetDrawable(R.drawable.ic_tab_item_session_inactive));

        mTabButtons[1].setTitle(getString(R.string.main_contact));
        mTabButtons[1].setIndex(1);
        mTabButtons[1].setSelectedImage(mGetDrawable(R.drawable.ic_tab_item_contact_active));
        mTabButtons[1].setUnselectedImage(mGetDrawable(R.drawable.ic_tab_item_contact_inactive));

        mTabButtons[2].setTitle(getString(R.string.main_inner_net));
        mTabButtons[2].setIndex(2);
        mTabButtons[2].setSelectedImage(mGetDrawable(R.drawable.ic_finder_active));
        mTabButtons[2].setUnselectedImage(mGetDrawable(R.drawable.ic_finder_inactive));

        mTabButtons[3].setTitle(getString(R.string.main_me_tab));
        mTabButtons[3].setIndex(3);
        mTabButtons[3].setSelectedImage(mGetDrawable(R.drawable.ic_tab_item_mine_active));
        mTabButtons[3].setUnselectedImage(mGetDrawable(R.drawable.ic_tab_item_mine_inactive));
    }

    public void setFragmentIndicator(int which) {
        getSupportFragmentManager()
                .beginTransaction()
                .hide(mFragments[0])
                .hide(mFragments[1])
                .hide(mFragments[2])
                .hide(mFragments[3])
                .show(mFragments[which])
                .commit();

        mTabButtons[0].setSelectedButton(false);
        mTabButtons[1].setSelectedButton(false);
        mTabButtons[2].setSelectedButton(false);
        mTabButtons[3].setSelectedButton(false);

        mTabButtons[which].setSelectedButton(true);
    }
    
    public void setUnreadMessageCnt(int unreadCnt) {
        mTabButtons[0].setUnreadNotify(unreadCnt);
    }
    
    public void handleSessionDoubleClick() {
        setFragmentIndicator(0);
        ((SessionFragment) mFragments[0]).scrollToUnreadPosition();
    }
    
    private void handleLocateDepartment(Intent intent) {
        int departmentIdToLocate = intent.getIntExtra(AppConstant.IntentConstant.KEY_LOCATE_DEPARTMENT, -1);
        if (-1 == departmentIdToLocate) {
            return;
        }

        logger.d("MainActivity#handleLocateDepartment, department to locate: %d", departmentIdToLocate);
        setFragmentIndicator(1);
        ContactFragment fragment = (ContactFragment) mFragments[1];
        if (null == fragment) {
            logger.e("MainActivity#handleLocateDepartment, department fragment is null");
            return;
        }
        fragment.locateDepartment(departmentIdToLocate);
    }

    public void onEventMainThread(UnreadEvent event) {
        switch (event.event) {
            case SESSION_UNREAD_MSG_READ:
            case UNREAD_MSG_LISTED:
            case UNREAD_MSG_RECEIVED:
                updateUnreadMessageCount();
                break;
        }
    }

    private void updateUnreadMessageCount() {
        if (null != imService) {
            int unReadNum = imService.getIMUnReadMsgManager().getTotalUnreadCount();
            mTabButtons[0].setUnreadNotify(unReadNum);
        }
    }

    public void onEventMainThread(LoginEvent event) {
        if (LoginEvent.LOGIN_OUT == Objects.requireNonNull(event)) {
            handleOnLogout();
        }
    }

    private void handleOnLogout() {
        logger.d("MainActivity#handleOnLogout");
        finish();
        jumpToLoginPage();
    }

    private void jumpToLoginPage() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra(AppConstant.IntentConstant.KEY_NOT_AUTO_LOGIN, true);
        startActivity(intent);
    }
    
    private Drawable mGetDrawable(int id) {
        return ResourcesCompat.getDrawable(getResources(), id, getTheme());
    }
}
