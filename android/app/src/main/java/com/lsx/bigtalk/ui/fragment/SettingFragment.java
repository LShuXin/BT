package com.lsx.bigtalk.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;

import com.lsx.bigtalk.DB.sp.ConfigurationSp;
import com.lsx.bigtalk.R;
import com.lsx.bigtalk.config.SysConstant;
import com.lsx.bigtalk.imservice.service.IMService;
import com.lsx.bigtalk.ui.helper.CheckboxConfigHelper;
import com.lsx.bigtalk.ui.base.BTBaseFragment;
import com.lsx.bigtalk.imservice.support.IMServiceConnector;

/**
 * 设置页面
 */
public class SettingFragment extends BTBaseFragment {
	private View curView = null;
	private CheckBox notificationNoDisturbCheckBox;
	private CheckBox notificationGotSoundCheckBox;
	private CheckBox notificationGotVibrationCheckBox;
	CheckboxConfigHelper checkBoxConfiger = new CheckboxConfigHelper();


    private final IMServiceConnector imServiceConnector = new IMServiceConnector(){
        @Override
        public void onIMServiceConnected() {
            logger.d("config#onIMServiceConnected");
            IMService imService = imServiceConnector.getIMService();
            if (imService != null) {
                checkBoxConfiger.init(imService.getConfigSp());
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
		notificationNoDisturbCheckBox = curView.findViewById(R.id.NotificationNoDisturbCheckbox);
		notificationGotSoundCheckBox = curView.findViewById(R.id.notifyGotSoundCheckBox);
		notificationGotVibrationCheckBox = curView.findViewById(R.id.notifyGotVibrationCheckBox);
//		saveTrafficModeCheckBox = (CheckBox) curView.findViewById(R.id.saveTrafficCheckBox);
		
		checkBoxConfiger.initCheckBox(notificationNoDisturbCheckBox, SysConstant.SETTING_GLOBAL, ConfigurationSp.CfgDimension.NOTIFICATION );
		checkBoxConfiger.initCheckBox(notificationGotSoundCheckBox, SysConstant.SETTING_GLOBAL , ConfigurationSp.CfgDimension.SOUND);
		checkBoxConfiger.initCheckBox(notificationGotVibrationCheckBox, SysConstant.SETTING_GLOBAL,ConfigurationSp.CfgDimension.VIBRATION );
//		checkBoxConfiger.initCheckBox(saveTrafficModeCheckBox, ConfigDefs.SETTING_GLOBAL, ConfigDefs.KEY_SAVE_TRAFFIC_MODE, ConfigDefs.DEF_VALUE_SAVE_TRAFFIC_MODE);
	}

	@Override
	public void onResume() {

		super.onResume();
	}

	/**
	 * @Description 初始化资源
	 */
	private void initRes() {
		// 设置标题栏
		setTopCenterTitleText(getActivity().getString(R.string.setting_page_name));
		setTopLeftBtnImage(R.drawable.tt_top_back);
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
