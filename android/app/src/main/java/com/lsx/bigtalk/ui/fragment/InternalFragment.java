package com.lsx.bigtalk.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;

import com.lsx.bigtalk.R;
import com.lsx.bigtalk.config.IntentConstant;
import com.lsx.bigtalk.ui.activity.WebViewFragmentActivity;
import com.lsx.bigtalk.ui.adapter.InternalAdapter;
import com.lsx.bigtalk.ui.base.BTBaseFragment;

public class InternalFragment extends BTBaseFragment {
    private View curView = null;
    private ListView internalListView;
    private InternalAdapter mAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (null != curView) {
            ((ViewGroup) curView.getParent()).removeView(curView);
            return curView;
        }
        curView = inflater.inflate(R.layout.internal_fragment,
                baseFragmentLayout);

        initRes();
        mAdapter = new InternalAdapter(this.getActivity());
        internalListView.setAdapter(mAdapter);
        mAdapter.update();
        return curView;
    }

    private void initRes() {
        // 设置顶部标题栏
        setTopCenterTitleText(getActivity().getString(R.string.main_inner_net));
        internalListView = curView.findViewById(R.id.internalListView);
        internalListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String url = mAdapter.getItem(i).getItemUrl();
                Intent intent=new Intent(InternalFragment.this.getActivity(),WebViewFragmentActivity.class);
                intent.putExtra(IntentConstant.WEBVIEW_URL, url);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void initHandler() {
    }

}
