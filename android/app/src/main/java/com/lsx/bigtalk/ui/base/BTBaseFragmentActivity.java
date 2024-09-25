package com.lsx.bigtalk.ui.base;


import androidx.fragment.app.FragmentActivity;
import com.hjq.toast.Toaster;

public abstract class BTBaseFragmentActivity extends FragmentActivity {


    public void toast(CharSequence text) {
        Toaster.show(text);
    }
   
}
