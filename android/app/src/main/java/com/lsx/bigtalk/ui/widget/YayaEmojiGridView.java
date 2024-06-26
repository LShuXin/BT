package com.lsx.bigtalk.ui.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.viewpager.widget.ViewPager;

import com.lsx.bigtalk.R;
import com.lsx.bigtalk.ui.adapter.ViewPageAdapter;
import com.lsx.bigtalk.config.SysConstant;
import com.lsx.bigtalk.ui.adapter.YayaEmojiGridViewAdapter;
import com.lsx.bigtalk.ui.helper.Emoparser;
import com.lsx.bigtalk.utils.CommonUtil;


public class YayaEmojiGridView extends LinearLayout {
    private final Context context;
    private ViewPager viewPager;
    private LinearLayout paginationDotsLayout;
    private ImageView[] paginationDots;
    private OnYayaEmojiGridViewItemClick onYayaEmojiGridViewItemClick;
    private int viewPagerCurrentIndex;
    private int viewPagerTotalPagesCount;


    /*
      Constructor with Context argument
      This constructor takes a single argument of type Context.
      It calls the superclass constructor using super(cxt), likely passing the Context argument to
      the parent class for its own initialization.
      It stores the Context argument in a member variable called context.
     */
    public YayaEmojiGridView(Context cxt) {
        super(cxt);
        context = cxt;
        initViewPage();
        initFootDots();
    }

    /*
      Constructor with Context and AttributeSet arguments
      This constructor takes two arguments: a Context and an AttributeSet.
      The Context argument serves the same purpose as in the previous constructor.
      The AttributeSet argument provides a set of attribute-value pairs that can be used to customize
      the appearance and behavior of the grid view. These attributes are typically defined in an XML
      layout file where the view is used.
      It calls the superclass constructor using super(cxt, attrs), likely passing both the Context
      and AttributeSet arguments to the parent class for initialization, allowing it to potentially
      use the attributes for customization.
      It then stores the Context argument in a member variable, similar to the first constructor.
    */
    public YayaEmojiGridView(Context cxt, AttributeSet attrs) {
        super(cxt, attrs);
        context = cxt;
        initViewPage();
        initFootDots();
    }

    private void initViewPage() {
        setOrientation(VERTICAL);

        viewPager = new ViewPager(context);
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
                CommonUtil.getDefaultPannelHeight(context));
        params.gravity = Gravity.BOTTOM;
        viewPager.setLayoutParams(params);

        paginationDotsLayout = new LinearLayout(context);
        paginationDotsLayout.setLayoutParams(
                new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        paginationDotsLayout.setGravity(Gravity.CENTER);
        paginationDotsLayout.setOrientation(HORIZONTAL);
        addView(viewPager);
        addView(paginationDotsLayout);
        // TODO
        // how to layout viewPager and paginationDotsLayout
    }

    private void initFootDots() {
        viewPagerTotalPagesCount = (int) Math.ceil((double) Emoparser.getInstance(context)
                .getYayaResIdList().length / (SysConstant.yayaEmojiPageSize - 1));

        // need one more position for something in the end
        // TODO
        int mod = Emoparser.getInstance(context).getResIdList().length
                % (SysConstant.yayaEmojiPageSize - 1);
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
                    LayoutParams params = new LayoutParams(
                            LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                    params.setMargins(
                            5,
                            CommonUtil.getElementSzie(context) / 2,
                            5,
                            CommonUtil.getElementSzie(context) / 2);
                    image.setBackgroundResource(R.drawable.default_emo_dots);
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
        if (onYayaEmojiGridViewItemClick == null) {
            return;
        }

        List<GridView> yayaEmojiGridViews = new ArrayList<GridView>();
        for (int i = 0; i < viewPagerTotalPagesCount; i++) {
            yayaEmojiGridViews.add(getViewPagerItem(i));
        }
        viewPager.setAdapter(new ViewPageAdapter(yayaEmojiGridViews));
    }

    private GridView getViewPagerItem(final int index) {
        GridView gridView = new GridView(context);
        gridView.setLayoutParams(
                new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        gridView.setNumColumns(4);
        gridView.setVerticalScrollBarEnabled(false);
        gridView.setHorizontalScrollBarEnabled(false);
        gridView.setPadding(8, 8, 8, 0);
        gridView.setVerticalSpacing(20);
        gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        gridView.setAdapter(new YayaEmojiGridViewAdapter(context, getGridViewData(index)));
        gridView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int start = index * SysConstant.yayaEmojiPageSize;
                onYayaEmojiGridViewItemClick.onItemClick(position + start, index);
            }
        });
        return gridView;
    }

    private int[] getGridViewData(int index) {
        ++index;
        int startPos = (index - 1) * SysConstant.yayaEmojiPageSize;
        int endPos = index * SysConstant.yayaEmojiPageSize - 1;

        if (endPos > Emoparser.getInstance(context).getYayaResIdList().length) {
            endPos = Emoparser.getInstance(context).getYayaResIdList().length - 1;
        }
        int[] resourceIds = new int[endPos - startPos + 1];

        int num = 0;
        for (int i = startPos; i <= endPos; i++) {
            resourceIds[num] = Emoparser.getInstance(context).getYayaResIdList()[i];
            num++;
        }
        return resourceIds;
    }

    public void setOnYayaEmojiGridViewItemClick(OnYayaEmojiGridViewItemClick onYayaEmojiGridViewItemClick) {
        this.onYayaEmojiGridViewItemClick = onYayaEmojiGridViewItemClick;
    }

    public interface OnYayaEmojiGridViewItemClick {
        void onItemClick(int pos, int viewIndex);
    }
}
