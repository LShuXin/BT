package com.lsx.bigtalk.ui.activity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import de.greenrobot.event.EventBus;
import com.lsx.bigtalk.AppConstant;
import com.lsx.bigtalk.R;
import com.lsx.bigtalk.ui.adapter.album.ImageGridAdapter;
import com.lsx.bigtalk.ui.adapter.album.ImageGridAdapter.TextCallback;
import com.lsx.bigtalk.ui.adapter.album.ImageItem;
import com.lsx.bigtalk.service.event.ImageSelectEvent;
import com.lsx.bigtalk.service.service.IMService;
import com.lsx.bigtalk.service.support.IMServiceConnector;
import com.lsx.bigtalk.logs.Logger;


public class ImageGridActivity extends Activity implements OnTouchListener {
    private static WeakReference<ImageGridActivity> imageGridActivityWeakRef = null;
    private List<ImageItem> dataList = null;
    private GridView gridView = null;
    private TextView finish = null;
    private String name = null;
    private ImageGridAdapter adapter = null;
	private final Logger logger = Logger.getLogger(ImageGridActivity.class);
    private final IMServiceConnector imServiceConnector = new IMServiceConnector() {
        @Override
        public void onIMServiceConnected() {
            IMService imService = imServiceConnector.getIMService();
            if (imService == null) {
                throw new RuntimeException("#connect imservice success,but is null");
            }
        }

        @Override
        public void onServiceDisconnected() {

        }
    };

    OnScrollListener onScrollListener = new OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            switch (scrollState) {
                case OnScrollListener.SCROLL_STATE_FLING:
                case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                    adapter.lock();
                    break;
                case OnScrollListener.SCROLL_STATE_IDLE:
                    adapter.unlock();
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                int visibleItemCount, int totalItemCount) {
        }
    };

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imServiceConnector.connect(this);
        setContentView(R.layout.image_gridview_activity);
        imageGridActivityWeakRef = new WeakReference<>(this);
        name = (String) getIntent().getSerializableExtra(
                AppConstant.IntentConstant.EXTRA_ALBUM_NAME);
        dataList = (List<ImageItem>) getIntent().getSerializableExtra(
                AppConstant.IntentConstant.EXTRA_IMAGE_LIST);
        initView();
        initAdapter();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        setAdapterSelectedMap(null);
        imServiceConnector.disconnect(this);
        super.onStop();
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                adapter.unlock();
                break;
            case MotionEvent.ACTION_UP:
                view.performClick();
                break;
            default:
                break;
        }
        return false;
    }

    private void initView() {
        gridView = findViewById(R.id.gridview);
        gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        gridView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                adapter.notifyDataSetChanged();
            }
        });

        TextView title = findViewById(R.id.base_fragment_title);
        if (name.length() > 12) {
            name = name.substring(0, 11) + "...";
        }
        title.setText(name);

        ImageView leftBtn = findViewById(R.id.back_btn);
        leftBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageGridActivity.this.finish();
            }
        });

        TextView cancel = findViewById(R.id.cancel);
        cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.setSelectMap(null);
                ImageGridActivity.this.finish();
            }
        });

        finish = findViewById(R.id.finish);
        finish.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	logger.d("pic#click send image btn");
                if (!adapter.getSelectMap().isEmpty()) {
                    List<ImageItem> itemList = new ArrayList<>();

                    for (int position : adapter.getSelectMap().keySet()) {
                        ImageItem imgItem = adapter.getSelectMap()
                                .get(position);
                        itemList.add(imgItem);
                    }

                    setSendText(0);
                    EventBus.getDefault().post(new ImageSelectEvent(itemList));
                    ImageGridActivity.this.setResult(RESULT_OK, null);
                    ImageGridActivity.this.finish();
                } else {
                    Toast.makeText(ImageGridActivity.this,
                            R.string.need_choose_images, Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });
        TextView preview = findViewById(R.id.preview);
        preview.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (!adapter.getSelectMap().isEmpty()) {
                    Intent intent = new Intent(ImageGridActivity.this,
                            PreviewActivity.class);
                    startActivityForResult(intent,
                            AppConstant.SysConstant.IMAGE_PREVIEW_FROM_ALBUM);
                } else {
                    Toast.makeText(ImageGridActivity.this,
                            R.string.need_choose_images, Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });
    }

    private void initAdapter() {
        adapter = new ImageGridAdapter(ImageGridActivity.this, dataList, new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                if (msg.what == 0) {
                    Toast.makeText(ImageGridActivity.this,
                            "最多选择" + AppConstant.SysConstant.MAX_SELECT_IMAGE_COUNT + "张图片",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
        adapter.setTextCallback(new TextCallback() {
            public void onListen(int count) {
                setSendText(count);
            }
        });
        gridView.setAdapter(adapter);
        gridView.setOnScrollListener(onScrollListener);
    }

    @SuppressLint("SetTextI18n")
    public static void setSendText(int selNum) {
        if (null == getActivity()) {
            return;
        }
        if (selNum == 0) {
            getActivity().finish.setText(getActivity().getResources().getString(R.string.send));
        } else {
            getActivity().finish.setText(getActivity().getResources().getString(R.string.send)
                    + "(" + selNum + ")");
        }
    }

    public static void setAdapterSelectedMap(Map<Integer, ImageItem> map) {
        if (null == getActivity()) {
            return;
        }
        Iterator<Integer> it = getActivity().adapter.getSelectMap().keySet().iterator();
        if (map != null) {
            while (it.hasNext()) {
                int key = it.next();
                getActivity().adapter.updateSelectedStatus(key, map.containsKey(key));
            }
            getActivity().adapter.setSelectMap(map);
            getActivity().adapter.setSelectTotalNum(map.size());
        } else {
            while (it.hasNext()) {
                int key = it.next();
                getActivity().adapter.updateSelectedStatus(key, false);
            }
            getActivity().adapter.setSelectMap(null);
            getActivity().adapter.setSelectTotalNum(0);
        }
        getActivity().adapter.notifyDataSetChanged();
    }

    public static ImageGridAdapter getAdapter() {
        if (null == getActivity()) {
            return null;
        }
        return getActivity().adapter;
    }

    static ImageGridActivity getActivity() {
        if (null != imageGridActivityWeakRef) {
            return imageGridActivityWeakRef.get();
        }
        return null;
    }
}
