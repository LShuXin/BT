package com.lsx.bigtalk.imservice.service;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.TextUtils;

import com.lsx.bigtalk.DB.sp.SystemConfigSp;
import com.lsx.bigtalk.config.SysConstant;
import com.lsx.bigtalk.imservice.entity.ImageMessageEntity;
import com.lsx.bigtalk.imservice.event.MessageEvent;
import com.lsx.bigtalk.ui.helper.PhotoHelper;
import com.lsx.bigtalk.utils.FileUtil;
import com.lsx.bigtalk.utils.Logger;
import com.lsx.bigtalk.utils.HttpClient;

import java.io.File;
import java.io.IOException;

import de.greenrobot.event.EventBus;

/**
 * @author : yingmu on 15-1-12.
 * @email : yingmu@mogujie.com.
 */
public class LoadImageService extends IntentService {

    private static final Logger logger = Logger.getLogger(LoadImageService.class);

    public LoadImageService() {
        super("LoadImageService");
    }

    public LoadImageService(String name) {
        super(name);
    }

    /**
     * This method is invoked on the worker thread with a request to process.
     * Only one Intent is processed at a time, but the processing happens on a
     * worker thread that runs independently from other application logic.
     * So, if this code takes a long time, it will hold up other requests to
     * the same IntentService, but it will not hold up anything else.
     * When all requests have been handled, the IntentService stops itself,
     * so you should not call {@link #stopSelf}.
     *
     * @param intent The value passed to {@link
     *               android.content.Context#startService(android.content.Intent)}.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        ImageMessageEntity messageInfo = (ImageMessageEntity) intent.getSerializableExtra(SysConstant.UPLOAD_IMAGE_INTENT_PARAMS);
        String result = null;
        Bitmap bitmap;
        try {
            File file = new File(messageInfo.getPath());
            if (file.exists() && FileUtil.getExtensionName(messageInfo.getPath()).equalsIgnoreCase(".gif")) {
                HttpClient httpClient = new HttpClient();
                SystemConfigSp.instance().init(getApplicationContext());
                result = httpClient.uploadImage(SystemConfigSp.instance().getStrConfig(SystemConfigSp.SysCfgDimension.MSFSSERVER), FileUtil.File2byte(messageInfo.getPath()), messageInfo.getPath());
            } else {
                bitmap = PhotoHelper.revitionImage(messageInfo.getPath());
                if (null != bitmap) {
                    HttpClient httpClient = new HttpClient();
                    byte[] bytes = PhotoHelper.getBytes(bitmap);
                    result = httpClient.uploadImage(SystemConfigSp.instance().getStrConfig(SystemConfigSp.SysCfgDimension.MSFSSERVER), bytes, messageInfo.getPath());
                }
            }

            if (TextUtils.isEmpty(result)) {
                logger.i("upload image faild,cause by result is empty/null");
                EventBus.getDefault().post(new MessageEvent(MessageEvent.Event.IMAGE_UPLOAD_FAILURE
                        , messageInfo));
            } else {
                logger.i("upload image succcess,imageUrl is %s", result);
                String imageUrl = result;
                messageInfo.setUrl(imageUrl);
                EventBus.getDefault().post(new MessageEvent(
                        MessageEvent.Event.IMAGE_UPLOAD_SUCCESS
                        , messageInfo));
            }
        } catch (IOException e) {
            logger.e(e.getMessage());
        }
    }
}
