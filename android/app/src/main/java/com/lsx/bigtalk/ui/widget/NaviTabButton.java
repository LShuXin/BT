package com.lsx.bigtalk.ui.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lsx.bigtalk.R;
import com.lsx.bigtalk.ui.activity.MainActivity;
import com.lsx.bigtalk.logs.Logger;
import com.lsx.bigtalk.ui.helper.listener.OnDoubleClickListener;


public class NaviTabButton extends FrameLayout {
	private int mIndex;

	private final ImageView mImage;
	private final TextView mTitle;
	private final TextView mNotify;

	private Drawable mSelectedImg;
	private Drawable mUnselectedImg;

	private final Context mContext;

	private final Logger logger = Logger.getLogger(NaviTabButton.class);

	public NaviTabButton(Context context) {
		this(context, null);
	}

	public NaviTabButton(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public NaviTabButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        this.mContext = context;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.nav_tab_button_view, this, true);
        RelativeLayout container = findViewById(R.id.tab_btn_container);

        mImage = findViewById(R.id.tab_btn_default);
        mTitle = findViewById(R.id.tab_btn_title);
        mNotify = findViewById(R.id.tab_unread_notify);

        if (mIndex == 0) {
            OnDoubleClickListener onDoubleClickListener = new OnDoubleClickListener() {
                @Override
                public void onDoubleClick(View view) {
                    if (mIndex == 0) {
                        ((MainActivity) mContext).handleSessionDoubleClick();
                    }
                }

                @Override
                public void onClick(View view) {
                }
            };
            container.setOnTouchListener(onDoubleClickListener);
        }

       View.OnClickListener clickListener = new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               ((MainActivity) mContext).setFragmentIndicator(mIndex);
           }
       };
       container.setOnClickListener(clickListener);
    }

	public void setIndex(int index) {
		this.mIndex = index;
	}

	public void setUnselectedImage(Drawable img) {
		this.mUnselectedImg = img;
	}

	public void setSelectedImage(Drawable img) {
		this.mSelectedImg = img;
	}

	private void setSelectedTitleColor(Boolean selected) {
		if (selected) {
			mTitle.setTextColor(getResources().getColor(
					R.color.default_blue_color));
		} else {
			mTitle.setTextColor(getResources().getColor(
					R.color.default_light_grey_color));
		}
	}

	public void setSelectedButton(Boolean selected) {
		setSelectedTitleColor(selected);
		if (selected) {
			mImage.setImageDrawable(mSelectedImg);
		} else {
			mImage.setImageDrawable(mUnselectedImg);
		}
	}

	public void setTitle(String title) {
		mTitle.setText(title);
	}

	public void setUnreadNotify(int unreadNum) {
		logger.d("unread#setUreadNotify -> unreadNum:%d", unreadNum);
		if (0 == unreadNum) {
			mNotify.setVisibility(View.INVISIBLE);
			return;
		}

		String notify;
		if (unreadNum > 99) {
			notify = "99+";
		} else {
			notify = Integer.toString(unreadNum);
		}

		mNotify.setText(notify);
		mNotify.setVisibility(View.VISIBLE);
	}

}
