package com.lsx.bigtalk.ui.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.viewpager.widget.ViewPager;

import com.lsx.bigtalk.AppConstant;
import com.lsx.bigtalk.R;
import com.lsx.bigtalk.ui.adapter.EmojiGridViewAdapter;
import com.lsx.bigtalk.ui.adapter.ViewPageAdapter;

import com.lsx.bigtalk.ui.helper.Emoparser;
import com.lsx.bigtalk.utils.CommonUtil;


public class EmojiGridView extends LinearLayout {
    private final Context context;
    private ViewPager viewPager;
    private OnEmojiGridViewItemClick onEmojiGridViewItemClick;
    private LinearLayout paginationDotsLayout;
    private ImageView[] paginationDots;
    private int viewPagerCurrentIndex;
    private int viewPagerTotalPagesCount;

    public EmojiGridView(Context cxt) {
        super(cxt);
        context = cxt;
        initViewPage();
        initFootDots();
    }

    public EmojiGridView(Context cxt, AttributeSet attrs) {
        super(cxt, attrs);
        context = cxt;
        initViewPage();
        initFootDots();
    }

    private void initViewPage() {
        setOrientation(VERTICAL);

        viewPager = new ViewPager(context);
        LayoutParams params = new LayoutParams(
                LayoutParams.MATCH_PARENT, CommonUtil.getDefaultPannelHeight(context));
        params.gravity = Gravity.BOTTOM;
        viewPager.setLayoutParams(params);

        paginationDotsLayout = new LinearLayout(context);
        paginationDotsLayout.setLayoutParams(
                new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        paginationDotsLayout.setGravity(Gravity.CENTER);
        paginationDotsLayout.setOrientation(HORIZONTAL);
        addView(viewPager);
        addView(paginationDotsLayout);
    }

    private void initFootDots() {
        viewPagerTotalPagesCount = (int) Math.ceil((double) Emoparser.getInstance(context)
                .getResIdList().length / (AppConstant.SysConstant.emojiPageSize - 1));
        int mod = Emoparser.getInstance(context).getResIdList().length
                % (AppConstant.SysConstant.emojiPageSize - 1);
        if (mod == 1) {
            --viewPagerTotalPagesCount;
        }

        if (0 < viewPagerTotalPagesCount) {
            if (viewPagerTotalPagesCount == 1) {
                paginationDotsLayout.setVisibility(View.GONE);
            } else {
                paginationDotsLayout.setVisibility(View.VISIBLE);
                for (int i = 0; i < viewPagerTotalPagesCount; i++) {
                    ImageView image = new ImageView(context);
                    image.setTag(i);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LayoutParams.WRAP_CONTENT,
                            LayoutParams.WRAP_CONTENT);
                    params.setMargins(
                            5,
                            CommonUtil.getElementSzie(context) / 2,
                            5,
                            CommonUtil.getElementSzie(context) / 2);
                    image.setBackgroundResource(R.drawable.ic_emoji_dots);
                    image.setEnabled(false);
                    paginationDotsLayout.addView(image, params);
                }
            }
        }
        if (1 != viewPagerTotalPagesCount) {
            paginationDots = new ImageView[viewPagerTotalPagesCount];
            for (int i = 0; i < viewPagerTotalPagesCount; i++) {
                paginationDots[i] = (ImageView) paginationDotsLayout.getChildAt(i);
                paginationDots[i].setEnabled(true);
                paginationDots[i].setTag(i);
            }
            viewPagerCurrentIndex = 0;
            paginationDots[viewPagerCurrentIndex].setEnabled(false);
            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    setCurDot(position);
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        }
    }

    private void setCurDot(int position) {
        if (
            position < 0
            ||
            position > viewPagerTotalPagesCount - 1
            ||
            viewPagerCurrentIndex == position
        ) {
            return;
        }
        paginationDots[position].setEnabled(false);
        paginationDots[viewPagerCurrentIndex].setEnabled(true);
        viewPagerCurrentIndex = position;
    }

    public void setAdapter() {
        if (onEmojiGridViewItemClick == null) {
            return;
        }
        List<GridView> emojiGridViews  = new ArrayList<GridView>();
        for (int i = 0; i < viewPagerTotalPagesCount; i++) {
            emojiGridViews.add(getViewPagerItem(i));
        }
        viewPager.setAdapter(new ViewPageAdapter(emojiGridViews));
    }

    // index => pageNo
    private int[] getGridViewData(int index) {
        ++index;
        int startPos = (index - 1) * (AppConstant.SysConstant.emojiPageSize - 1);
        int endPos = index * (AppConstant.SysConstant.emojiPageSize - 1);

        if (endPos > Emoparser.getInstance(context).getResIdList().length) {
            endPos = Emoparser.getInstance(context).getResIdList().length;
        }
        // add one more space for go back icon
        int length = endPos - startPos + 1;
        int[] resourceIds = new int[length];

        int num = 0;
        for (int i = startPos; i < endPos; i++) {
            resourceIds[num] = Emoparser.getInstance(context).getResIdList()[i];
            num++;
        }
        if (length > 1) {
            resourceIds[length - 1] = R.drawable.ic_emoji_back_normal;
        }

        return resourceIds;
    }

    private GridView getViewPagerItem(final int index) {
        GridView gridView = new GridView(context);
        gridView.setLayoutParams(
                new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        gridView.setNumColumns(7);
        gridView.setVerticalScrollBarEnabled(false);
        gridView.setHorizontalScrollBarEnabled(false);
        gridView.setPadding(8, 8, 8, 0);
        gridView.setVerticalSpacing(
                CommonUtil.getElementSzie(context) / 2 + CommonUtil.getElementSzie(context) / 3);
        gridView.setBackgroundColor(Color.TRANSPARENT);
        gridView.setAdapter(
                new EmojiGridViewAdapter(context, getGridViewData(index)));
        gridView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int start = index * (AppConstant.SysConstant.emojiPageSize - 1);
                onEmojiGridViewItemClick.onItemClick(position + start, index);
            }
        });
        return gridView;
    }

    public void setOnEmojiGridViewItemClick(OnEmojiGridViewItemClick onEmojiGridViewItemClick) {
        this.onEmojiGridViewItemClick = onEmojiGridViewItemClick;
    }

    public interface OnEmojiGridViewItemClick {
        void onItemClick(int position, int viewIndex);
    }
}
