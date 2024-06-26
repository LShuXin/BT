
package com.lsx.bigtalk.ui.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.lsx.bigtalk.R;
import com.lsx.bigtalk.config.HandlerConstant;
import com.lsx.bigtalk.ui.fragment.ContactFragment;

public class TopTabButtonGroup extends FrameLayout {
    private Context context = null;
    private Button tabALLBtn = null;
    private Button tabDepartmentBtn = null;

    public Button getTabDepartmentBtn() {
		return tabDepartmentBtn;
	}

	public TopTabButtonGroup(Context cxt) {
        super(cxt);
        this.context = cxt;
        initView();
    }

    public TopTabButtonGroup(Context cxt, AttributeSet attrs) {
        super(cxt,attrs);
        this.context = cxt;
        initView();
    }

    public TopTabButtonGroup(Context cxt, AttributeSet attrs, int defStyle) {
        super(cxt, attrs, defStyle);
        this.context = cxt;
        initView();
    }

    private void initView() {
        // 加载布局
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.top_tab_button, this);

        tabALLBtn = findViewById(R.id.all_btn);
        tabDepartmentBtn = findViewById(R.id.department_btn);

        // tabDepartmentBtn.setText(context.getString(R.string.contact_department));
        // tabDepartmentBtn.setBackgroundResource(R.drawable.contact_top_right_nor);
        tabDepartmentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Handler handler = ContactFragment.getHandler();
                Message message = handler.obtainMessage();
                message.what = HandlerConstant.CONTACT_TAB_CHANGED;
                message.obj = 1;
                handler.sendMessage(message);

                setSelTextColor(1);
                tabDepartmentBtn.setBackgroundResource(R.drawable.contact_top_right_selected);
                tabALLBtn.setBackgroundResource(R.drawable.contact_top_left_normal);
            }
        });

        // tabALLBtn.setText(context.getString(R.string.contact_all));
        // tabALLBtn.setBackgroundResource(R.drawable.contact_top_left_sel);
        tabALLBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Handler handler = ContactFragment.getHandler();
                Message message = handler.obtainMessage();
                message.what=HandlerConstant.CONTACT_TAB_CHANGED;
                message.obj = 0;
                handler.sendMessage(message);

                setSelTextColor(0);
                tabALLBtn.setBackgroundResource(R.drawable.contact_top_left_selected);
                tabDepartmentBtn.setBackgroundResource(R.drawable.contact_top_right_normal);
            }
        });

    }

    private void setSelTextColor(int index) {
        if (0 == index) {
            tabALLBtn.setTextColor(getResources().getColor(android.R.color.white));
            tabDepartmentBtn.setTextColor(getResources().getColor(R.color.default_blue_color));
        } else {
            tabDepartmentBtn.setTextColor(getResources().getColor(android.R.color.white));
            tabALLBtn.setTextColor(getResources().getColor(R.color.default_blue_color));
        }

    }
}
