package com.lsx.bigtalk.ui.adapter;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lsx.bigtalk.AppConstant;
import com.lsx.bigtalk.service.support.SessionInfo;
import com.mogujie.tools.ScreenTools;

import com.lsx.bigtalk.storage.db.entity.GroupEntity;
import com.lsx.bigtalk.R;

import com.lsx.bigtalk.utils.DateUtil;
import com.lsx.bigtalk.logs.Logger;
import com.lsx.bigtalk.ui.widget.IMBaseImageView;
import com.lsx.bigtalk.ui.widget.IMGroupAvatar;


@SuppressLint("ResourceAsColor")
public class SessionAdapter extends BaseAdapter {
    private LayoutInflater mInflater = null;
    private List<SessionInfo> sessionList = new ArrayList<>();
    private final Logger logger = Logger.getLogger(SessionAdapter.class);
    private static final int SESSION_TYPE_INVALID = 0;
    private static final int SESSION_TYPE_SINGLE = 1;
    private static final int SESSION_TYPE_GROUP = 2;

    public SessionAdapter(Context context) {
        this.mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return sessionList.size();
    }

    @Override
    public SessionInfo getItem(int position) {
        if (position >= sessionList.size() || position < 0) {
            return null;
        }
        return sessionList.get(position);
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public int getItemViewType(int position) {
        try {
            if (position >= sessionList.size()) {
                return SESSION_TYPE_INVALID;
            }
            SessionInfo sessionInfo = sessionList.get(position);
            if (sessionInfo.getSessionType() == AppConstant.DBConstant.SESSION_TYPE_SINGLE) {
                return SESSION_TYPE_SINGLE;
            } else if (sessionInfo.getSessionType() == AppConstant.DBConstant.SESSION_TYPE_GROUP) {
                return SESSION_TYPE_GROUP;
            } else {
                return SESSION_TYPE_INVALID;
            }
        } catch (Exception e) {
            logger.e(e.toString());
            return SESSION_TYPE_INVALID;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        try {
            final int type = getItemViewType(position);
            SessionHolderBase holder = null;

            switch (type) {
                case SESSION_TYPE_SINGLE:
                    convertView = renderUser(position, convertView, parent);
                    break;
                case SESSION_TYPE_GROUP:
                    convertView = renderGroup(position, convertView, parent);
                    break;
            }
            return convertView;
        } catch (Exception e) {
            logger.e(e.toString());
            return null;
        }
    }

    public void shieldSession(GroupEntity entity) {
        String sessionKey = entity.getSessionKey();
        for (SessionInfo sessionInfo : sessionList) {
            if (sessionInfo.getSessionKey().equals(sessionKey)) {
                sessionInfo.setIsShield(entity.getStatus() == AppConstant.DBConstant.GROUP_STATUS_SHIELD);
                notifyDataSetChanged();
                break;
            }
        }
    }

    public void spinSession(String sessionKey, boolean isSpin) {
        for (SessionInfo sessionInfo : sessionList) {
            if (sessionInfo.getSessionKey().equals(sessionKey)) {
                sessionInfo.setIsSpin(isSpin);
                notifyDataSetChanged();
                break;
            }
        }
    }

    public int getUnreadPositionOnView(int currentPostion) {
        int nextIndex = currentPostion + 1;
        int sum = getCount();
        if (nextIndex > sum) {
            currentPostion = 0;
        }
        /* 从当前点到末尾 */
        for (int index = nextIndex; index < sum; index++) {
            int unCnt = sessionList.get(index).getUnReadCnt();
            if (unCnt > 0) {
                return index;
            }
        }
        /* 从0到当前点 */
        for (int index = 0; index < currentPostion; index++) {
            int unCnt = sessionList.get(index).getUnReadCnt();
            if (unCnt > 0) {
                return index;
            }
        }

        return 0;
    }

    private static final class SingleSessionViewHolder extends SessionHolderBase {
        public IMBaseImageView avatar;
    }

    private final static class GroupSessionViewHolder extends SessionHolderBase {
        public IMGroupAvatar avatarLayout;
    }

    public static class SessionHolderBase {
        public TextView uname;
        public TextView lastContent;
        public TextView lastTime;
        public TextView msgCount;
        public ImageView noDisturb;
    }

    private View renderUser(int position, View convertView, ViewGroup parent) {
        SessionInfo sessionInfo = sessionList.get(position);
        SingleSessionViewHolder holder;
        if (null == convertView) {
            convertView = mInflater.inflate(R.layout.session_item_view, parent, false);
            holder = new SingleSessionViewHolder();
            holder.avatar = convertView.findViewById(R.id.contact_avatar);
            holder.uname = convertView.findViewById(R.id.contact_name);
            holder.lastContent = convertView.findViewById(R.id.message_body);
            holder.lastTime = convertView.findViewById(R.id.message_time);
            holder.msgCount = convertView.findViewById(R.id.unread_message_count);
            holder.noDisturb = convertView.findViewById(R.id.message_time_no_disturb_view);
            holder.avatar.setImageResource(R.drawable.image_default_user_avatar);
            convertView.setTag(holder);
        } else {
            holder = (SingleSessionViewHolder) convertView.getTag();
        }

        if (sessionInfo.getIsSpin()) {
            // todo   R.color.top_session_background
            convertView.setBackgroundColor(Color.parseColor("#f4f4f4f4"));
        } else {
            convertView.setBackgroundColor(Color.WHITE);
        }

        handleSingleContact(holder, sessionInfo);
        return convertView;
    }

    private View renderGroup(int position, View convertView, ViewGroup parent) {
        SessionInfo sessionInfo = sessionList.get(position);
        GroupSessionViewHolder holder;
        if (null == convertView) {
            convertView = mInflater.inflate(R.layout.group_session_item_view, parent, false);
            holder = new GroupSessionViewHolder();
            holder.avatarLayout = convertView.findViewById(R.id.contact_avatar);
            holder.uname = convertView.findViewById(R.id.contact_name);
            holder.lastContent = convertView.findViewById(R.id.message_body);
            holder.lastTime = convertView.findViewById(R.id.message_time);
            holder.msgCount = convertView.findViewById(R.id.unread_message_count);
            holder.noDisturb = convertView.findViewById(R.id.message_time_no_disturb_view);
            convertView.setTag(holder);
        } else {
            holder = (GroupSessionViewHolder) convertView.getTag();
        }

        if (sessionInfo.getIsSpin()) {
            // todo   R.color.top_session_background
            convertView.setBackgroundColor(Color.parseColor("#f4f4f4f4"));
        } else {
            convertView.setBackgroundColor(Color.WHITE);
        }
        
        if (sessionInfo.getIsShield()) {
            holder.noDisturb.setVisibility(View.VISIBLE);
        } else {
            holder.noDisturb.setVisibility(View.GONE);
        }

        handleGroupContact(holder, sessionInfo);
        return convertView;
    }
    
    // base-adapter-helper 了解一下
    public void setData(List<SessionInfo> sessionList) {
        logger.d("SessionAdapter#sessionList Changed");
        this.sessionList = sessionList;
        notifyDataSetChanged();
    }

    private void handleSingleContact(SingleSessionViewHolder contactViewHolder, SessionInfo sessionInfo) {
        String avatarUrl = null;
        String userName = "";
        String lastContent = "";
        String lastTime = "";
        int unReadCount = 0;

        userName = sessionInfo.getName();
        lastContent = sessionInfo.getLatestMsgData();
        // todo 是不是每次都需要计算
        lastTime = DateUtil.getSessionTime(sessionInfo.getUpdateTime());
        unReadCount = sessionInfo.getUnReadCnt();
        if (null != sessionInfo.getAvatar() && !sessionInfo.getAvatar().isEmpty()) {
            avatarUrl = sessionInfo.getAvatar().get(0);

        }
        // 设置未读消息计数
        if (unReadCount > 0) {
            String strCountString = String.valueOf(unReadCount);
            if (unReadCount > 99) {
                strCountString = "99+";
            }
            contactViewHolder.msgCount.setVisibility(View.VISIBLE);
            contactViewHolder.msgCount.setText(strCountString);
        } else {
            contactViewHolder.msgCount.setVisibility(View.GONE);
        }
        //头像设置
        contactViewHolder.avatar.setDefaultImageRes(R.drawable.image_default_user_avatar);
        contactViewHolder.avatar.setCorner(8);
        contactViewHolder.avatar.setAvatarAppend(AppConstant.SysConstant.AVATAR_APPEND_100);
        contactViewHolder.avatar.setImageUrl(avatarUrl);
        // 设置其它信息
        contactViewHolder.uname.setText(userName);
        contactViewHolder.lastContent.setText(lastContent);
        contactViewHolder.lastTime.setText(lastTime);
    }

    private void handleGroupContact(GroupSessionViewHolder groupViewHolder, SessionInfo sessionInfo) {
        String userName;
        String lastContent;
        String lastTime;
        int unReadCount;

        userName = sessionInfo.getName();
        lastContent = sessionInfo.getLatestMsgData();
        // todo 是不是每次都需要计算
        lastTime = DateUtil.getSessionTime(sessionInfo.getUpdateTime());
        unReadCount = sessionInfo.getUnReadCnt();

        if (unReadCount > 0) {
            if (sessionInfo.getIsShield()) {
                groupViewHolder.msgCount.setBackgroundResource(R.drawable.ic_message_notify_no_disturb);
                groupViewHolder.msgCount.setVisibility(View.VISIBLE);
                groupViewHolder.msgCount.setText("");
                ((RelativeLayout.LayoutParams) groupViewHolder.msgCount.getLayoutParams()).leftMargin = ScreenTools.instance(this.mInflater.getContext()).dip2px(-7);
                ((RelativeLayout.LayoutParams) groupViewHolder.msgCount.getLayoutParams()).topMargin = ScreenTools.instance(this.mInflater.getContext()).dip2px(6);
                groupViewHolder.msgCount.getLayoutParams().width = ScreenTools.instance(this.mInflater.getContext()).dip2px(10);
                groupViewHolder.msgCount.getLayoutParams().height = ScreenTools.instance(this.mInflater.getContext()).dip2px(10);
            } else {
                groupViewHolder.msgCount.setBackgroundResource(R.drawable.ic_message_notify);
                groupViewHolder.msgCount.setVisibility(View.VISIBLE);
                ((RelativeLayout.LayoutParams) groupViewHolder.msgCount.getLayoutParams()).leftMargin = ScreenTools.instance(this.mInflater.getContext()).dip2px(-10);
                ((RelativeLayout.LayoutParams) groupViewHolder.msgCount.getLayoutParams()).topMargin = ScreenTools.instance(this.mInflater.getContext()).dip2px(3);
                groupViewHolder.msgCount.getLayoutParams().width = RelativeLayout.LayoutParams.WRAP_CONTENT;
                groupViewHolder.msgCount.getLayoutParams().height = RelativeLayout.LayoutParams.WRAP_CONTENT;
                groupViewHolder.msgCount.setPadding(ScreenTools.instance(this.mInflater.getContext()).dip2px(3), 0, ScreenTools.instance(this.mInflater.getContext()).dip2px(3), 0);

                String strCountString = String.valueOf(unReadCount);
                if (unReadCount > 99) {
                    strCountString = "99+";
                }
                groupViewHolder.msgCount.setVisibility(View.VISIBLE);
                groupViewHolder.msgCount.setText(strCountString);
            }

        } else {
            groupViewHolder.msgCount.setVisibility(View.GONE);
        }

        //头像设置
        setGroupAvatar(groupViewHolder, sessionInfo.getAvatar());
        // 设置其它信息
        groupViewHolder.uname.setText(userName);
        groupViewHolder.lastContent.setText(lastContent);
        groupViewHolder.lastTime.setText(lastTime);
    }

    private void setGroupAvatar(GroupSessionViewHolder holder, List<String> avatarUrlList) {
        try {
            if (null == avatarUrlList) {
                return;
            }
            holder.avatarLayout.setAvatarUrlAppend(AppConstant.SysConstant.AVATAR_APPEND_32);
            holder.avatarLayout.setChildCorner(3);
            holder.avatarLayout.setAvatarUrls(new ArrayList<String>(avatarUrlList));
        } catch (Exception e) {
            logger.e(e.toString());
        }
    }
}
