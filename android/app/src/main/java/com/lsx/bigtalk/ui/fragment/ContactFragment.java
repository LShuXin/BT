package com.lsx.bigtalk.ui.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.lsx.bigtalk.DB.entity.DepartmentEntity;
import com.lsx.bigtalk.DB.entity.GroupEntity;
import com.lsx.bigtalk.DB.entity.UserEntity;
import com.lsx.bigtalk.R;
import com.lsx.bigtalk.config.HandlerConstant;
import com.lsx.bigtalk.imservice.event.GroupEvent;
import com.lsx.bigtalk.imservice.event.UserInfoEvent;
import com.lsx.bigtalk.imservice.support.IMServiceConnector;
import com.lsx.bigtalk.imservice.manager.IMContactManager;
import com.lsx.bigtalk.imservice.service.IMService;
import com.lsx.bigtalk.ui.adapter.ContactAdapter;
import com.lsx.bigtalk.ui.adapter.DeptAdapter;
import com.lsx.bigtalk.ui.widget.SortSideBar;
import com.lsx.bigtalk.ui.widget.SortSideBar.OnTouchingLetterChangedListener;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

import java.util.List;

import de.greenrobot.event.EventBus;


public class ContactFragment extends MainFragment implements OnTouchingLetterChangedListener {
    private View curView = null;
    private static Handler uiHandler = null;
    private ListView allContactListView;
    private ListView departmentContactListView;

    private ContactAdapter contactAdapter;
    private DeptAdapter departmentAdapter;

    private IMService imService;
    private IMContactManager contactMgr;
    private int curTabIndex = 0;

    private final IMServiceConnector imServiceConnector = new IMServiceConnector() {
        @Override
        public void onIMServiceConnected() {
            logger.d("ContactFragment#onIMServiceConnected");

            imService = imServiceConnector.getIMService();
            if (imService == null) {
                logger.e("ContactFragment#onIMServiceConnected# imservice is null!!");
                return;
            }
            contactMgr = imService.getIMContactManager();

            // 初始化视图
            initAdapter();
            renderEntityList();
            EventBus.getDefault().registerSticky(ContactFragment.this);
        }

        @Override
        public void onServiceDisconnected() {
            if (EventBus.getDefault().isRegistered(ContactFragment.this)) {
                EventBus.getDefault().unregister(ContactFragment.this);
            }
        }
    };
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imServiceConnector.connect(getActivity());
        initHandler();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(ContactFragment.this)) {
            EventBus.getDefault().unregister(ContactFragment.this);
        }
        imServiceConnector.disconnect(getActivity());
    }

    @Override
    protected void initHandler() {
        uiHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                if (msg.what == HandlerConstant.CONTACT_TAB_CHANGED) {
                    if (null != msg.obj) {
                        curTabIndex = (Integer) msg.obj;
                        if (0 == curTabIndex) {
                            allContactListView.setVisibility(View.VISIBLE);
                            departmentContactListView.setVisibility(View.GONE);
                        } else {
                            departmentContactListView.setVisibility(View.VISIBLE);
                            allContactListView.setVisibility(View.GONE);
                        }
                    }
                }
            }
        };
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (null != curView) {
            ((ViewGroup) curView.getParent()).removeView(curView);
            return curView;
        }
        curView = inflater.inflate(R.layout.contact_fragment, baseFragmentLayout);
        initView();
        return curView;
    }
    
    private void initView() {
        showTopTabButtonGroup();
        hideAppBar();

        super.init(curView);
        showProgressBar();

        SortSideBar sortSideBar = curView.findViewById(R.id.sidrbar);
        TextView dialog = curView.findViewById(R.id.dialog);
        sortSideBar.setTextView(dialog);
        sortSideBar.setOnTouchingLetterChangedListener(this);

        allContactListView = curView.findViewById(R.id.all_contact_list);
        departmentContactListView = curView.findViewById(R.id.department_contact_list);

        //this is critical, disable loading when finger sliding, otherwise you'll find sliding is not very smooth
        allContactListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), true, true));
        departmentContactListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), true, true));
        // todo eric
        // showLoadingProgressBar(true);
    }

    private void initAdapter() {
        contactAdapter = new ContactAdapter(getActivity(), imService);
        departmentAdapter = new DeptAdapter(getActivity(), imService);
        allContactListView.setAdapter(contactAdapter);
        departmentContactListView.setAdapter(departmentAdapter);

        // 单击视图事件
        allContactListView.setOnItemClickListener(contactAdapter);
        allContactListView.setOnItemLongClickListener(contactAdapter);

        departmentContactListView.setOnItemClickListener(departmentAdapter);
        departmentContactListView.setOnItemLongClickListener(departmentAdapter);
    }

    public void locateDepartment(int departmentId) {
        logger.d("ContactFragment#locateDepartment id:%s", departmentId);

        if (topTabButtonGroup == null) {
            logger.e("department#TopTabButton is null");
            return;
        }
        Button tabDepartmentBtn = topTabButtonGroup.getTabDepartmentBtn();
        if (tabDepartmentBtn == null) {
            return;
        }
        tabDepartmentBtn.performClick();
        locateDepartmentImpl(departmentId);
    }

    private void locateDepartmentImpl(int departmentId) {
        if (imService == null) {
            return;
        }
        DepartmentEntity department = imService.getIMContactManager().findDepartment(departmentId);
        if (department == null) {
            logger.e("ContactFragment#no such id:%s", departmentId);
            return;
        }

        logger.d("department#go to locate department:%s", department);
        final int position = departmentAdapter.locateDepartment(department.getDepartName());
        logger.d("department#located position:%d", position);

        if (position < 0) {
            logger.i("department#locateDepartment id:%s failed", departmentId);
            return;
        }
        //the first time locate works
        //from the second time, the locating operations fail ever since
        departmentContactListView.post(new Runnable() {

            @Override
            public void run() {
                departmentContactListView.setSelection(position);
            }
        });
    }

    private void renderEntityList() {
        hideProgressBar();
        logger.d("contact#renderEntityList");

        if (contactMgr.getIsContactDataReady()) {
            renderUserList();
            renderDeptList();
        }
        if (imService.getIMGroupManager().isGroupDataReady()) {
            renderGroupList();
        }
        showTopSearchBarFrameLayout();
    }


    private void renderDeptList() {
        List<UserEntity> departmentList = contactMgr.getDepartmentTabSortedList();
        departmentAdapter.putUserList(departmentList);
    }

    private void renderUserList() {
        List<UserEntity> contactList = contactMgr.getSortedContactList();
        // 没有任何的联系人数据
        if (contactList.isEmpty()) {
            return;
        }
        contactAdapter.putUserList(contactList);
    }

    private void renderGroupList() {
        logger.d("ContactFragment#renderGroupList");
        List<GroupEntity> originList = imService.getIMGroupManager().getNormalGroupSortedList();
        if (originList.isEmpty()) {
            return;
        }
        contactAdapter.putGroupList(originList);
    }

    private ListView getCurListView() {
        if (0 == curTabIndex) {
            return allContactListView;
        } else {
            return departmentContactListView;
        }
    }

    @Override
    public void onTouchingLetterChanged(String s) {
        int position = -1;
        if (0 == curTabIndex) {
            position = contactAdapter.getPositionForSection(s.charAt(0));
        } else {
            position = departmentAdapter.getPositionForSection(s.charAt(0));
        }
        if (position != -1) {
            getCurListView().setSelection(position);
        }
    }

    public static Handler getHandler() {
        return uiHandler;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void onEventMainThread(GroupEvent event) {
        switch (event.getEvent()) {
            case GROUP_INFO_UPDATED:
            case GROUP_INFO_OK:
                renderGroupList();
                searchDataReady();
                break;
        }
    }

    public void onEventMainThread(UserInfoEvent event) {
        switch (event) {
            case USER_INFO_UPDATE:
            case USER_INFO_OK:
                renderDeptList();
                renderUserList();
                searchDataReady();
                break;
        }
    }

    public void searchDataReady() {
        if (imService.getIMContactManager().getIsContactDataReady() &&
                imService.getIMGroupManager().isGroupDataReady()) {
            showTopSearchBarFrameLayout();
        }
    }
}
