package com.lsx.bigtalk.ui.helper;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import androidx.annotation.RequiresApi;


import com.lsx.bigtalk.AppConstant;
import com.lsx.bigtalk.service.event.LoginEvent;
import com.lsx.bigtalk.service.support.SearchElement;
import com.lsx.bigtalk.storage.db.entity.DepartmentEntity;
import com.lsx.bigtalk.storage.db.entity.GroupEntity;
import com.lsx.bigtalk.storage.db.entity.UserEntity;
import com.lsx.bigtalk.R;

import com.lsx.bigtalk.service.event.SocketEvent;
import com.lsx.bigtalk.ui.activity.GroupMemberSelectActivity;
import com.lsx.bigtalk.ui.activity.MessageActivity;
import com.lsx.bigtalk.ui.activity.UserInfoActivity;
import com.lsx.bigtalk.logs.Logger;
import com.lsx.bigtalk.utils.pinyin.PinYin.PinYinElement;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;


public class IMUIHelper {
	
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
	public static void handleMsgContactItemLongPressed(final UserEntity contact, final Context ctx) {
        if (contact == null || ctx == null) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(ctx, android.R.style.Theme_Holo_Light_Dialog));
        builder.setTitle(contact.getMainName());
        String[] items = new String[] {
				ctx.getString(R.string.check_profile),
                ctx.getString(R.string.start_session)};

        final int peerUserId = contact.getPeerId();
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        IMUIHelper.openUserProfileActivity(ctx, peerUserId);
                        break;
                    case 1:
                        IMUIHelper.openChatActivity(ctx, contact.getSessionKey());
                        break;
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();
    }

    public static int getLoginErrorTip(LoginEvent event) {
        switch (event) {
            case LOGIN_AUTH_FAILED:
                return R.string.login_error_general_failed;
            case LOGIN_INNER_FAILED:
                return R.string.login_error_unexpected;
            default:
                return R.string.login_error_unexpected;
        }
    }

    public static int getSocketErrorTip(SocketEvent event) {
        switch (event) {
            case CONNECT_MSG_SERVER_FAILED :
                return R.string.connect_msg_server_failed;
            default:
                return  R.string.login_error_unexpected;
        }
    }

    public static void openChatActivity(Context ctx, String sessionKey) {
        Intent intent = new Intent(ctx, MessageActivity.class);
        intent.putExtra(AppConstant.IntentConstant.KEY_SESSION_KEY, sessionKey);
        ctx.startActivity(intent);
    }

    public static void openUserProfileActivity(Context ctx, int contactId) {
        Intent intent = new Intent(ctx, UserInfoActivity.class);
        intent.putExtra(AppConstant.IntentConstant.KEY_PEER_ID, contactId);
        ctx.startActivity(intent);
    }

    public static void openGroupMemberSelectActivity(Context ctx, String sessionKey) {
        Intent intent = new Intent(ctx, GroupMemberSelectActivity.class);
        intent.putExtra(AppConstant.IntentConstant.KEY_SESSION_KEY, sessionKey);
        ctx.startActivity(intent);
    }

    public interface dialogCallback {
        void callback();
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
	public static void showCustomDialog(Context context, int visible, String title, final dialogCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(context, android.R.style.Theme_Holo_Light_Dialog));
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialog_view = inflater.inflate(R.layout.edit_dialog_view, null);
        final EditText editText = dialog_view.findViewById(R.id.dialog_edit_text);
        editText.setVisibility(visible);
        TextView textText = dialog_view.findViewById(R.id.dialog_title);
        textText.setText(title);
        builder.setView(dialog_view);

        builder.setPositiveButton(context.getString(R.string.tt_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callback.callback();
                dialog.dismiss();
            }
        });

        builder.setNegativeButton(context.getString(R.string.tt_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    public static void callPhone(Context ctx, String phoneNumber) {
        if (ctx == null) {
            return;
        }
        if (phoneNumber == null || TextUtils.isEmpty(phoneNumber)) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"
                + phoneNumber));

        ctx.startActivity(intent);
    }

    // 文字高亮显示
    public static void setTextHighlighted(TextView textView, String text, SearchElement searchElement) {
        textView.setText(text);
        if (TextUtils.isEmpty(text) || searchElement == null) {
            return;
        }

        int startIndex = searchElement.startIndex;
        int endIndex = searchElement.endIndex;
        if (startIndex < 0 || endIndex > text.length()) {
            return;
        }
        int color = Color.rgb(69, 192, 26);
        textView.setText(text, BufferType.SPANNABLE);
        Spannable span = (Spannable) textView.getText();
        span.setSpan(new ForegroundColorSpan(color), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    public static String getRealAvatarUrl(String avatarUrl) {
        if (avatarUrl.toLowerCase().contains("http")) {
            return avatarUrl;
        } else if (TextUtils.isEmpty(avatarUrl.trim())) {
            return "";
        } else {
            return AppConstant.UrlConstant.AVATAR_URL_PREFIX + avatarUrl;
        }
    }

	public static boolean handleDepartmentSearch(String key, DepartmentEntity department) {
		if (TextUtils.isEmpty(key) || department == null) {
			return false;
		}
		department.getSearchElement().reset();

		return handleTokenFirstCharsSearch(key, department.getPinyinElement(), department.getSearchElement())
		|| handleTokenPinyinFullSearch(key, department.getPinyinElement(), department.getSearchElement())
		|| handleNameSearch(department.getDepartName(), key, department.getSearchElement());
	}


	public static boolean handleGroupSearch(String key, GroupEntity group) {
		if (TextUtils.isEmpty(key) || group == null) {
			return false;
		}
		group.getSearchElement().reset();

		return handleTokenFirstCharsSearch(key, group.getPinyinElement(), group.getSearchElement())
		|| handleTokenPinyinFullSearch(key, group.getPinyinElement(), group.getSearchElement())
		|| handleNameSearch(group.getMainName(), key, group.getSearchElement());
	}

	public static boolean handleContactSearch(String key, UserEntity contact) {
		if (TextUtils.isEmpty(key) || contact == null) {
			return false;
		}

		contact.getSearchElement().reset();

		return handleTokenFirstCharsSearch(key, contact.getPinyinElement(), contact.getSearchElement())
		|| handleTokenPinyinFullSearch(key, contact.getPinyinElement(), contact.getSearchElement())
		|| handleNameSearch(contact.getMainName(), key, contact.getSearchElement());
        // 原先是 contact.name 代表花名的意思嘛??
	}

	public static boolean handleNameSearch(String name, String key,
			SearchElement searchElement) {
		int index = name.indexOf(key);
		if (index == -1) {
			return false;
		}

		searchElement.startIndex = index;
		searchElement.endIndex = index + key.length();

		return true;
	}

	public static boolean handleTokenFirstCharsSearch(String key, PinYinElement pinYinElement, SearchElement searchElement) {
		return handleNameSearch(pinYinElement.tokenFirstChars, key.toUpperCase(), searchElement);
	}

	public static boolean handleTokenPinyinFullSearch(String key, PinYinElement pinYinElement, SearchElement searchElement) {
		if (TextUtils.isEmpty(key)) {
			return false;
		}

		String searchKey = key.toUpperCase();

		//onLoginOut the old search result
		searchElement.reset();

		int tokenCnt = pinYinElement.tokenPinyinList.size();
		int startIndex = -1;
		int endIndex = -1;

		for (int i = 0; i < tokenCnt; ++i) {
			String tokenPinyin = pinYinElement.tokenPinyinList.get(i);

			int tokenPinyinSize = tokenPinyin.length();
			int searchKeySize = searchKey.length();

			int keyCnt = Math.min(searchKeySize, tokenPinyinSize);
			String keyPart = searchKey.substring(0, keyCnt);

			if (tokenPinyin.startsWith(keyPart)) {

				if (startIndex == -1) {
					startIndex = i;
				}

				endIndex = i + 1;
			} else {
				continue;
			}

			if (searchKeySize <= tokenPinyinSize) {
				searchKey = "";
				break;
			}

			searchKey = searchKey.substring(keyCnt, searchKeySize);
		}

		if (!searchKey.isEmpty()) {
			return false;
		}

		if (startIndex >= 0) {
			searchElement.startIndex = startIndex;
			searchElement.endIndex = endIndex;

			return true;
		}

		return false;
	}

    // search helper end



	public static void setViewTouchHightlighted(final View view) {
		if (view == null) {
			return;
		}

		view.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					view.setBackgroundColor(Color.rgb(1, 175, 244));
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					view.setBackgroundColor(Color.rgb(255, 255, 255));
				}
				return false;
			}
		});
	}





    // 这个还是蛮有用的,方便以后的替换
	public static int getDefaultAvatarResId(int sessionType) {
		if (sessionType == AppConstant.DBConstant.SESSION_TYPE_SINGLE) {
			return R.drawable.image_default_user_avatar;
		} else if (sessionType == AppConstant.DBConstant.SESSION_TYPE_GROUP) {
			return R.drawable.image_group_avatar;
		} else if (sessionType == AppConstant.DBConstant.SESSION_TYPE_GROUP) {
			return R.drawable.image_default_group_avatar;
		}

		return R.drawable.image_default_user_avatar;
	}


	public static void setEntityImageViewAvatarNoDefaultPortrait(ImageView imageView,
			String avatarUrl, int sessionType, int roundPixel) {
		setEntityImageViewAvatarImpl(imageView, avatarUrl, sessionType, false, roundPixel);
	}

	public static void setEntityImageViewAvatarImpl(ImageView imageView,
			String avatarUrl, int sessionType, boolean showDefaultPortrait, int roundPixel) {
		if (avatarUrl == null) {
			avatarUrl = "";
		}

		String fullAvatar = getRealAvatarUrl(avatarUrl);
		int defaultResId = -1;

		if (showDefaultPortrait) {
			defaultResId = getDefaultAvatarResId(sessionType);
		}

		displayImage(imageView, fullAvatar, defaultResId, roundPixel);
	}

	public static void displayImage(ImageView imageView, String resourceUri, int defaultResId, int roundPixel) {
		Logger logger = Logger.getLogger(IMUIHelper.class);
		logger.d("displayimage#displayImage resourceUri:%s, defeaultResourceId:%d", resourceUri, defaultResId);

		if (resourceUri == null) {
			resourceUri = "";
		}

		boolean showDefaultImage = !(defaultResId <= 0);

		if (TextUtils.isEmpty(resourceUri) && !showDefaultImage) {
			logger.e("displayimage#, unable to display image");
			return;
		}


		DisplayImageOptions options;
		if (showDefaultImage) {
			options = new DisplayImageOptions.Builder().
			showImageOnLoading(defaultResId).
			showImageForEmptyUri(defaultResId).
			showImageOnFail(defaultResId).
			cacheInMemory(true).
			cacheOnDisk(true).
			considerExifParams(true).
			displayer(new RoundedBitmapDisplayer(roundPixel)).
			imageScaleType(ImageScaleType.EXACTLY).// 改善OOM
			bitmapConfig(Bitmap.Config.RGB_565).// 改善OOM
			build();
		} else {
			options = new DisplayImageOptions.Builder().
			cacheInMemory(true).
			cacheOnDisk(true).
//			considerExifParams(true).
//			displayer(new RoundedBitmapDisplayer(roundPixel)).
//			imageScaleType(ImageScaleType.EXACTLY).// 改善OOM
//			bitmapConfig(Bitmap.Config.RGB_565).// 改善OOM
			build();
		}

		ImageLoader.getInstance().displayImage(resourceUri, imageView, options, null);
	}



    public static void displayImageNoOptions(ImageView imageView,
                                    String resourceUri, int defaultResId, int roundPixel) {

        Logger logger = Logger.getLogger(IMUIHelper.class);

        logger.d("displayimage#displayImage resourceUri:%s, defeaultResourceId:%d", resourceUri, defaultResId);

        if (resourceUri == null) {
            resourceUri = "";
        }

        boolean showDefaultImage = !(defaultResId <= 0);

        if (TextUtils.isEmpty(resourceUri) && !showDefaultImage) {
            logger.e("displayimage#, unable to display image");
            return;
        }

        DisplayImageOptions options;
        if (showDefaultImage) {
            options = new DisplayImageOptions.Builder().
                    showImageOnLoading(defaultResId).
                    showImageForEmptyUri(defaultResId).
                    showImageOnFail(defaultResId).
                    cacheInMemory(true).
                    cacheOnDisk(true).
                    considerExifParams(true).
                    displayer(new RoundedBitmapDisplayer(roundPixel)).
                    imageScaleType(ImageScaleType.EXACTLY).// 改善OOM
                    bitmapConfig(Bitmap.Config.RGB_565).// 改善OOM
                    build();
        } else {
            options = new DisplayImageOptions.Builder().
//                    cacheInMemory(true).
//                    cacheOnDisk(true).
                    imageScaleType(ImageScaleType.EXACTLY).// 改善OOM
                    bitmapConfig(Bitmap.Config.RGB_565).// 改善OOM
        build();
        }
        ImageLoader.getInstance().displayImage(resourceUri, imageView, options, null);
    }

}
