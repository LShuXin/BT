package com.lsx.bigtalk.ui.adapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lsx.bigtalk.DB.entity.GroupEntity;
import com.lsx.bigtalk.DB.entity.PeerEntity;
import com.lsx.bigtalk.DB.entity.UserEntity;
import com.lsx.bigtalk.R;
import com.lsx.bigtalk.config.DBConstant;
import com.lsx.bigtalk.config.SysConstant;
import com.lsx.bigtalk.imservice.manager.IMContactManager;
import com.lsx.bigtalk.imservice.service.IMService;
import com.lsx.bigtalk.ui.widget.IMBaseImageView;
import com.lsx.bigtalk.helper.IMUIHelper;
import com.lsx.bigtalk.utils.Logger;


public class GroupManageAdapter extends BaseAdapter {
	private final Logger logger = Logger.getLogger(GroupManageAdapter.class);
	private final Context context;
	private boolean isRemove = false;
    private boolean showMinusTag = false;
    private boolean showPlusTag = false;
	private final List<UserEntity> memberList = new ArrayList<>();
    private final IMService imService;
    private int groupCreatorId = -1;
    private final PeerEntity peerEntity;

	public GroupManageAdapter(Context c, IMService imService, PeerEntity peerEntity) {
        memberList.clear();
        this.context = c;
		this.imService = imService;
        this.peerEntity = peerEntity;
        setData();
	}

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        GroupHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.group_manage_grid_item, parent);

            holder = new GroupHolder();
            holder.imageView = convertView.findViewById(R.id.grid_item_image);
            holder.userTitle = convertView.findViewById(R.id.group_manager_user_title);
            holder.role = convertView.findViewById(R.id.grid_item_image_role);
            holder.deleteImg = convertView.findViewById(R.id.deleteLayout);
            holder.imageView.setDefaultImageRes(R.drawable.default_user_avatar);
            convertView.setTag(holder);
        } else {
            holder = (GroupHolder)convertView.getTag();
        }

        holder.role.setVisibility(View.GONE);
        if (position >= 0 && position < memberList.size()) {
            final UserEntity userEntity = memberList.get(position);
            setHolder(holder, position, userEntity.getAvatar(), 0, userEntity.getMainName(), userEntity);
            if (holder.imageView != null) {
                holder.imageView.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        IMUIHelper.openUserProfileActivity(context, userEntity.getPeerId());
                    }
                });
            }

            if (groupCreatorId > 0 && groupCreatorId == userEntity.getPeerId()) {
                holder.role.setVisibility(View.VISIBLE);
            }

            if (isRemove && userEntity.getPeerId() != groupCreatorId) {
                holder.deleteImg.setVisibility(View.VISIBLE);
            } else {
                holder.deleteImg.setVisibility(View.INVISIBLE);
            }

        } else if (position == memberList.size() && showPlusTag) {
            setHolder(holder, position, null, R.drawable.group_member_add, "", null);
            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    IMUIHelper.openGroupMemberSelectActivity(context, peerEntity.getSessionKey());
                }
            });
            holder.deleteImg.setVisibility(View.INVISIBLE);

        } else if (position == memberList.size() + 1 && showMinusTag) {
            setHolder(holder, position, null, R.drawable.group_member_delete, "", null);
            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    toggleDeleteIcon();
                }
            });
            holder.deleteImg.setVisibility(View.INVISIBLE);
        }
        return convertView;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        int memberListSize = memberList.size();
        if(showPlusTag){
            memberListSize = memberListSize +1;
        }
        // 现在的情况是有减 一定有加
        if(showMinusTag){
            memberListSize = memberListSize +1;
        }
        return memberListSize;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }


    //todo 在选择添加人页面，currentGroupEntity 的值没有设定
	public void setData() {
        int sessionType = peerEntity.getType();
        switch (sessionType){
            case DBConstant.SESSION_TYPE_GROUP:{
               GroupEntity groupEntity =  (GroupEntity)peerEntity;
               setGroupData(groupEntity);
            }break;
            case DBConstant.SESSION_TYPE_SINGLE:{
                setSingleData((UserEntity)peerEntity);
            }break;
        }
        notifyDataSetChanged();
	}

    private void setGroupData(GroupEntity entity){
        int loginId = imService.getIMLoginManager().getLoginId();
        int ownerId = entity.getCreatorId();
        IMContactManager manager = imService.getIMContactManager();
        for(Integer memId:entity.getlistGroupMemberIds()){
           UserEntity user =  manager.findContact(memId);
           if(user!=null){
               if(ownerId == user.getPeerId()){
                   // 群主放在第一个
                   groupCreatorId =ownerId;
                   memberList.add(0, user);
               }else {
                   memberList.add(user);
               }
           }
        }
        //按钮状态的判断
        switch (entity.getGroupType()){
            case DBConstant.GROUP_TYPE_TEMP:{
                if(loginId == entity.getCreatorId()){
                    showMinusTag = true;
                    showPlusTag = true;
                }else{
                    //展示 +
                    showPlusTag = true;
                }
            }
            break;
            case DBConstant.GROUP_TYPE_NORMAL:{
                if(loginId == entity.getCreatorId()){
                    // 展示加减
                    showMinusTag = true;
                    showPlusTag = true;
                }else{
                    // 什么也不展示
                }
            }
            break;
        }
    }

    private void setSingleData(UserEntity userEntity){
        if(userEntity != null){
            memberList.add(userEntity);
            showPlusTag = true;
        }
    }

	public void removeById(int contactId) {
        for (UserEntity contact : memberList) {
            if (contact.getPeerId() == contactId) {
                memberList.remove(contact);
                break;
            }
        }
        notifyDataSetChanged();
	}

	public void add(UserEntity contact) {
		isRemove = false;
		memberList.add(contact);
		notifyDataSetChanged();
	}

    public void add(List<UserEntity> list) {
        isRemove = false;
        // 群成员的展示没有去重，在收到IMGroupChangeMemberNotify 可能会造成重复数据
        for(UserEntity userEntity:list){
            if(!memberList.contains(userEntity)){
                memberList.add(userEntity);
            }
        }
        notifyDataSetChanged();
    }

	private void setHolder(final GroupHolder holder, int position,
			String avatarUrl, int avatarResourceId, String name,
			UserEntity contactEntity) {

		if (null != holder) {
			if (avatarUrl != null) {
                holder.imageView.setDefaultImageRes(R.drawable.default_user_avatar);
                holder.imageView.setCorner(8);
                holder.imageView.setAvatarAppend(SysConstant.AVATAR_APPEND_120);
                holder.imageView.setImageResource(R.drawable.default_user_avatar);
                holder.imageView.setImageUrl(avatarUrl);
            } else {
                holder.imageView.setImageId(0);
                holder.imageView.setImageId(avatarResourceId);
                holder.imageView.setImageUrl(null);
			}

			holder.contactEntity = contactEntity;
			if (contactEntity != null) {
				holder.deleteImg.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
                        if (holder.contactEntity == null) {
                            return;
                        }
                        int userId = holder.contactEntity.getPeerId();
                        removeById(userId);
                        Set<Integer> removeMemberlist = new HashSet<>(1);
                        removeMemberlist.add(userId);
                        imService.getIMGroupManager().reqRemoveGroupMember(peerEntity.getPeerId(), removeMemberlist);
					}
				});
			}

			holder.userTitle.setText(name);
			holder.imageView.setVisibility(View.VISIBLE);
			holder.userTitle.setVisibility(View.VISIBLE);
		}
	}

	
	static final class GroupHolder {
		IMBaseImageView imageView;
		TextView userTitle;
		View deleteImg;
		UserEntity contactEntity;
        ImageView role;
	}

    public void toggleDeleteIcon() {
        isRemove = !isRemove;
        notifyDataSetChanged();
    }

}
