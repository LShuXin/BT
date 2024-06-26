package com.lsx.bigtalk.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.lsx.bigtalk.R;
import com.lsx.bigtalk.config.IntentConstant;
import com.lsx.bigtalk.ui.adapter.album.AlbumHelper;
import com.lsx.bigtalk.ui.adapter.album.ImageBucket;
import com.lsx.bigtalk.ui.adapter.album.ImageBucketAdapter;
import com.lsx.bigtalk.utils.Logger;

import java.io.Serializable;
import java.util.List;


public class PickPhotoActivity extends Activity {
    List<ImageBucket> dataList = null;
    ListView listView = null;
    ImageBucketAdapter adapter = null;
    AlbumHelper helper = null;
    TextView cancel = null;
    public static Bitmap bimap = null;
    boolean touchable = true;
    private String currentSessionKey;
    private final Logger logger = Logger.getLogger(PickPhotoActivity.class);

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            this.finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        logger.d("pic#PickPhotoActivity onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pick_photo_activity);
        initData();
        initView();
    }

    /**
     * 初始化数据
     */
    private void initData() {
        Bundle bundle = getIntent().getExtras();
        currentSessionKey = bundle.getString(IntentConstant.KEY_SESSION_KEY);
        helper = AlbumHelper.getHelper(getApplicationContext());
        dataList = helper.getImagesBucketList(true);
        bimap = BitmapFactory.decodeResource(getResources(),
                R.drawable.default_album_grid_image);
    }

    /**
     * 初始化view
     */
    private void initView() {
        listView = findViewById(R.id.list);
        adapter = new ImageBucketAdapter(this, dataList);

        listView.setAdapter(adapter);
//        listView.setOnTouchListener(this);

        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Intent intent = new Intent(PickPhotoActivity.this,
                        ImageGridActivity.class);
                intent.putExtra(IntentConstant.EXTRA_IMAGE_LIST,
                        (Serializable) dataList.get(position).imageList);
                intent.putExtra(IntentConstant.EXTRA_ALBUM_NAME,
                        dataList.get(position).bucketName);
                intent.putExtra(IntentConstant.KEY_SESSION_KEY, currentSessionKey);
                startActivityForResult(intent, 1);//requestcode》＝0
//                PickPhotoActivity.this.finish();
            }
        });
        cancel = findViewById(R.id.cancel);
        cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(Activity.RESULT_OK, null);
                PickPhotoActivity.this.finish();
                overridePendingTransition(R.anim.stay_y, R.anim.album_bottom_exit);
            }
        });

    }

}
