package com.lsx.bigtalk.ui.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.lsx.bigtalk.AppConstant;
import com.lsx.bigtalk.storage.db.entity.DepartmentEntity;
import com.lsx.bigtalk.storage.db.entity.UserEntity;
import com.lsx.bigtalk.R;

import com.lsx.bigtalk.service.manager.IMContactManager;
import com.lsx.bigtalk.service.service.IMService;
import com.lsx.bigtalk.ui.widget.IMBaseImageView;
import com.lsx.bigtalk.ui.helper.IMUIHelper;
import com.lsx.bigtalk.logs.Logger;


public class DeptAdapter extends BaseAdapter implements
        SectionIndexer,
        AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener {

    private final Logger logger = Logger.getLogger(DeptAdapter.class);
    private List<UserEntity> userList = new ArrayList<>();
    private final Context ctx;
    private final IMService imService;

    public DeptAdapter(Context context, IMService imService) {
        this.ctx = context;
        this.imService = imService;
    }

    public void putUserList(List<UserEntity> pUserList) {
        this.userList.clear();
        if (pUserList == null || pUserList.isEmpty()) {
            return;
        }
        this.userList = pUserList;
        notifyDataSetChanged();
    }

    /**
     * Returns an array of objects representing sections of the list. The
     * returned array and its contents should be non-null.
     * <p/>
     * The list view will call toString() on the objects to get the preview text
     * to display while scrolling. For example, an adapter may return an array
     * of Strings representing letters of the alphabet. Or, it may return an
     * array of objects whose toString() methods return their section titles.
     *
     * @return the array of section objects
     */
    @Override
    public Object[] getSections() {
        return new Object[0];
    }

    /**
     * Given the index of a section within the array of section objects, returns
     * the starting position of that section within the adapter.
     * <p/>
     * If the section's starting position is outside of the adapter bounds, the
     * position must be clipped to fall within the size of the adapter.
     *
     * @param sectionIndex the index of the section within the array of section
     *                     objects
     * @return the starting position of that section within the adapter,
     * constrained to fall within the adapter bounds
     */
    @Override
    public int getPositionForSection(int sectionIndex) {
        int index = 0;
        IMContactManager imContactManager = imService.getIMContactManager();
        for (UserEntity entity : userList) {
            DepartmentEntity department = imContactManager.findDepartment(entity.getDepartmentId());
            if (department == null) {
                return 0;
            }
            String deptPinyin = department.getPinyinElement().pinyin;
            int firstCharacter = TextUtils.isEmpty(deptPinyin) ? -1 : deptPinyin.charAt(0);
            if (firstCharacter == sectionIndex) {
                return index;
            }
            index++;
        }
        return -1;
    }

    /**
     * Given a position within the adapter, returns the index of the
     * corresponding section within the array of section objects.
     * <p/>
     * If the section index is outside of the section array bounds, the index
     * must be clipped to fall within the size of the section array.
     * <p/>
     * For example, consider an indexer where the section at array index 0
     * starts at adapter position 100. Calling this method with position 10,
     * which is before the first section, must return index 0.
     *
     * @param position the position within the adapter for which to return the
     *                 corresponding section index
     * @return the index of the corresponding section within the array of
     * section objects, constrained to fall within the array bounds
     */
    @Override
    public int getSectionForPosition(int position) {
        return 0;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        UserEntity userEntity = (UserEntity) getItem(position);
        IMUIHelper.openUserProfileActivity(ctx, userEntity.getPeerId());
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        UserEntity userEntity = (UserEntity) getItem(position);
        IMUIHelper.handleMsgContactItemLongPressed(userEntity, ctx);
        return true;
    }

    @Override
    public int getCount() {
        if (userList != null) {
            return userList.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (userList != null && position < userList.size()) {
            return userList.get(position);
        }
        return null;
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
            userHolder = new UserHolder();
            view = LayoutInflater.from(ctx).inflate(R.layout.contact_list_item_view, parent,false);
            userHolder.nameView = view.findViewById(R.id.contact_nickname_title);
            userHolder.realNameView = view.findViewById(R.id.contact_real_name_title);
            userHolder.sectionView = view.findViewById(R.id.contact_category_title);
            userHolder.avatar = view.findViewById(R.id.contact_avatar);
            userHolder.divider = view.findViewById(R.id.contact_divider);
            view.setTag(userHolder);
        } else {
            userHolder = (UserHolder) view.getTag();
        }

        userHolder.nameView.setText(userEntity.getMainName());
        userHolder.avatar.setImageResource(R.drawable.image_default_user_avatar);
        userHolder.divider.setVisibility(View.VISIBLE);
        userHolder.sectionView.setVisibility(View.GONE);

        DepartmentEntity deptEntity = imService.getIMContactManager().findDepartment(userEntity.getDepartmentId());
        String sectionName = deptEntity == null ? "" : deptEntity.getDepartName();
        String preSectionName = null;
        if (position > 0) {
            int preDeptId = ((UserEntity)getItem(position - 1)).getDepartmentId();
            DepartmentEntity preDept = imService.getIMContactManager().findDepartment(preDeptId);
            preSectionName = preDept == null ? "" : preDept.getDepartName();
        }

        if (TextUtils.isEmpty(preSectionName) || !preSectionName.equals(sectionName)) {
            userHolder.sectionView.setVisibility(View.VISIBLE);
            userHolder.sectionView.setText(sectionName);
            userHolder.divider.setVisibility(View.GONE);
        } else {
            userHolder.sectionView.setVisibility(View.GONE);
        }

        userHolder.avatar.setDefaultImageRes(R.drawable.image_default_user_avatar);
        userHolder.avatar.setCorner(0);
        userHolder.avatar.setAvatarAppend(AppConstant.SysConstant.AVATAR_APPEND_100);
        userHolder.avatar.setImageUrl(userEntity.getAvatar());

        userHolder.realNameView.setText(userEntity.getRealName());
        userHolder.realNameView.setVisibility(View.GONE);
        return view;
    }

    public static class UserHolder {
        View divider;
        TextView sectionView;
        TextView nameView;
        TextView realNameView;
        IMBaseImageView avatar;
    }

    public int locateDepartment(String departmentTitle) {
        int index = 0;
        for (UserEntity entity : userList) {
            DepartmentEntity deptEntity = imService.getIMContactManager().findDepartment(entity.getDepartmentId());
            String sectionName = deptEntity == null ? "" : deptEntity.getDepartName();
            if (sectionName != null && !sectionName.isEmpty() && (0 == sectionName.compareToIgnoreCase(departmentTitle))) {
                return index;
            }
            index++;
        }
        return -1;
    }
}
