package com.lsx.bigtalk.ui.adapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.lsx.bigtalk.DB.entity.UserEntity;
import com.lsx.bigtalk.R;
import com.lsx.bigtalk.config.SysConstant;
import com.lsx.bigtalk.ui.widget.IMBaseImageView;
import com.lsx.bigtalk.helper.IMUIHelper;
import com.lsx.bigtalk.utils.Logger;


public class GroupMemberSelectAdapter extends BaseAdapter implements
        SectionIndexer,
        AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener {
    private final Logger logger = Logger.getLogger(GroupMemberSelectAdapter.class);
    private List<UserEntity> allUserList = new ArrayList<>();
    private List<UserEntity> backupList = new ArrayList<>();
    private Set<Integer> alreadyListSet = new HashSet<>();
    private final Set<Integer> checkListSet= new HashSet<>();
    private boolean isSearchMode = false;
    private final Context ctx;

    public GroupMemberSelectAdapter(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    public Object[] getSections() {
        return new Object[0];
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        int index = 0;
        for (UserEntity entity : allUserList){
            int firstCharacter = entity.getSectionName().charAt(0);
            if (firstCharacter == sectionIndex) {
                return index;
            }
            index++;
        }
        return -1;
    }


    @Override
    public int getSectionForPosition(int position) {
        return 0;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        UserEntity contact = (UserEntity) getItem(position);
        UserHolder viewHolder = (UserHolder) view.getTag();

        if (viewHolder == null || alreadyListSet.contains(contact.getPeerId())) {
            return;
        }
        viewHolder.checkBox.toggle();
        boolean checked = viewHolder.checkBox.isChecked();
        int userId = contact.getPeerId();
        if (checked) {
            checkListSet.add(userId);
        } else {
            checkListSet.remove(userId);
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        UserEntity contact = (UserEntity) getItem(position);
        IMUIHelper.handleMsgContactItemLongPressed(contact, ctx);
        return true;
    }

    @Override
    public int getCount() {
        return allUserList == null ? 0 : allUserList.size();
    }

    @Override
    public Object getItem(int position) {
        return allUserList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        UserEntity userEntity = (UserEntity) getItem(position);
        if (userEntity == null) {
            return null;
        }

        UserHolder userHolder;
        if (view == null) {
            view = LayoutInflater.from(ctx).inflate(R.layout.item_contact, parent,false);
            userHolder = new UserHolder();
            userHolder.nameView = view.findViewById(R.id.contact_item_title);
            userHolder.realNameView = view.findViewById(R.id.contact_realname_title);
            userHolder.sectionView = view.findViewById(R.id.contact_category_title);
            userHolder.avatar = view.findViewById(R.id.contact_portrait);
            userHolder.checkBox = view.findViewById(R.id.checkBox);
            userHolder.divider = view.findViewById(R.id.contact_divider);
            view.setTag(userHolder);
        } else {
            userHolder = (UserHolder) view.getTag();
        }

        userHolder.checkBox.setVisibility(View.VISIBLE);
        if (isSearchMode) {
            IMUIHelper.setTextHighlighted(userHolder.nameView, userEntity.getMainName(),
                    userEntity.getSearchElement());
        } else {
            userHolder.nameView.setText(userEntity.getMainName());
        }

        userHolder.avatar.setImageResource(R.drawable.default_user_avatar);
        userHolder.divider.setVisibility(View.VISIBLE);

        if (!isSearchMode) {
            String sectionName = userEntity.getSectionName();
            String preSectionName = null;
            if (position > 0) {
                preSectionName = ((UserEntity) getItem(position - 1)).getSectionName();
            }
            if (TextUtils.isEmpty(preSectionName) || !preSectionName.equals(sectionName)) {
                userHolder.sectionView.setVisibility(View.VISIBLE);
                userHolder.sectionView.setText(sectionName);
                userHolder.divider.setVisibility(View.GONE);
            } else {
                userHolder.sectionView.setVisibility(View.GONE);
            }
        } else {
            userHolder.sectionView.setVisibility(View.GONE);
        }

        boolean checked = checkListSet.contains(userEntity.getPeerId());
        userHolder.checkBox.setChecked(checked);
        boolean disable = alreadyListSet.contains(userEntity.getPeerId());
        userHolder.checkBox.setEnabled(!disable);

        userHolder.avatar.setDefaultImageRes(R.drawable.default_user_avatar);
        userHolder.avatar.setCorner(0);
        userHolder.avatar.setAvatarAppend(SysConstant.AVATAR_APPEND_100);
        userHolder.avatar.setImageUrl(userEntity.getAvatar());

        userHolder.realNameView.setText(userEntity.getRealName());
        userHolder.realNameView.setVisibility(View.GONE);
        return view;
    }

    public void setAllUserList(List<UserEntity> allUserList) {
        this.allUserList = allUserList;
        this.backupList  = allUserList;
    }

    public void recover(){
        isSearchMode = false;
        allUserList = backupList;
        notifyDataSetChanged();
    }

    public void onSearch(String key){
        isSearchMode = true;
        //allUserList.clear();
        List<UserEntity> searchList = new ArrayList<>();
        for(UserEntity entity:backupList){
            if(IMUIHelper.handleContactSearch(key,entity)){
                searchList.add(entity);
            }
        }
        allUserList = searchList;
        notifyDataSetChanged();
    }


    public static class UserHolder {
        View divider;
        TextView sectionView;
        TextView nameView;
        TextView realNameView;
        IMBaseImageView avatar;
        CheckBox checkBox;
    }


    /**------------------set/get------------------*/

    public Set<Integer> getAlreadyListSet() {
        return alreadyListSet;
    }

    public void setAlreadyListSet(Set<Integer> alreadyListSet) {
        this.alreadyListSet = alreadyListSet;
    }

    public Set<Integer> getCheckListSet() {
        return checkListSet;
    }
}
