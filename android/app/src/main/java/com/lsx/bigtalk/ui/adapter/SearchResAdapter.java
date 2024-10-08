package com.lsx.bigtalk.ui.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.lsx.bigtalk.AppConstant;
import com.lsx.bigtalk.storage.db.entity.DepartmentEntity;
import com.lsx.bigtalk.storage.db.entity.GroupEntity;
import com.lsx.bigtalk.storage.db.entity.UserEntity;
import com.lsx.bigtalk.R;


import com.lsx.bigtalk.service.service.IMService;
import com.lsx.bigtalk.ui.activity.MainActivity;
import com.lsx.bigtalk.ui.widget.IMBaseImageView;
import com.lsx.bigtalk.ui.widget.IMGroupAvatar;
import com.lsx.bigtalk.ui.helper.IMUIHelper;
import com.lsx.bigtalk.logs.Logger;
import com.lsx.bigtalk.utils.ScreenUtil;


public class SearchResAdapter extends BaseAdapter implements
        AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener {

    private final Logger logger = Logger.getLogger(SearchResAdapter.class);
    private final Context ctx;
    private final IMService imService;
    private List<UserEntity> userList = new ArrayList<>();
    private List<DepartmentEntity> deptList = new ArrayList<>();
    private List<GroupEntity> groupList = new ArrayList<>();
    private String searchKey;

    public SearchResAdapter(Context context, IMService imService) {
        this.ctx = context;
        this.imService = imService;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        Object object = getItem(position);
        if (object instanceof UserEntity) {
            UserEntity userEntity = (UserEntity) object;
            IMUIHelper.handleMsgContactItemLongPressed(userEntity, ctx);
        }
        return true;
    }

    @Override
    public int getItemViewType(int position) {
        int userSize = userList == null ? 0 : userList.size();
        int groupSize = groupList == null ? 0 : groupList.size();

        if (position < userSize) {
            return SearchType.USER.ordinal();
        } else if (position < userSize + groupSize) {
            return SearchType.GROUP.ordinal();
        } else {
            return SearchType.DEPT.ordinal();
        }
    }

    @Override
    public int getViewTypeCount() {
        return SearchType.values().length;
    }

    @Override
    public int getCount() {
        int groupSize = groupList == null ? 0 : groupList.size();
        int userSize = userList == null ? 0 : userList.size();
        int deptSize = deptList == null ? 0 : deptList.size();
        return groupSize + userSize + deptSize;
    }

    @Override
    public Object getItem(int position) {
        int typeIndex = getItemViewType(position);
        SearchType renderType = SearchType.values()[typeIndex];
        switch (renderType) {
            case USER: {
                return userList.get(position);
            }
            case GROUP: {
                int userSize = userList == null ? 0 : userList.size();
                int realIndex = position - userSize;
                if (realIndex < 0) {
                    throw new IllegalArgumentException("SearchAdapter#getItem#group类型判断错误");
                }
                return groupList.get(realIndex);
            }
            case DEPT: {
                int userSize = userList == null ? 0 : userList.size();
                int groupSize = groupList == null ? 0 : groupList.size();

                int realIndex = position - userSize - groupSize;
                if (realIndex < 0) {
                    throw new IllegalArgumentException("SearchAdapter#getItem#group类型判断错误");
                }
                return deptList.get(realIndex);
            }
            default:
                throw new IllegalArgumentException("SearchAdapter#getItem#不存在的类型" + renderType.name());
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int typeIndex = getItemViewType(position);
        SearchType renderType = SearchType.values()[typeIndex];
        View view = null;
        switch (renderType) {
            case USER: {
                view = renderUser(position, convertView, parent);
            }
            break;
            case GROUP: {
                view = renderGroup(position, convertView, parent);
            }
            break;
            case DEPT: {
                view = renderDept(position, convertView, parent);
            }
            break;
        }
        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Object object = getItem(position);
        if (object instanceof UserEntity) {
            UserEntity userEntity = (UserEntity) object;
            IMUIHelper.openChatActivity(ctx, userEntity.getSessionKey());
        } else if (object instanceof GroupEntity) {
            GroupEntity groupEntity = (GroupEntity) object;
            IMUIHelper.openChatActivity(ctx, groupEntity.getSessionKey());
        } else if (object instanceof DepartmentEntity) {
            DepartmentEntity department = (DepartmentEntity) object;
            locateDepartment(ctx, department);
        }
    }

    public void clear() {
        this.userList.clear();
        this.groupList.clear();
        this.deptList.clear();
        notifyDataSetChanged();
    }

    public void putUserList(List<UserEntity> userList) {
        this.userList.clear();
        if (userList == null || userList.isEmpty()) {
            return;
        }
        this.userList = userList;
    }

    public void putGroupList(List<GroupEntity> groupList) {
        this.groupList.clear();
        if (groupList == null || groupList.isEmpty()) {
            return;
        }
        this.groupList = groupList;
    }

    public void putDeptList(List<DepartmentEntity> deptList) {
        this.deptList.clear();
        if (deptList == null || deptList.isEmpty()) {
            return;
        }
        this.deptList = deptList;
    }

    private void locateDepartment(Context ctx, DepartmentEntity department) {
        Intent intent = new Intent(ctx, MainActivity.class);
        intent.putExtra(AppConstant.IntentConstant.KEY_LOCATE_DEPARTMENT, department.getDepartId());
        ctx.startActivity(intent);
    }

    public View renderUser(int position, View view, ViewGroup parent) {
        UserHolder userHolder;
        UserEntity userEntity = (UserEntity) getItem(position);
        if (userEntity == null) {
            return null;
        }
        if (view == null) {
            view = LayoutInflater.from(ctx).inflate(R.layout.contact_list_item_view, parent, false);
            userHolder = new UserHolder();
            userHolder.nameView = view.findViewById(R.id.contact_nickname_title);
            userHolder.realNameView = view.findViewById(R.id.contact_real_name_title);
            userHolder.sectionView = view.findViewById(R.id.contact_category_title);
            userHolder.avatar = view.findViewById(R.id.contact_avatar);
            userHolder.divider = view.findViewById(R.id.contact_divider);
            view.setTag(userHolder);
        } else {
            userHolder = (UserHolder) view.getTag();
        }

        IMUIHelper.setTextHighlighted(userHolder.nameView, userEntity.getMainName(), userEntity.getSearchElement());

        userHolder.avatar.setImageResource(R.drawable.image_default_user_avatar);
        userHolder.divider.setVisibility(View.VISIBLE);

        if (position == 0) {
            userHolder.sectionView.setVisibility(View.VISIBLE);
            userHolder.sectionView.setText(ctx.getString(R.string.contact));
            userHolder.divider.setVisibility(View.GONE);
        } else {
            userHolder.sectionView.setVisibility(View.GONE);
            userHolder.divider.setVisibility(View.VISIBLE);
        }

        userHolder.avatar.setDefaultImageRes(R.drawable.image_default_user_avatar);
        userHolder.avatar.setCorner(0);
        userHolder.avatar.setAvatarAppend(AppConstant.SysConstant.AVATAR_APPEND_100);
        userHolder.avatar.setImageUrl(userEntity.getAvatar());

        userHolder.realNameView.setText(userEntity.getRealName());
        userHolder.realNameView.setVisibility(View.GONE);
        return view;
    }

    public View renderGroup(int position, View view, ViewGroup parent) {
        GroupHolder groupHolder;
        GroupEntity groupEntity = (GroupEntity) getItem(position);
        if (groupEntity == null) {
            return null;
        }
        if (view == null) {
            groupHolder = new GroupHolder();
            view = LayoutInflater.from(ctx).inflate(R.layout.group_contact_item_view, parent, false);
            groupHolder.nameView = view.findViewById(R.id.contact_nickname_title);
            groupHolder.sectionView = view.findViewById(R.id.contact_category_title);
            groupHolder.avatar = view.findViewById(R.id.contact_avatar);
            groupHolder.divider = view.findViewById(R.id.contact_divider);
            view.setTag(groupHolder);
        } else {
            groupHolder = (GroupHolder) view.getTag();
        }

        IMUIHelper.setTextHighlighted(groupHolder.nameView, groupEntity.getMainName(), groupEntity.getSearchElement());

        groupHolder.sectionView.setVisibility(View.GONE);
        groupHolder.divider.setVisibility(View.VISIBLE);

        int userSize = userList == null ? 0 : userList.size();
        if (position == userSize) {
            groupHolder.sectionView.setVisibility(View.VISIBLE);
            groupHolder.sectionView.setText(ctx.getString(R.string.fixed_group_or_temp_group));
            groupHolder.divider.setVisibility(View.GONE);
        } else {
            groupHolder.sectionView.setVisibility(View.GONE);
        }

        groupHolder.avatar.setVisibility(View.VISIBLE);
        List<String> avatarUrlList = new ArrayList<>();
        Set<Integer> userIds = groupEntity.getlistGroupMemberIds();
        int i = 0;
        for (Integer buddyId : userIds) {
            UserEntity entity = imService.getIMContactManager().findContact(buddyId);
            if (entity == null) {
                continue;
            }
            avatarUrlList.add(entity.getAvatar());
            if (i >= 3) {
                break;
            }
            i++;
        }
        setGroupAvatar(groupHolder.avatar, avatarUrlList);
        return view;
    }

    public View renderDept(int position, View view, ViewGroup parent) {
        DeptHolder deptHolder;
        DepartmentEntity deptEntity = (DepartmentEntity) getItem(position);
        if (deptEntity == null) {
            return null;
        }
        if (view == null) {
            deptHolder = new DeptHolder();
            view = LayoutInflater.from(ctx).inflate(R.layout.contact_list_item_view, parent, false);
            deptHolder.avatar = view.findViewById(R.id.contact_avatar);
            deptHolder.nameView = view.findViewById(R.id.contact_nickname_title);
            deptHolder.sectionView = view.findViewById(R.id.contact_category_title);
            deptHolder.divider = view.findViewById(R.id.contact_divider);
            view.setTag(deptHolder);
        } else {
            deptHolder = (DeptHolder) view.getTag();
        }
        deptHolder.avatar.setVisibility(View.INVISIBLE);
        IMUIHelper.setTextHighlighted(deptHolder.nameView, deptEntity.getDepartName(), deptEntity.getSearchElement());
        deptHolder.divider.setVisibility(View.VISIBLE);

        int groupSize = groupList == null ? 0 : groupList.size();
        int userSize = userList == null ? 0 : userList.size();
        int realIndex = position - groupSize - userSize;
        if (realIndex == 0) {
            deptHolder.sectionView.setVisibility(View.VISIBLE);
            deptHolder.sectionView.setText(ctx.getString(R.string.department));
            deptHolder.divider.setVisibility(View.GONE);
        } else {
            deptHolder.sectionView.setVisibility(View.GONE);
        }
        return view;
    }

    private void setGroupAvatar(IMGroupAvatar avatar, List<String> avatarUrlList) {
        try {
            avatar.setViewSize(ScreenUtil.instance(ctx).dip2px(38));
            avatar.setChildCorner(2);
            avatar.setAvatarUrlAppend(AppConstant.SysConstant.AVATAR_APPEND_32);
            avatar.setParentPadding(3);
            avatar.setAvatarUrls((ArrayList<String>) avatarUrlList);
        } catch (Exception e) {
            logger.e(e.toString());
        }
    }

    public void setSearchKey(String searchKey) {
        this.searchKey = searchKey;
    }

    public String getSearchKey() {
        return searchKey;
    }

    private enum SearchType {
        USER,
        GROUP,
        DEPT,
        ILLEGAL
    }

    public static class UserHolder {
        View divider;
        TextView sectionView;
        TextView nameView;
        TextView realNameView;
        IMBaseImageView avatar;
    }

    public static class DeptHolder {
        View divider;
        TextView sectionView;
        TextView nameView;
        IMBaseImageView avatar;
    }

    public static class GroupHolder {
        View divider;
        TextView sectionView;
        TextView nameView;
        IMGroupAvatar avatar;
    }
}
