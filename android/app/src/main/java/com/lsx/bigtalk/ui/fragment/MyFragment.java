package com.lsx.bigtalk.ui.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.lsx.bigtalk.DB.entity.UserEntity;
import com.lsx.bigtalk.R;
import com.lsx.bigtalk.config.SysConstant;
import com.lsx.bigtalk.imservice.event.UserInfoEvent;
import com.lsx.bigtalk.imservice.manager.IMLoginManager;
import com.lsx.bigtalk.imservice.service.IMService;
import com.lsx.bigtalk.imservice.support.IMServiceConnector;
import com.lsx.bigtalk.ui.activity.SettingActivity;
import com.lsx.bigtalk.ui.widget.IMBaseImageView;
import com.lsx.bigtalk.utils.FileUtil;
import com.lsx.bigtalk.helper.IMUIHelper;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.util.Objects;

import de.greenrobot.event.EventBus;


public class MyFragment extends MainFragment {
    private View curView = null;
    private View contentView;

    private final IMServiceConnector imServiceConnector = new IMServiceConnector() {
        @Override
        public void onServiceDisconnected() {

        }

        @Override
        public void onIMServiceConnected() {
            if (curView == null) {
                return;
            }
            IMService imService = imServiceConnector.getIMService();
            if (imService == null) {
                return;
            }
            if (!imService.getIMContactManager().getIsContactDataReady()) {
                logger.i("detail#contact data are not ready");
            } else {
                init(imService);
            }
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        imServiceConnector.connect(getActivity());
        EventBus.getDefault().register(this);

        if (null != curView) {
            ((ViewGroup) curView.getParent()).removeView(curView);
            return curView;
        }
        curView = inflater.inflate(R.layout.mine_fragment, baseFragmentLayout);

        initRes();

        return curView;
    }

    private void initRes() {
        super.init(curView);

        contentView = curView.findViewById(R.id.content);
        View exitView = curView.findViewById(R.id.exitPage);
        View clearView = curView.findViewById(R.id.clearPage);
        View settingView = curView.findViewById(R.id.settingPage);

        clearView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), android.R.style.Theme_Holo_Light_Dialog));
                LayoutInflater inflater = (LayoutInflater) requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View dialog_view = inflater.inflate(R.layout.custom_dialog, null);
                final EditText editText = dialog_view.findViewById(R.id.dialog_edit_content);
                editText.setVisibility(View.GONE);
                TextView textText = dialog_view.findViewById(R.id.dialog_title);
                textText.setText(R.string.clear_cache_tip);
                builder.setView(dialog_view);

                builder.setPositiveButton(getString(R.string.tt_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ImageLoader.getInstance().clearMemoryCache();
                        ImageLoader.getInstance().clearDiskCache();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                FileUtil.deleteHistoryFiles(new File(Environment.getExternalStorageDirectory().toString()
                                        + File.separator + "MGJ-IM" + File.separator), System.currentTimeMillis());
                                Toast toast = Toast.makeText(getActivity(), R.string.thumb_remove_finish, Toast.LENGTH_LONG);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();
                            }
                        }, 500);

                        dialog.dismiss();
                    }
                });

                builder.setNegativeButton(getString(R.string.tt_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.show();
            }
        });

        exitView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), android.R.style.Theme_Holo_Light_Dialog));
                LayoutInflater inflater = (LayoutInflater) requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View dialog_view = inflater.inflate(R.layout.custom_dialog, null);
                final EditText editText = dialog_view.findViewById(R.id.dialog_edit_content);
                editText.setVisibility(View.GONE);
                TextView textText = dialog_view.findViewById(R.id.dialog_title);
                textText.setText(R.string.exit_teamtalk_tip);
                builder.setView(dialog_view);
                builder.setPositiveButton(getString(R.string.tt_ok), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        IMLoginManager.getInstance().setIsKickedOut(false);
                        IMLoginManager.getInstance().logOut();
                        getActivity().finish();
                        dialog.dismiss();
                    }
                });

                builder.setNegativeButton(getString(R.string.tt_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.show();

            }
        });

        settingView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MyFragment.this.getActivity(), SettingActivity.class));
            }
        });

        hideContent();

        setTopCenterTitleText(requireActivity().getString(R.string.main_me_tab));
    }

    private void hideContent() {
        if (contentView != null) {
            contentView.setVisibility(View.GONE);
        }
    }

    private void showContent() {
        if (contentView != null) {
            contentView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        imServiceConnector.disconnect(getActivity());
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void initHandler() {

    }

    public void onEventMainThread(UserInfoEvent event) {
        if (Objects.requireNonNull(event) == UserInfoEvent.USER_INFO_OK) {
            init(imServiceConnector.getIMService());
        }
    }

    private void init(IMService imService) {
        showContent();
        hideProgressBar();

        if (imService == null) {
            return;
        }

        final UserEntity loginContact = imService.getIMLoginManager().getUserEntity();
        if (loginContact == null) {
            return;
        }
        TextView nickNameView = curView.findViewById(R.id.nickName);
        TextView userNameView = curView.findViewById(R.id.userName);
        IMBaseImageView avatarImageView = curView.findViewById(R.id.user_avatar);

        nickNameView.setText(loginContact.getMainName());
        userNameView.setText(loginContact.getRealName());

        avatarImageView.setDefaultImageRes(R.drawable.default_user_avatar);
        avatarImageView.setCorner(15);
        avatarImageView.setAvatarAppend(SysConstant.AVATAR_APPEND_200);
        avatarImageView.setImageResource(R.drawable.default_user_avatar);
        avatarImageView.setImageUrl(loginContact.getAvatar());

        RelativeLayout userContainer = curView.findViewById(R.id.user_container);
        userContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                IMUIHelper.openUserProfileActivity(getActivity(), loginContact.getPeerId());
            }
        });
    }

    private void deleteFilesByDirectory(File directory) {
        if (directory != null && directory.exists() && directory.isDirectory()) {
            for (File item : Objects.requireNonNull(directory.listFiles())) {
                item.delete();
            }
        } else {
            logger.e("fragment#deleteFilesByDirectory, failed");
        }
    }
}
