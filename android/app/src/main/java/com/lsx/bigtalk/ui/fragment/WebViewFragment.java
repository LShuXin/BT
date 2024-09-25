package com.lsx.bigtalk.ui.fragment;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;

import com.lsx.bigtalk.R;

import java.util.Objects;


@SuppressLint("SetJavaScriptEnabled")
public class WebViewFragment extends MainFragment {
	private View curView = null;
	private static String url;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        extractUidFromUri();
		if (null != curView) {
            ((ViewGroup) curView.getParent()).removeView(curView);
            return curView;
        }
        curView = inflater.inflate(R.layout.webview_fragment, baseFragmentLayout);
        super.init(curView);
        showProgressBar();
        initRes();

        return curView;
    }
	
    private void initRes() {
        // 设置顶部标题栏
        setTopCenterTitleTextBold(requireActivity().getString(R.string.main_inner_net));
		setTopLeftBtnImage(R.drawable.ic_back);
		topLeftContainerLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				requireActivity().finish();
			}
		});
		setTopLeftText(getResources().getString(R.string.top_left_back));

        WebView webView = curView.findViewById(R.id.sampleWebView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(url);
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setWebViewClient(new WebViewClient() {

			@Override
			public void onPageFinished(WebView view, String url) {
				setTopCenterTitleText(view.getTitle());
                hideProgressBar();
			}

			@Override
			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
				// TODO Auto-generated method stub
				super.onReceivedError(view, errorCode, description, failingUrl);
				hideProgressBar();
			}
        	
        });
    }

	@Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void initHandler() {
    }

	public static void setUrl(String str) {
		url = str;
	}

    private static final String SCHEMA ="com.lsx.bigtalk://message_private_url";
    private static final String PARAM_UID ="uid";
    private static final Uri PROFILE_URI = Uri.parse(SCHEMA);
    private void extractUidFromUri() {
        Uri uri = requireActivity().getIntent().getData();
        if (null != uri && Objects.equals(PROFILE_URI.getScheme(), uri.getScheme())) {
            url = uri.getQueryParameter(PARAM_UID);
        }
        if (null != url && url.startsWith("www")) {
            url = "https://" + url;
        } else if (null != url && url.startsWith("https")) {
            String bUid = url.substring(5);
            url = "http"+bUid;
        }
    }
}
