package com.mogujie.tt.ui.base;

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

import androidx.fragment.app.Fragment;

import com.mogujie.tools.ScreenTools;
import com.mogujie.tt.R;
import com.mogujie.tt.ui.activity.SearchActivity;
import com.mogujie.tt.utils.Logger;
import com.mogujie.tt.ui.widget.SearchEditText;
import com.mogujie.tt.ui.widget.TopTabButton;

public abstract class TTBaseFragment extends Fragment {
	/** 导航栏返回按钮 */
	protected ImageView topLeftBtn;
	/** 导航栏右侧功能按钮 */
	protected ImageView topRightBtn;
	/** 导航栏标题 */
	protected TextView topTitleTxt;
	/** 导航栏返回按钮后面的文本 */
	protected TextView topLeftTitleTxt;
	/** 导航栏右侧文本 */
	protected TextView topRightTitleTxt;
	/** 导航栏容器 */
	protected ViewGroup topBar;
	/** 导航栏中的 tab 按钮（联系人页面）*/
	protected TopTabButton topContactTitle;
	/** 导航栏中搜索文本框（搜索页面）*/
	protected SearchEditText topSearchEdt;

	protected ViewGroup topContentView;
	/** 导航栏左侧返回按钮和文案的容器 */
	protected RelativeLayout topLeftContainerLayout;
	/** 导航栏中的搜索框容器（搜索页面）*/
	protected FrameLayout searchFrameLayout;
	/** 导航栏中的 tab按钮的容器（联系人页面）*/
	protected FrameLayout topContactFrame;
	
	protected float x1, y1, x2, y2 = 0;
	protected static Logger logger = Logger.getLogger(TTBaseFragment.class);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		topContentView = (ViewGroup) LayoutInflater.from(getActivity()).inflate(R.layout.tt_fragment_base, null);

		topBar = topContentView.findViewById(R.id.topbar);
		topTitleTxt = topContentView.findViewById(R.id.base_fragment_title);
		topLeftTitleTxt = topContentView.findViewById(R.id.left_txt);
		topRightTitleTxt = topContentView.findViewById(R.id.right_txt);
		topLeftBtn = topContentView.findViewById(R.id.left_btn);
		topRightBtn = topContentView.findViewById(R.id.right_btn);
		topContactTitle = topContentView.findViewById(R.id.contact_tile);
		topSearchEdt = topContentView.findViewById(R.id.chat_title_search);
		topLeftContainerLayout = topContentView.findViewById(R.id.top_left_container);
 		searchFrameLayout = topContentView.findViewById(R.id.searchbar);
		topContactFrame = topContentView.findViewById(R.id.contactTopBar);

		// 默认隐藏 TTBaseFragment 中的控件
		topTitleTxt.setVisibility(View.GONE);
		topRightBtn.setVisibility(View.GONE);
		topLeftBtn.setVisibility(View.GONE);
		topLeftTitleTxt.setVisibility(View.GONE);
		topRightTitleTxt.setVisibility(View.GONE);
		topContactTitle.setVisibility(View.GONE);
		topSearchEdt.setVisibility(View.GONE);
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
	public View onCreateView(LayoutInflater inflater, ViewGroup vg, Bundle bundle) {
		if (null != topContentView) {
			((ViewGroup) topContentView.getParent()).removeView(topContentView);
			return topContentView;
		}
		return topContentView;
	}

	protected void setTopTitleBold(String title) {
		if (title == null) {
			return;
		}
		if (title.length() > 12) {
			title = title.substring(0, 11) + "...";
		}
		// 设置字体为加粗
		TextPaint paint = topTitleTxt.getPaint();
		paint.setFakeBoldText(true); 
		
		topTitleTxt.setText(title);
		topTitleTxt.setVisibility(View.VISIBLE);
	}
	
	protected void setTopTitle(String title) {
		if (title == null) {
			return;
		}
		if (title.length() > 12) {
			title = title.substring(0, 11) + "...";
		}
		topTitleTxt.setText(title);
		topTitleTxt.setVisibility(View.VISIBLE);
	}

	protected void hideTopTitle() {
		topTitleTxt.setVisibility(View.GONE);
	}

	/** 显示联系人页面顶部导航栏中的 tab 按钮组 */
	protected void showContactTopBar() {
		topContactFrame.setVisibility(View.VISIBLE);
		topContactTitle.setVisibility(View.VISIBLE);
	}

	/** 设置导航栏左侧返回按钮背景图 */
	protected void setTopLeftButton(int resID) {
		if (resID <= 0) {
			return;
		}

		topLeftBtn.setImageResource(resID);
		topLeftBtn.setVisibility(View.VISIBLE);
	}

    protected void setTopLeftButtonPadding(int l, int t, int r, int b) {
        topLeftBtn.setPadding(l, t, r, b);
    }

	protected void hideTopLeftButton() {
		topLeftBtn.setVisibility(View.GONE);
	}

	protected void setTopLeftText(String text) {
		if (null == text) {
			return;
		}
		topLeftTitleTxt.setText(text);
		topLeftTitleTxt.setVisibility(View.VISIBLE);
	}

	protected void setTopRightText(String text) {
		if (null == text) {
			return;
		}
		topRightTitleTxt.setText(text);
		topRightTitleTxt.setVisibility(View.VISIBLE);
	}

	protected void setTopRightButton(int resID) {
		if (resID <= 0) {
			return;
		}

		topRightBtn.setImageResource(resID);
		topRightBtn.setVisibility(View.VISIBLE);
	}

	protected void hideTopRightButton() {
		topRightBtn.setVisibility(View.GONE);
	}

	/** 设置导航栏背景 */
	protected void setTopBar(int resID) {
		if (resID <= 0) {
			return;
		}
		topBar.setBackgroundResource(resID);
	}

	/**
	 * 隐藏一般导航栏，显示导航栏总的联系人 tab
	 * */
    protected void hideTopBar() {
        topBar.setVisibility(View.GONE);
        LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) topContactFrame.getLayoutParams();
        linearParams.height = ScreenTools.instance(getActivity()).dip2px(45);
        topContactFrame.setLayoutParams(linearParams);
        topContactFrame.setPadding(0,ScreenTools.instance(getActivity()).dip2px(10),0,0);
    }

	/** 显示导航栏中的搜索文本框 */
	protected void showTopSearchBar() {
		topSearchEdt.setVisibility(View.VISIBLE);
	}

	/** 隐藏导航栏中的搜索文本框 */
	protected void hideTopSearchBar() {
		topSearchEdt.setVisibility(View.GONE);
	}

	/** 显示导航栏中的搜索控件 */
	protected void showSearchFrameLayout() {
		searchFrameLayout.setVisibility(View.VISIBLE);
        searchFrameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                showSearchView();
            }
        });
	}

	protected abstract void initHandler();

	@Override
	public void onActivityCreated(Bundle bundle) {
		logger.d("Fragment onActivityCreate:" + getClass().getName());
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
		setTopRightButton(R.drawable.tt_top_search);
		topRightBtn.setOnClickListener(new View.OnClickListener() {
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
