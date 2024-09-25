package com.lsx.bigtalk.ui.fragment;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.lsx.bigtalk.AppConstant;
import com.lsx.bigtalk.storage.db.entity.PeerEntity;

import com.lsx.bigtalk.storage.db.entity.GroupEntity;
import com.lsx.bigtalk.storage.db.entity.UserEntity;
import com.lsx.bigtalk.R;

import com.lsx.bigtalk.ui.adapter.GroupManageAdapter;
//import com.lsx.bigtalk.ui.helper.CheckboxConfigHelper;
import com.lsx.bigtalk.service.event.GroupEvent;
import com.lsx.bigtalk.service.service.IMService;
import com.lsx.bigtalk.ui.base.BTBaseFragment;
import com.lsx.bigtalk.service.support.IMServiceConnector;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;


/**
 * @YM
 * 个人与群组的聊天详情都会来到这个页面
 * single: 这有sessionId的头像，以及加号"+" ， 创建群成功之后，跳到聊天的页面
 * group:  群成员，加减号 ， 修改成功之后，跳到群管理页面
 * 临时群任何人都可以加人，但是只有群主可以踢人”这个逻辑修改下，正式群暂时只给createId开放
 */
public class GroupManagementFragment extends BTBaseFragment {
    private View curView = null;
    /**adapter配置*/
    private GridView gridView;
    private GroupManageAdapter adapter;


    /**详情的配置  勿扰以及指定聊天*/
//    CheckboxConfigHelper checkBoxConfiger = new CheckboxConfigHelper();
    CheckBox noDisturbCheckbox;
    CheckBox topSessionCheckBox;

    /**需要的状态参数*/
    private IMService imService;
    private String curSessionKey;
    private PeerEntity peerEntity;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imServiceConnector.connect(getActivity());
        EventBus.getDefault().register(this);
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (null != curView) {
            ((ViewGroup) curView.getParent()).removeView(curView);
            return curView;
        }
        curView = inflater.inflate(R.layout.group_management_fragment, baseFragmentLayout);
        noDisturbCheckbox = curView.findViewById(R.id.notification_no_disturb_switch);
        topSessionCheckBox = curView.findViewById(R.id.NotificationTopMessageCheckbox);
        initRes();
        return curView;
    }

    private void initRes() {
        // 设置标题栏
        setTopLeftBtnImage(R.drawable.ic_back);
        setTopLeftText(getActivity().getString(R.string.top_left_back));
        topLeftContainerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    /**
     * Called when the fragment is no longer in use.  This is called
     * after {@link #onStop()} and before {@link #onDetach()}.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        imServiceConnector.disconnect(getActivity());
    }

    @Override
    protected void initHandler() {
    }


    private final IMServiceConnector imServiceConnector = new IMServiceConnector(){
        @Override
        public void onServiceDisconnected() {
        }

        @Override
        public void onIMServiceConnected() {
            logger.d("groupmgr#onIMServiceConnected");
            imService = imServiceConnector.getIMService();
            if(imService == null){
              Toast.makeText(GroupManagementFragment.this.getActivity(),
                        getResources().getString(R.string.im_service_disconnected), Toast.LENGTH_SHORT).show();
               return;
            }
            initView();
            initAdapter();
        }
    };


    private void initView() {
        setTopCenterTitleText(getString(R.string.chat_detail));
        if (null == imService || null == curView ) {
            logger.e("groupmgr#init failed,cause by imService or curView is null");
            return;
        }

        curSessionKey =  getActivity().getIntent().getStringExtra(AppConstant.IntentConstant.KEY_SESSION_KEY);
        if (TextUtils.isEmpty(curSessionKey)) {
            logger.e("groupmgr#getSessionInfoFromIntent failed");
            return;
        }
        peerEntity = imService.getIMSessionManager().findPeerEntity(curSessionKey);
        if(peerEntity == null){
            logger.e("groupmgr#findPeerEntity failed,sessionKey:%s",curSessionKey);
            return;
        }
        switch (peerEntity.getType()){
            case AppConstant.DBConstant.SESSION_TYPE_GROUP:{
                GroupEntity groupEntity = (GroupEntity) peerEntity;
                // 群组名称的展示
                TextView groupNameView = curView.findViewById(R.id.group_name_value);
                groupNameView.setText(groupEntity.getMainName());
            }break;

            case AppConstant.DBConstant.SESSION_TYPE_SINGLE:{
                // 个人不显示群聊名称
                View groupNameContainerView = curView.findViewById(R.id.group_name);
                groupNameContainerView.setVisibility(View.GONE);
            }break;
        }
    }

    private void initAdapter(){
        logger.d("groupmgr#initAdapter");

        gridView = curView.findViewById(R.id.group_management_gridview);
        gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));// 去掉点击时的黄色背影
        gridView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), true, true));

        adapter = new GroupManageAdapter(getActivity(),imService,peerEntity);
        gridView.setAdapter(adapter);
    }

    /**事件驱动通知*/
    public void onEventMainThread(GroupEvent event){
        switch (event.getEvent()){

            case CHANGE_GROUP_MEMBER_FAIL:
            case CHANGE_GROUP_MEMBER_TIMEOUT:{
                Toast.makeText(getActivity(), getString(R.string.change_temp_group_failed), Toast.LENGTH_SHORT).show();
                return;
            }
            case CHANGE_GROUP_MEMBER_SUCCESS:{
                onMemberChangeSuccess(event);
            }break;
        }
    }

    private void onMemberChangeSuccess(GroupEvent event){
        int groupId = event.getGroupEntity().getPeerId();
        if(groupId != peerEntity.getPeerId()){
            return;
        }
        List<Integer> changeList = event.getChangeList();
        if(changeList == null || changeList.size()<=0){
            return;
        }
        int changeType = event.getChangeType();

        switch (changeType){
            case AppConstant.DBConstant.GROUP_MODIFY_TYPE_ADD:
                ArrayList<UserEntity> newList = new ArrayList<>();
                for(Integer userId:changeList){
                    UserEntity userEntity =  imService.getIMContactManager().findContact(userId);
                    if(userEntity!=null) {
                        newList.add(userEntity);
                    }
                }
                adapter.add(newList);
                break;
            case AppConstant.DBConstant.GROUP_MODIFY_TYPE_DEL:
                for(Integer userId:changeList){
                    adapter.removeById(userId);
                }
                break;
        }
    }
}
