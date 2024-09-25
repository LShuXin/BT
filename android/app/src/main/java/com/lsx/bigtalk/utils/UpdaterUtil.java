package com.lsx.bigtalk.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

import com.lsx.bigtalk.R;
import com.lsx.bigtalk.logs.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class UpdaterUtil {
    private final Context mContext;
    private static Logger logger = Logger.getLogger(UpdaterUtil.class);
    private static String tag = "UpdateDetection";
    private String apkVersion;
    private String apkUrl;
    private String savePath;
    private String saveFileName;
    private int progress;
    private ProgressBar progressBar;
    private static final int ON_DOWNLOADING = 1;
    private static final int DOWNLOAD_FINISHED = 2;
    private boolean interceptFlag = false;
    private final Handler handler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ON_DOWNLOADING:
                    progressBar.setProgress(progress);
                    break;
                case DOWNLOAD_FINISHED:
                    installApk();
                    break;
                default:
                    break;
            }
        }
    };

    public UpdaterUtil(Context context) {
        this.mContext = context;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            savePath = Environment.getDownloadCacheDirectory() + "/" + "download";
            saveFileName = savePath + "/bigtalk.apk";
        }
    }

    public void checkUpdate() {
        long currentVersionCode = getCurrentVersionCode();
        long latestVersionCode = getLatestVersionCode();
        if (currentVersionCode < latestVersionCode) {
            showUpdateNoticeDialog();
        }
    }

    private long getCurrentVersionCode() {
        long versionCode = 0;

        try {
            PackageManager packageManager =  mContext.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(mContext.getPackageName(), 0);
            versionCode = packageInfo.getLongVersionCode();
        } catch (PackageManager.NameNotFoundException e) {
            logger.e(tag, e);
        }

        return versionCode;
     }

    private String getCurrentVersionName() {
        String versionName = "";

        try {
            PackageManager packageManager =  mContext.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(mContext.getPackageName(), 0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            logger.e(tag, e);
        }

        return versionName;
    }
    
    /// TODO: implement update logic
    private int getLatestVersionCode() {
        return -1;
    }
    
    private void showUpdateNoticeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("软件版本更新");
        //提示语
        String updateMsg = "有最新的软件包哦，亲快下载吧~";
        builder.setMessage(updateMsg);
        builder.setPositiveButton("下载", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                showDownloadDialog();
            }
        });
        builder.setNegativeButton("以后再说", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        Dialog noticeDialog = builder.create();
        noticeDialog.show();
    }

    private void showDownloadDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("软件版本更新");

        final LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(R.layout.update_progress_view, null);
        progressBar = v.findViewById(R.id.progress);

        builder.setView(v);
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                interceptFlag = true;
            }
        });
        Dialog downloadDialog = builder.create();
        downloadDialog.show();

        downloadApk();
    }

    private void downloadApk() {
        Thread downLoadThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(apkUrl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.connect();
                    int length = conn.getContentLength();
                    InputStream is = conn.getInputStream();

                    File file = new File(savePath);
                    if (!file.exists()) {
                        file.mkdir();
                    }
                    String apkFile = saveFileName;
                    File ApkFile = new File(apkFile);
                    FileOutputStream fos = new FileOutputStream(ApkFile);

                    int count = 0;
                    byte[] buf = new byte[1024];

                    do {
                        int numread = is.read(buf);
                        count += numread;
                        progress = (int) (((float) count / length) * 100);
                        //更新进度
                        handler.sendEmptyMessage(ON_DOWNLOADING);
                        if (numread <= 0) {
                            //下载完成通知安装
                            handler.sendEmptyMessage(DOWNLOAD_FINISHED);
                            break;
                        }
                        fos.write(buf, 0, numread);
                    } while (!interceptFlag);//点击取消就停止下载.

                    fos.close();
                    is.close();

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        downLoadThread.start();
    }

    private void installApk() {
        File apkfile = new File(saveFileName);
        if (!apkfile.exists()) {
            return;
        }
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setDataAndType(Uri.parse("file://" + apkfile), "application/vnd.android.package-archive");
        mContext.startActivity(i);
    }
}
