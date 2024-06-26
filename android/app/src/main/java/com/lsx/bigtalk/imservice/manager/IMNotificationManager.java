package com.lsx.bigtalk.imservice.manager;

import java.util.Objects;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationCompat.Builder;
import android.view.View;

import com.lsx.bigtalk.DB.entity.GroupEntity;
import com.lsx.bigtalk.DB.entity.UserEntity;
import com.lsx.bigtalk.DB.sp.ConfigurationSp;
import com.lsx.bigtalk.R;
import com.lsx.bigtalk.config.DBConstant;
import com.lsx.bigtalk.config.IntentConstant;
import com.lsx.bigtalk.config.SysConstant;
import com.lsx.bigtalk.imservice.entity.UnreadMessageEntity;
import com.lsx.bigtalk.imservice.event.GroupEvent;
import com.lsx.bigtalk.imservice.event.UnreadEvent;
import com.lsx.bigtalk.ui.activity.MessageActivity;
import com.lsx.bigtalk.helper.IMUIHelper;
import com.lsx.bigtalk.utils.Logger;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import de.greenrobot.event.EventBus;

/**
 * fake notification; unable to receive notification once the app exit
 * 通知栏新消息通知
 * a.每个session 只显示一条
 * b.每个msg 信息都显示
 * 配置依赖与 configure
 */

public class IMNotificationManager extends IMManager {
    private final Logger logger = Logger.getLogger(IMNotificationManager.class);
    private ConfigurationSp configurationSp;
	@SuppressLint("StaticFieldLeak")
    private static IMNotificationManager instance;
	public static synchronized IMNotificationManager getInstance() {
        if (null == instance) {
            instance = new IMNotificationManager();
        }
        return instance;
	}

	private IMNotificationManager() {
        
	}

    @Override
    public void doOnStart() {
        cancelAllNotifications();
    }

    public void onLoginSuccess() {
        int loginId = IMLoginManager.getInstance().getLoginId();
        configurationSp = ConfigurationSp.instance(ctx, loginId);
        if (!EventBus.getDefault().isRegistered(instance)) {
            EventBus.getDefault().register(instance);
        }
    }

    public void reset() {
        EventBus.getDefault().unregister(this);
        cancelAllNotifications();
    }
    
    public void onEventMainThread(UnreadEvent event) {
        if (event.event == UnreadEvent.Event.UNREAD_MSG_RECEIVED) {
            UnreadMessageEntity unreadEntity = event.entity;
            handleMsgReceived(unreadEntity);
        }
    }
    
    public void onEventMainThread(GroupEvent event) {
        GroupEntity gEntity = event.getGroupEntity();
        if (event.getEvent() == GroupEvent.Event.SHIELD_GROUP_OK) {
            if (gEntity == null) {
                return;
            }
            cancelSessionNotifications(gEntity.getSessionKey());
        }
    }

    private void handleMsgReceived(UnreadMessageEntity entity) {
        logger.d("IMNotificationManager#handleMsgReceived");
        int peerId = entity.getPeerId();
        int sessionType = entity.getSessionType();
        logger.d("IMNotificationManager#handleMsgReceived#peerId:%d, sessionType:%d", peerId, sessionType);

        if (entity.getIsShield()) {
           logger.d("IMNotificationManager#handleMsgReceived#SHIELD");
           return;
        }

        boolean globallyOnOff = configurationSp.getCfg(SysConstant.SETTING_GLOBAL,ConfigurationSp.CfgDimension.NOTIFICATION);
        if (globallyOnOff) {
            logger.d("notification#shouldGloballyShowNotification is false, return");
            return;
        }

        // 单独的设置
        boolean singleOnOff = configurationSp.getCfg(entity.getSessionKey(),ConfigurationSp.CfgDimension.NOTIFICATION);
        if (singleOnOff) {
            logger.d("notification#shouldShowNotificationBySession is false, return");
            return;
        }

        if (IMLoginManager.getInstance().getLoginId() != peerId) {
             showNotification(entity);
        }
    }


	public void cancelAllNotifications() {
		logger.d("IMNotificationManager#cancelAllNotifications");
        if (null == ctx) {
            return;
        }
		NotificationManager notifyMgr = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
		if (notifyMgr == null) {
			return;
		}
		notifyMgr.cancelAll();
	}

    public void cancelSessionNotifications(String sessionKey) {
        logger.d("IMNotificationManager#cancelSessionNotifications");
        NotificationManager notifyMgr = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        if (null == notifyMgr) {
            return;
        }
        int notificationId = getSessionNotificationId(sessionKey);
        notifyMgr.cancel(notificationId);
    }

	private void showNotification(final UnreadMessageEntity unreadEntity) {
		// todo eric need to set the exact size of the big icon
        // 服务端有些特定的支持 尺寸是不是要调整一下 todo 100*100  下面的就可以不要了
		ImageSize targetSize = new ImageSize(80, 80);
        int peerId = unreadEntity.getPeerId();
        int sessionType = unreadEntity.getSessionType();
        String avatarUrl = "";
        String title = "";
        String content = unreadEntity.getLatestMsgData();
        String unit = ctx.getString(R.string.msg_cnt_unit);
        int totalUnread = unreadEntity.getUnReadCnt();

        if(unreadEntity.getSessionType() == DBConstant.SESSION_TYPE_SINGLE){
            UserEntity contact = IMContactManager.getInstance().findContact(peerId);
            if(contact !=null){
                title = contact.getMainName();
                avatarUrl = contact.getAvatar();
            }else{
                title = "User_"+peerId;
                avatarUrl = "";
            }

        }else{
            GroupEntity group = IMGroupManager.getInstance().findGroup(peerId);
            if(group !=null){
                title = group.getMainName();
                avatarUrl = group.getAvatar();
            }else{
                title = "Group_"+peerId;
                avatarUrl = "";
            }
        }
        //获取头像
		avatarUrl = IMUIHelper.getRealAvatarUrl(avatarUrl);
        final String ticker = String.format("[%d%s]%s: %s", totalUnread, unit, title, content);
        final int notificationId = getSessionNotificationId(unreadEntity.getSessionKey());
        final Intent intent = new Intent(ctx, MessageActivity.class);
        intent.putExtra(IntentConstant.KEY_SESSION_KEY, unreadEntity.getSessionKey());

        logger.d("notification#notification avatarUrl:%s", avatarUrl);
        final String finalTitle = title;
        ImageLoader.getInstance().loadImage(avatarUrl, targetSize, null, new SimpleImageLoadingListener() {

            @Override
            public void onLoadingComplete(String imageUri, View view,
                                          Bitmap loadedImage) {
                logger.d("notification#icon onLoadComplete");
                // holder.image.setImageBitmap(loadedImage);
                showInNotificationBar(finalTitle,ticker,loadedImage,notificationId,intent);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view,
                                        FailReason failReason) {
                logger.d("notification#icon onLoadFailed");
                // 服务器支持的格式有哪些
                // todo eric default avatar is too small, need big size(128 * 128)
                Bitmap defaultBitmap = BitmapFactory.decodeResource(ctx.getResources(), IMUIHelper.getDefaultAvatarResId(unreadEntity.getSessionType()));
                showInNotificationBar(finalTitle,ticker,defaultBitmap,notificationId,intent);
            }
        });
	}

	private void showInNotificationBar(String title,String ticker, Bitmap iconBitmap,int notificationId,Intent intent) {
		logger.d("notification#showInNotificationBar title:%s ticker:%s",title,ticker);

		NotificationManager notifyMgr = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
		if (notifyMgr == null) {
			return;
		}

		Builder builder = new NotificationCompat.Builder(ctx);
		builder.setContentTitle(title);
		builder.setContentText(ticker);
		builder.setSmallIcon(R.drawable.small_brand);
		builder.setTicker(ticker);
		builder.setWhen(System.currentTimeMillis());
		builder.setAutoCancel(true);

		// this is the content near the right bottom side
		// builder.setContentInfo("content info");

		if (configurationSp.getCfg(SysConstant.SETTING_GLOBAL,ConfigurationSp.CfgDimension.VIBRATION)) {
			// delay 0ms, vibrate 200ms, delay 250ms, vibrate 200ms
			long[] vibrate = {0, 200, 250, 200};
			builder.setVibrate(vibrate);
		} else {
			logger.d("notification#setting is not using vibration");
		}

		// sound
		if (configurationSp.getCfg(SysConstant.SETTING_GLOBAL,ConfigurationSp.CfgDimension.SOUND)) {
			builder.setDefaults(Notification.DEFAULT_SOUND);
		} else {
			logger.d("notification#setting is not using sound");
		}
		if (iconBitmap != null) {
			logger.d("notification#fetch icon from network ok");
			builder.setLargeIcon(iconBitmap);
		} else {
            // do nothint ?
		}
		// if MessageActivity is in the background, the system would bring it to
		// the front
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent pendingIntent = PendingIntent.getActivity(ctx, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(pendingIntent);
		Notification notification = builder.build();
		notifyMgr.notify(notificationId, notification);
	}

	// come from
	// http://www.partow.net/programming/hashfunctions/index.html#BKDRHashFunction
	private long hashBKDR(String str) {
		long seed = 131; // 31 131 1313 13131 131313 etc..
		long hash = 0;

		for (int i = 0; i < str.length(); i++) {
			hash = (hash * seed) + str.charAt(i);
		}
		return hash;
	}

	/* End Of BKDR Hash Function */
	public int getSessionNotificationId(String sessionKey) {
		logger.d("notification#getSessionNotificationId sessionTag:%s", sessionKey);
		int hashedNotificationId = (int) hashBKDR(sessionKey);
		logger.d("notification#hashedNotificationId:%d", hashedNotificationId);
		return hashedNotificationId;
	}
}
