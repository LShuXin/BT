package com.lsx.bigtalk.ui.activity;

import java.io.Serializable;
import java.util.List;
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
import com.lsx.bigtalk.AppConstant;
import com.lsx.bigtalk.R;
import com.lsx.bigtalk.ui.adapter.album.AlbumHelper;
import com.lsx.bigtalk.ui.adapter.album.ImageBucket;
import com.lsx.bigtalk.ui.adapter.album.ImageBucketAdapter;
import com.lsx.bigtalk.logs.Logger;


public class ImagePickerActivity extends Activity {
    private static String TAG = "PickPhotoActivity";
    List<ImageBucket> dataList = null;
    ListView listView = null;
    ImageBucketAdapter adapter = null;
    AlbumHelper helper = null;
    TextView cancel = null;
    public static Bitmap bimap = null;
    private String currentSessionKey;
    private final Logger logger = Logger.getLogger(ImagePickerActivity.class);

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        logger.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_picker_activity);
        initData();
        initView();
    }

    /**
     * 初始化数据
     */
    private void initData() {
        Bundle bundle = getIntent().getExtras();
        if (null == bundle) {
            return;
        }
        currentSessionKey = bundle.getString(AppConstant.IntentConstant.KEY_SESSION_KEY);
        helper = AlbumHelper.getHelper(getApplicationContext());
        dataList = helper.getImagesBucketList(true);
        bimap = BitmapFactory.decodeResource(getResources(),
                R.drawable.image_image_placeholder);
    }

    private void initView() {
        listView = findViewById(R.id.list);
        adapter = new ImageBucketAdapter(this, dataList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Intent intent = new Intent(ImagePickerActivity.this,
                        ImageGridActivity.class);
                intent.putExtra(AppConstant.IntentConstant.EXTRA_IMAGE_LIST,
                        (Serializable) dataList.get(position).imageList);
                intent.putExtra(AppConstant.IntentConstant.EXTRA_ALBUM_NAME,
                        dataList.get(position).bucketName);
                intent.putExtra(AppConstant.IntentConstant.KEY_SESSION_KEY, currentSessionKey);
                startActivityForResult(intent, 1);
            }
        });
        cancel = findViewById(R.id.cancel);
        cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(Activity.RESULT_OK, null);
                ImagePickerActivity.this.finish();
                overridePendingTransition(R.anim.stay_y, R.anim.album_bottom_exit);
            }
        });

    }

}
