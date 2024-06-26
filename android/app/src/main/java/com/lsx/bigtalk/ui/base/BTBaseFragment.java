package com.lsx.bigtalk.ui.base;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.mogujie.tools.ScreenTools;
import com.lsx.bigtalk.R;
import com.lsx.bigtalk.ui.activity.SearchActivity;
import com.lsx.bigtalk.utils.Logger;
import com.lsx.bigtalk.ui.widget.SearchEditText;
import com.lsx.bigtalk.ui.widget.TopTabButtonGroup;

public abstract class BTBaseFragment extends Fragment {
	protected ViewGroup baseFragmentLayout;
	/** 导航栏容器 */
	protected ViewGroup appBar;
	/** 导航栏左侧返回按钮和文案的容器 */
	protected RelativeLayout topLeftContainerLayout;
	/** 导航栏返回按钮 */
	protected ImageView topLeftBtnImageView;
	/** 导航栏返回按钮后面的文本 */
	protected TextView topLeftBtnTitleTextView;
	/** 导航栏标题 */
	protected TextView topCenterTitleTextView;
	/** 导航栏右侧功能按钮 */
	protected ImageView topRightBtnImageView;
	/** 导航栏右侧文本 */
	protected TextView topRightBtnTitleTextView;

	/** 导航栏中的 tab 按钮组（联系人页面）*/
	protected TopTabButtonGroup topTabButtonGroup;
	/** 导航栏中的 tab 按钮的容器（联系人页面）*/
	protected FrameLayout topTabButtonGroupLayout;

	/** 导航栏中的搜索框容器（搜索页面）*/
	protected FrameLayout topSearchBarFrameLayout;
	/** 导航栏中搜索文本框（搜索页面）*/
	protected SearchEditText topSearchBarSearchEditText;

	protected float x1, y1, x2, y2 = 0;
	protected static Logger logger = Logger.getLogger(BTBaseFragment.class);

	@SuppressLint("InflateParams")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		baseFragmentLayout = (ViewGroup) LayoutInflater.from(getActivity()).inflate(R.layout.base_fragment, null);
		appBar = baseFragmentLayout.findViewById(R.id.app_bar);
	
		topLeftContainerLayout = baseFragmentLayout.findViewById(R.id.top_left_container);
		topLeftBtnImageView = baseFragmentLayout.findViewById(R.id.left_btn);
		topLeftBtnTitleTextView = baseFragmentLayout.findViewById(R.id.left_txt);

		topCenterTitleTextView = baseFragmentLayout.findViewById(R.id.base_fragment_title);

		topRightBtnImageView = baseFragmentLayout.findViewById(R.id.right_btn);
		topRightBtnTitleTextView = baseFragmentLayout.findViewById(R.id.right_txt);

		topTabButtonGroupLayout = baseFragmentLayout.findViewById(R.id.contactTopBar);
		topTabButtonGroup = baseFragmentLayout.findViewById(R.id.contact_tile);

		topSearchBarFrameLayout = baseFragmentLayout.findViewById(R.id.searchbar);
		topSearchBarSearchEditText = baseFragmentLayout.findViewById(R.id.chat_title_search);

		// 默认隐藏控件
		topLeftBtnImageView.setVisibility(View.GONE);
		topLeftBtnTitleTextView.setVisibility(View.GONE);
		topCenterTitleTextView.setVisibility(View.GONE);
		topRightBtnImageView.setVisibility(View.GONE);
		topRightBtnTitleTextView.setVisibility(View.GONE);
		topTabButtonGroup.setVisibility(View.GONE);
		topSearchBarSearchEditText.setVisibility(View.GONE);
	}

	// 这是 Android 开发中 Fragment 的生命周期方法之一，用于创建该 Fragment 的用户界面（UI）部分。
	// 具体来说，onCreateView 方法被用来实例化并返回与 Fragment 相关联的视图。这个方法有三个参数：
	// 1. LayoutInflater inflater： 用于从 XML 布局文件中创建视图的对象。可以使用它将 XML 布局文件
	// 转换为实际的视图对象。
	// 2. ViewGroup container： Fragment 的父视图，即要将该 Fragment 的视图添加到的容器。通常，在
	// onCreateView 方法内，你会使用 inflater 创建视图，并将其返回，而 container 参数则用于确定
	// 这个视图将被添加到哪个容器中。
	// 3. Bundle savedInstanceState： 保存了 Fragment 的状态信息，用于在重新创建 Fragment 时恢复
	// 之前的状态。
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup vg, Bundle bundle) {
		if (null != baseFragmentLayout) {
			((ViewGroup) baseFragmentLayout.getParent()).removeView(baseFragmentLayout);
			return baseFragmentLayout;
		}
		return null;
	}

	protected void setTopCenterTitleTextBold(String title) {
		if (title == null) {
			return;
		}
		if (title.length() > 12) {
			title = title.substring(0, 11) + "...";
		}
		// 设置字体为加粗
		TextPaint paint = topCenterTitleTextView.getPaint();
		paint.setFakeBoldText(true); 
		
		topCenterTitleTextView.setText(title);
		topCenterTitleTextView.setVisibility(View.VISIBLE);
	}
	
	protected void setTopCenterTitleText(String title) {
		if (title == null) {
			return;
		}
		if (title.length() > 12) {
			title = title.substring(0, 11) + "...";
		}
		topCenterTitleTextView.setText(title);
		topCenterTitleTextView.setVisibility(View.VISIBLE);
	}

	protected void hideTopCenterTitle() {
		topCenterTitleTextView.setVisibility(View.GONE);
	}

	/** 显示联系人页面顶部导航栏中的 tab 按钮组 */
	protected void showTopTabButtonGroup() {
		topTabButtonGroupLayout.setVisibility(View.VISIBLE);
		topTabButtonGroup.setVisibility(View.VISIBLE);
	}

	/** 设置导航栏左侧返回按钮背景图 */
	protected void setTopLeftBtnImage(int resId) {
		if (resId <= 0) {
			return;
		}

		topLeftBtnImageView.setImageResource(resId);
		topLeftBtnImageView.setVisibility(View.VISIBLE);
	}

    protected void setTopLeftBtnPadding(int l, int t, int r, int b) {
        topLeftBtnImageView.setPadding(l, t, r, b);
    }

	protected void hideTopLeftBtn() {
		topLeftBtnImageView.setVisibility(View.GONE);
	}

	protected void setTopLeftText(String text) {
		if (null == text) {
			return;
		}
		topLeftBtnTitleTextView.setText(text);
		topLeftBtnTitleTextView.setVisibility(View.VISIBLE);
	}

	protected void setTopRightText(String text) {
		if (null == text) {
			return;
		}
		topRightBtnTitleTextView.setText(text);
		topRightBtnTitleTextView.setVisibility(View.VISIBLE);
	}

	protected void setTopRightBtnImage(int resId) {
		if (resId <= 0) {
			return;
		}

		topRightBtnImageView.setImageResource(resId);
		topRightBtnImageView.setVisibility(View.VISIBLE);
	}

	protected void hideTopRightBtn() {
		topRightBtnImageView.setVisibility(View.GONE);
	}

	/** 设置导航栏背景 */
	protected void setAppBarImage(int resId) {
		if (resId <= 0) {
			return;
		}
		appBar.setBackgroundResource(resId);
	}

	/**
	 * 隐藏一般导航栏，显示导航栏总的联系人 tab
	 * */
    protected void hideAppBar() {
        appBar.setVisibility(View.GONE);
        LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) topTabButtonGroupLayout.getLayoutParams();
        linearParams.height = ScreenTools.instance(getActivity()).dip2px(45);
        topTabButtonGroupLayout.setLayoutParams(linearParams);
        topTabButtonGroupLayout.setPadding(0,ScreenTools.instance(getActivity()).dip2px(10),0,0);
    }

	/** 显示导航栏中的搜索文本框 */
	protected void showTopSearchBar() {
		topSearchBarSearchEditText.setVisibility(View.VISIBLE);
	}

	/** 隐藏导航栏中的搜索文本框 */
	protected void hideTopSearchBar() {
		topSearchBarSearchEditText.setVisibility(View.GONE);
	}

	/** 显示导航栏中的搜索控件 */
	protected void showTopSearchBarFrameLayout() {
		topSearchBarFrameLayout.setVisibility(View.VISIBLE);
        topSearchBarFrameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                showSearchView();
            }
        });
	}

	protected abstract void initHandler();

	@Override
	public void onActivityCreated(Bundle bundle) {
		logger.d("BTBaseFragment#onActivityCreate");
		super.onActivityCreated(bundle);
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	protected void initSearch() {
		setTopRightBtnImage(R.drawable.top_search);
		topRightBtnImageView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// 显示搜索页面
				showSearchView();
			}
		});
	}

	public void showSearchView() {
		startActivity(new Intent(getActivity(), SearchActivity.class));
	}
	
	protected void onSearchDataReady() {
		initSearch();
	}
}
