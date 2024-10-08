package com.lsx.bigtalk.ui.adapter.album;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import com.lsx.bigtalk.AppConstant;
import com.lsx.bigtalk.R;
import com.lsx.bigtalk.ui.adapter.album.BitmapCache.ImageCallback;

import com.lsx.bigtalk.ui.activity.ImagePickerActivity;
import com.lsx.bigtalk.logs.Logger;


public class ImageGridAdapter extends BaseAdapter {
    private TextCallback textcallback = null;
    private Activity activity = null;
    private List<ImageItem> dataList = null;
    private Map<Integer, ImageItem> selectedMap = new TreeMap<Integer, ImageItem>();
    private BitmapCache cache = null;
    private Handler mHandler = null;
    private int selectTotal = 0;
    private final Logger logger = Logger.getLogger(ImageGridAdapter.class);
    private boolean allowLoad = true;

    ImageCallback callback = new ImageCallback() {
        @Override
        public void imageLoad(ImageView imageView, Bitmap bitmap, Object... params) {
            try {
                if (null != imageView && null != bitmap) {
                    String url = (String) params[0];
                    if (null != url && url.equals(imageView.getTag())) {
                        imageView.setImageBitmap(bitmap);
                    } else {
                        logger.e("callback, bmp not match");
                    }
                } else {
                    logger.e("callback, bmp null");
                }
            } catch (Exception e) {
                logger.e(e.getMessage());
            }
        }
    };

    public void lock() {
        this.allowLoad = false;
        notifyDataSetChanged();
    }

    public void unlock() {
        this.allowLoad = true;
        notifyDataSetChanged();
    }

    public ImageGridAdapter(Activity act, List<ImageItem> list, Handler handler) {
        activity = act;
        cache = BitmapCache.getInstance();
        dataList = list;
        mHandler = handler;
    }

    @Override
    public int getCount() {
        int count = 0;
        if (null != dataList) {
            count = dataList.size();
        }
        return count;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void updateSelectedStatus(int position, boolean selected) {
        if (null != dataList) {
            ImageItem item = dataList.get(position);
            item.setSelected(selected);
        }
    }

    public void setSelectTotalNum(int num) {
        selectTotal = num;
    }

    public int getSelectTotalNum() {
        return selectTotal;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Holder holder = null;
        try {
            if (null == convertView) {
                holder = new Holder();
                convertView = View.inflate(activity, R.layout.gridview_image_item_view,
                        null);
                holder.iv = convertView.findViewById(R.id.image);
                holder.selected = convertView
                        .findViewById(R.id.selectIcon);
                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }

            setHolder(holder, position);

            if (getCount() - 1 == position) {
                convertView.setPadding(0, 0, 0, 30);
            } else {
                convertView.setPadding(0, 0, 0, 0);
            }

            return convertView;
        } catch (Exception e) {
            logger.e(e.getMessage());
            return null;
        }
    }

    private void setHolder(final Holder holder, final int position) {
        try {
            final ImageItem item = dataList.get(position);
            holder.iv.setTag(item.getImagePath());

            Bitmap bmp = cache.getCacheBitmap(item.getThumbnailPath(),
                    item.getImagePath());
            if (null != bmp) {
                holder.iv.setImageBitmap(bmp);
            } else {
                if (allowLoad) {
                    cache.displayBmp(holder.iv, item.getThumbnailPath(),
                            item.getImagePath(), callback);
                } else {
                    holder.iv
                            .setImageResource(R.drawable.image_image_placeholder);
                }
            }

            if (item.isSelected()) {
                holder.selected.setImageResource(R.drawable.ic_selected);
            } else {
                holder.selected
                        .setImageResource(R.drawable.ic_unselected);
            }
            holder.iv.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    String path = dataList.get(position).getImagePath();
                    Bitmap bmp = cache.getCacheBitmap(path, path);
                    if (null != bmp && bmp == ImagePickerActivity.bimap) {
                        Toast.makeText(
                                activity,
                                activity.getResources().getString(
                                        R.string.unavailable_image_file),
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (selectTotal < AppConstant.SysConstant.MAX_SELECT_IMAGE_COUNT) {
                        item.setSelected(!item.isSelected());
                        if (item.isSelected()) {
                            holder.selected
                                    .setImageResource(R.drawable.ic_selected);
                            selectTotal++;
                            if (null != textcallback)
                                textcallback.onListen(selectTotal);
                            selectedMap.put(position, dataList.get(position));

                        } else if (!item.isSelected()) {
                            holder.selected
                                    .setImageResource(R.drawable.ic_unselected);
                            selectTotal--;
                            if (null != textcallback)
                                textcallback.onListen(selectTotal);
                            selectedMap.remove(position);
                        }
                    } else if (selectTotal >= AppConstant.SysConstant.MAX_SELECT_IMAGE_COUNT) {
                        if (item.isSelected()) {
                            item.setSelected(!item.isSelected());
                            holder.selected
                                    .setImageResource(R.drawable.ic_unselected);
                            selectTotal--;
                            selectedMap.remove(position);
                        } else {
                            Message message = Message.obtain(mHandler, 0);
                            message.sendToTarget();
                        }
                    }
                }

            });
        } catch (Exception e) {
            logger.e(e.getMessage());
        }
    }

    public Map<Integer, ImageItem> getSelectMap() {
        return selectedMap;
    }

    public void setSelectMap(Map<Integer, ImageItem> map) {
        if (null == map) {
            selectedMap.clear();
            return;
        }
        selectedMap = map;
    }

    public void setTextCallback(TextCallback listener) {
        textcallback = listener;
    }

    static class Holder {
        private ImageView iv;
        private ImageView selected;
    }

    public interface TextCallback {
        void onListen(int count);
    }

}
