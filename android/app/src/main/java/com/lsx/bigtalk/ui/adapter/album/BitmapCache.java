package com.lsx.bigtalk.ui.adapter.album;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.ImageView;

import com.lsx.bigtalk.ui.activity.PickPhotoActivity;
import com.lsx.bigtalk.utils.Logger;


public class BitmapCache extends Activity {
    public Handler handler = new Handler();
    public final String TAG = getClass().getSimpleName();
    private static final HashMap<String, SoftReference<Bitmap>> imageCache = new HashMap<String, SoftReference<Bitmap>>();
    private final int threadCount = Runtime.getRuntime().availableProcessors();
    private final ExecutorService executorService = Executors.newFixedThreadPool(threadCount + 1);
    private static BitmapCache instance = null;
    private final Logger logger = Logger.getLogger(BitmapCache.class);

    public static synchronized BitmapCache getInstance() {
        if (null == instance) {
            instance = new BitmapCache();
        }
        return instance;
    }

    private BitmapCache() {
    }

    private void put(String path, Bitmap bmp) {
        if (!TextUtils.isEmpty(path) && null != bmp) {
            imageCache.put(path, new SoftReference<Bitmap>(bmp));
        }
    }

    public Bitmap getCacheBitmap(String thumbPath, String sourcePath) {
        try {
            if (TextUtils.isEmpty(thumbPath) && TextUtils.isEmpty(sourcePath)) {
                return null;
            }

            final String path;
            if (!TextUtils.isEmpty(thumbPath)) {
                path = thumbPath;
            } else if (!TextUtils.isEmpty(sourcePath)) {
                path = sourcePath;
            } else {
                return null;
            }

            if (imageCache.containsKey(path)) {
                SoftReference<Bitmap> reference = imageCache.get(path);
                return reference.get();
            }
            return null;
        } catch (Exception e) {
            logger.e(e.getMessage());
            return null;
        }
    }

    public void displayBmp(final ImageView iv, final String thumbPath, final String sourcePath, final ImageCallback callback) {
        try {
            if (TextUtils.isEmpty(thumbPath) && TextUtils.isEmpty(sourcePath)) {
                return;
            }

            final String path;
            final boolean isThumbPath;
            if (!TextUtils.isEmpty(thumbPath)) {
                path = thumbPath;
                isThumbPath = true;
            } else if (!TextUtils.isEmpty(sourcePath)) {
                path = sourcePath;
                isThumbPath = false;
            } else {
                return;
            }

            iv.setImageBitmap(null);

            Runnable calculateBitmapWorker = new Runnable() {
                @Override
                public void run() {
                    Bitmap thumb = null;
                    try {
                        if (isThumbPath) {
                            thumb = BitmapFactory.decodeFile(thumbPath);
                            if (null == thumb) {
                                thumb = revisionImageSize(sourcePath);
                            }
                        } else {
                            thumb = revisionImageSize(sourcePath);
                        }
                    } catch (Exception ignored) {

                    }
                    if (null == thumb) {
                        thumb = PickPhotoActivity.bimap;
                    }
                    put(path, thumb);
                    final Bitmap bmpToCallback = thumb;
                    if (null != callback) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.imageLoad(iv, bmpToCallback, sourcePath);
                            }
                        });
                    }
                }
            };

            executorService.execute(calculateBitmapWorker);
        } catch (Exception e) {
            logger.e(e.getMessage());
        }
    }

    public Bitmap revisionImageSize(String path) throws IOException {
        BufferedInputStream in = null;
        try {
            in = new BufferedInputStream(Files.newInputStream(new File(path).toPath()));
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in, null, options);
            in.close();
            in = null;
            int i = 0;
            Bitmap bitmap = null;
            while (true) {
                if ((options.outWidth >> i <= 256)
                        && (options.outHeight >> i <= 256)) {
                    in = new BufferedInputStream(Files.newInputStream(new File(
                            path).toPath()));
                    options.inSampleSize = (int) Math.pow(2.0D, i);
                    options.inJustDecodeBounds = false;
                    bitmap = BitmapFactory.decodeStream(in, null, options);
                    break;
                }
                i += 1;
            }
            return bitmap;
        } catch (Exception e) {
            logger.e(e.getMessage());
            return null;
        } finally {
            if (null != in) {
                in.close();
                in = null;
            }
        }
    }

    public interface ImageCallback {
        void imageLoad(ImageView imageView, Bitmap bitmap, Object... params);
    }
}
