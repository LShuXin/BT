package com.lsx.bigtalk.ui.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.lsx.bigtalk.R;
import com.lsx.bigtalk.ui.adapter.album.ImageGridAdapter;
import com.lsx.bigtalk.ui.adapter.album.ImageItem;
import com.lsx.bigtalk.ui.widget.CustomViewPager;
import com.lsx.bigtalk.utils.ImageUtil;
import com.lsx.bigtalk.utils.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class PreviewActivity extends Activity
        implements
        ViewPager.OnPageChangeListener {

    private CustomViewPager viewPager;
    private ImageView[] tips;
    private ImageView[] mImageViews;
    private ViewGroup group;
    private ImageView select;
    private final ImageGridAdapter adapter = ImageGridActivity.getAdapter();
    private final Map<Integer, Integer> removePosition = new HashMap<Integer, Integer>();
    private int curImagePosition = -1;
    private final Logger logger = Logger.getLogger(PreviewActivity.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        logger.d("PreviewActivity#onCreate");
        setContentView(R.layout.preview_activity);
        initView();
        loadView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initView() {
        viewPager = findViewById(R.id.viewPager);
        group = findViewById(R.id.viewGroup);
        select = findViewById(R.id.select_btn);
        ImageView back = findViewById(R.id.back_btn);
        back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != adapter) {
                    for (int key : removePosition.keySet()) {
                        adapter.getSelectMap().remove(key);
                    }
                    ImageGridActivity.setAdapterSelectedMap(adapter.getSelectMap());
                    removePosition.clear();
                }
                PreviewActivity.this.finish();
            }
        });
        select.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null == adapter) {
                    return;
                }
                if (adapter.getSelectMap().containsKey(curImagePosition)) {
                    ImageItem item = adapter.getSelectMap().get(curImagePosition);
                    if (item != null) {
                        item.setSelected(!item.isSelected());
                    }
                    if (item != null && item.isSelected()) {
                        int selTotal = adapter.getSelectTotalNum();
                        adapter.setSelectTotalNum(++selTotal);
                        removePosition.remove(curImagePosition);
                        ImageGridActivity.setSendText(selTotal);
                        select.setImageResource(R.drawable.album_img_selected);
                    }
                }
            }
        });
    }

    private void loadView() {
        if (null == adapter) {
            return;
        }

        mImageViews = new ImageView[adapter.getSelectMap().size()];
        if (adapter.getSelectMap().size() > 1) {
            tips = new ImageView[adapter.getSelectMap().size()];
            for (int i = 0; i < tips.length; i++) {
                ImageView imageView = new ImageView(this);
                imageView.setLayoutParams(new LayoutParams(10, 10));
                tips[i] = imageView;
                if (i == 0) {
                    tips[i].setBackgroundResource(R.drawable.default_dot_down);
                } else {
                    tips[i].setBackgroundResource(R.drawable.default_dot_up);
                }
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
                layoutParams.leftMargin = 5;
                layoutParams.rightMargin = 5;
                group.addView(imageView, layoutParams);
            }
        }

        Iterator<?> it = adapter.getSelectMap().keySet().iterator();
        int index = -1;
        while (it.hasNext()) {
            int key = (Integer) it.next();
            ImageItem item = adapter.getSelectMap().get(key);
            ImageView imageView = new ImageView(this);
            mImageViews[++index] = imageView;
            Bitmap bmp = ImageUtil.getBigBitmapForDisplay(item.getImagePath(), PreviewActivity.this);
            if (bmp == null)
                bmp = ImageUtil.getBigBitmapForDisplay(item.getThumbnailPath(), PreviewActivity.this);
            if (bmp != null)
                imageView.setImageBitmap(bmp);
            if (index == 0) {
                curImagePosition = key;
            }
        }

        // 设置view pager
        viewPager.setAdapter(new PreviewAdapter());
        viewPager.setOnPageChangeListener(this);
        viewPager.setScanScroll(adapter.getSelectMap().size() != 1);
        viewPager.setCurrentItem(0);
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {

    }

    @Override
    public void onPageSelected(int position) {
        if (null == adapter) {
            return;
        }
        @SuppressWarnings("rawtypes")
        Iterator it = adapter.getSelectMap().keySet().iterator();
        int index = -1;
        while (it.hasNext()) {
            int key = (Integer) it.next();
            if (++index == position) {
                curImagePosition = key;// 对应适配器中图片列表的真实位置
                if (adapter.getSelectMap().get(key).isSelected()) {
                    select.setImageResource(R.drawable.album_img_selected);
                } else {
                    select.setImageResource(R.drawable.album_img_unselected);
                }
            }
        }
        setImageBackground(position);
    }

    public class PreviewAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return mImageViews.length;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object obj) {
            return view == obj;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull View container, int position) {
            try {
                ((ViewGroup) container).addView(mImageViews[position]);
            } catch (Exception e) {
            }
            return mImageViews[position];
        }
    }

    private void setImageBackground(int selectItems) {
        for (int i = 0; i < tips.length; i++) {
            if (i == selectItems) {
                tips[i].setBackgroundResource(R.drawable.default_dot_down);
            } else {
                tips[i].setBackgroundResource(R.drawable.default_dot_up);
            }
        }
    }

}
