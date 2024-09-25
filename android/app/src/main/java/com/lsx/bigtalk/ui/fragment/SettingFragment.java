package com.lsx.bigtalk.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import androidx.annotation.NonNull;
import com.lsx.bigtalk.R;
import com.lsx.bigtalk.service.service.IMService;
import com.lsx.bigtalk.ui.base.BTBaseFragment;
import com.lsx.bigtalk.service.support.IMServiceConnector;


public class SettingFragment extends BTBaseFragment {
	private View curView = null;
	private Switch notificationNoDisturbSwitch;
	private Switch notificationGotSoundSwitch;
	private Switch notificationGotVibrationSwitch;

    private final IMServiceConnector imServiceConnector = new IMServiceConnector() {
        @Override
        public void onIMServiceConnected() {
            logger.d("SettingFragment#onIMServiceConnected");
            IMService imService = imServiceConnector.getIMService();
            if (imService != null) {
                initOptions();
            }
        }

        @Override
        public void onServiceDisconnected() {
        }
    };

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
		imServiceConnector.connect(this.getActivity());
		if (null != curView) {
			((ViewGroup) curView.getParent()).removeView(curView);
			return curView;
		}
		curView = inflater.inflate(R.layout.setting_fragment, baseFragmentLayout);
		initRes();
		return curView;
	}

    /**
     * Called when the fragment is no longer in use.  This is called
     * after {@link #onStop()} and before {@link #onDetach()}.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        imServiceConnector.disconnect(getActivity());
    }

    private void initOptions() {
		notificationNoDisturbSwitch = curView.findViewById(R.id.notification_no_disturb_switch);
		notificationGotSoundSwitch = curView.findViewById(R.id.notify_got_sound_switch);
		notificationGotVibrationSwitch = curView.findViewById(R.id.notify_got_vibration_switch);
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	/**
	 * @Description 初始化资源
	 */
	private void initRes() {
		setTopCenterTitleText(getActivity().getString(R.string.setting_page_name));
		setTopLeftBtnImage(R.drawable.ic_back);
		topLeftContainerLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				getActivity().finish();
			}
		});
		setTopLeftText(getResources().getString(R.string.top_left_back));
	}

	@Override
	protected void initHandler() {
	}

}
