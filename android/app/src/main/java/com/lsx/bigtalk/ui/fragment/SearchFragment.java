package com.lsx.bigtalk.ui.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;

import com.mogujie.tools.ScreenTools;
import com.lsx.bigtalk.storage.db.entity.DepartmentEntity;
import com.lsx.bigtalk.storage.db.entity.GroupEntity;
import com.lsx.bigtalk.storage.db.entity.UserEntity;
import com.lsx.bigtalk.R;
import com.lsx.bigtalk.service.support.IMServiceConnector;
import com.lsx.bigtalk.service.service.IMService;
import com.lsx.bigtalk.ui.adapter.SearchResAdapter;
import com.lsx.bigtalk.ui.base.BTBaseFragment;
import com.lsx.bigtalk.logs.Logger;

import java.util.List;

/**
 * @yingmu  modify
 */
public class SearchFragment extends BTBaseFragment {

	private final Logger logger = Logger.getLogger(SearchFragment.class);
	private View curView = null;
	private ListView listView;
    private View noSearchResultView;
	private SearchResAdapter adapter;
	IMService imService;

    private final IMServiceConnector imServiceConnector = new IMServiceConnector(){
        @Override
        public void onIMServiceConnected() {
            logger.d("config#onIMServiceConnected");
            imService = imServiceConnector.getIMService();
            //init set adapter service
            initAdapter();
        }
        @Override
        public void onServiceDisconnected() {
        }
    };

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

		imServiceConnector.connect(this.getActivity());
		if (null != curView) {
			((ViewGroup) curView.getParent()).removeView(curView);
			return curView;
		}
		curView = inflater.inflate(R.layout.search_fragment, baseFragmentLayout);
        noSearchResultView = curView.findViewById(R.id.layout_no_search_result);
		initTopBar();
        listView = curView.findViewById(R.id.search);
		return curView;
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	private void initTopBar() {
        setAppBarImage(R.drawable.bg_appbar);
		showTopSearchBar();
		setTopLeftBtnImage(R.drawable.ic_back);
		hideTopRightBtn();

        topLeftBtnImageView.setPadding(0, 0, ScreenTools.instance(getActivity()).dip2px(30), 0);
		topLeftBtnImageView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                getActivity().finish();
            }
        });

        topSearchBarSearchEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				String key = s.toString();
				adapter.setSearchKey(key);
                if(key.isEmpty())
                {
                    adapter.clear();
                    noSearchResultView.setVisibility(View.GONE);
                }else{
                    searchEntityLists(key);
                }
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}
			@Override
			public void afterTextChanged(Editable s) {
			}
		});
	}

    private void initAdapter(){
        adapter = new SearchResAdapter(getActivity(),imService);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(adapter);
        listView.setOnItemLongClickListener(adapter);
    }

    // 文字高亮search 模块
	private void searchEntityLists(String key) {
        List<UserEntity> contactList = imService.getIMContactManager().searchUserContact(key);
        int contactSize = contactList.size();
        adapter.putUserList(contactList);

        List<GroupEntity> groupList = imService.getIMGroupManager().searchGroup(key);
        int groupSize = groupList.size();
        adapter.putGroupList(groupList);

        List<DepartmentEntity> departmentList = imService.getIMContactManager().searchDeptContact(key);
        int deptSize = departmentList.size();
        adapter.putDeptList(departmentList);

        int sum = contactSize + groupSize +deptSize;
        adapter.notifyDataSetChanged();
        if(sum <= 0){
            noSearchResultView.setVisibility(View.VISIBLE);
        }else{
            noSearchResultView.setVisibility(View.GONE);
        }
	}

  	@Override
	protected void initHandler() {
	}

    @Override
    public void onDestroy() {
        imServiceConnector.disconnect(getActivity());
        super.onDestroy();
    }
}
