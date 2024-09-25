package com.lsx.bigtalk.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.lsx.bigtalk.logs.Logger;


public class YayaEmojiGridViewAdapter extends BaseAdapter {
    private Context context = null;
    private int[] emojiResourceIds = null;
    private static final Logger logger = Logger.getLogger(YayaEmojiGridViewAdapter.class);

    public YayaEmojiGridViewAdapter(Context cxt, int[] ids) {
        this.context = cxt;
        this.emojiResourceIds = ids;
    }

    @Override
    public int getCount() {
        return emojiResourceIds.length;
    }

    @Override
    public Object getItem(int position) {
        return emojiResourceIds[position];
    }

    @Override
    public long getItemId(int position) {
        return emojiResourceIds[position];
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        try {
            GridViewHolder gridViewHolder = null;
            if (null == convertView && null != context) {
                gridViewHolder = new GridViewHolder();
                convertView = gridViewHolder.layoutView;
                if (convertView != null) {
                    convertView.setTag(gridViewHolder);
                }
            } else if (convertView != null) {
                gridViewHolder = (GridViewHolder) convertView.getTag();
            }
            if (null == gridViewHolder || null == convertView) {
                return null;
            }
            gridViewHolder.emojiImageView.setImageBitmap(getBitmap(position));
            return convertView;
        } catch (Exception e) {
            logger.e(e.getMessage());
            return null;
        }
    }

    private Bitmap getBitmap(int position) {
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeResource(context.getResources(),
                    emojiResourceIds[position]);
        } catch (Exception e) {
            logger.e(e.getMessage());
        }
        return bitmap;
    }

    public class GridViewHolder {
        public LinearLayout layoutView;
        public ImageView emojiImageView;

        public GridViewHolder() {
            try {
                ViewGroup.LayoutParams layoutParams = new LayoutParams(
                        LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                layoutView = new LinearLayout(context);
                layoutView.setLayoutParams(layoutParams);
                layoutView.setOrientation(LinearLayout.VERTICAL);
                layoutView.setGravity(Gravity.CENTER);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        105,
                        115);
                params.gravity = Gravity.CENTER;

                emojiImageView = new ImageView(context);
                layoutView.addView(emojiImageView, params);
            } catch (Exception e) {
                logger.e(e.getMessage());
            }
        }
    }
}
