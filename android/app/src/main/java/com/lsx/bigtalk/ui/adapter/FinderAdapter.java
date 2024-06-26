package com.lsx.bigtalk.ui.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.BaseJsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cz.msebera.android.httpclient.Header;

import com.lsx.bigtalk.DB.sp.SystemConfigSp;
import com.lsx.bigtalk.R;
import com.lsx.bigtalk.utils.Logger;


public class FinderAdapter extends BaseAdapter {
    private final Context ctx;
    private final List<InternalItem> dataList = new ArrayList<>();
    private final Logger logger = Logger.getLogger(FinderAdapter.class);
    private final AsyncHttpClient client;

    public FinderAdapter(Context context) {
        ctx = context;
        client = new AsyncHttpClient();
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public InternalItem getItem(int position) {
        if (position >= dataList.size() || position < 0) {
            return null;
        }
        return dataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public static class ViewHolder {
        public TextView title;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        try {
            InternalItem info = dataList.get(position);
            ViewHolder holder;
            if (null == convertView) {
                convertView = LayoutInflater.from(ctx).inflate(R.layout.item_internal_item, parent, false);
                holder = new ViewHolder();
                holder.title = convertView.findViewById(R.id.tt_internal_item_title);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.title.setText(info.getItemName());
            return convertView;
        } catch (Exception e) {
            logger.e(e.toString());
            return null;
        }
    }

    public void update() {
        client.setUserAgent("Android-TT");
        SystemConfigSp.instance().init(ctx.getApplicationContext());
        client.get(SystemConfigSp.instance().getStrConfig(SystemConfigSp.SysCfgDimension.DISCOVERYURI), new BaseJsonHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, String s, Object o) {
            }

            @Override
            public void onFailure(int i, Header[] headers, Throwable throwable, String responseString, Object o) {
                try {
                    convertJson2Data();
                } catch (JSONException e) {
                    logger.e(e.toString());
                }
            }

            @Override
            protected Object parseResponse(String s, boolean b) throws Throwable {
                SystemConfigSp.instance().setStrConfig(SystemConfigSp.SysCfgDimension.DISCOVERYDATA, s);
                convertJson2Data();
                return null;
            }
        });
    }

    private void convertJson2Data() throws JSONException {
        String strData = SystemConfigSp.instance().getStrConfig(SystemConfigSp.SysCfgDimension.DISCOVERYDATA);
        if (!TextUtils.isEmpty(strData)) {
            JSONArray jsonArray = new JSONArray(strData);
            int len = jsonArray.length();
            dataList.clear();
            for (int i = 0; i < len; i++) {
                JSONObject item = (JSONObject) jsonArray.get(i);
                InternalItem info = new InternalItem();
                info.setItemName(item.getString("itemName"));
                info.setItemUrl(item.getString("itemUrl"));
                info.setItemPriority(item.getInt("itemPriority"));
                dataList.add(info);
            }
            dataList.sort(new SortByPriority());
        } else {
            dataList.clear();
        }
        notifyDataSetChanged();
    }

    public static class InternalItem {
        private int id;
        private String itemName;
        private String itemUrl;
        private int itemPriority;
        private int status;
        private int created;
        private int updated;

        public void setId(int id) {
            this.id = id;
        }

        public int getId() {
            return this.id;
        }

        public void setItemName(String name) {
            this.itemName = name;
        }

        public String getItemName() {
            return itemName;
        }

        public void setItemUrl(String url) {
            this.itemUrl = url;
        }

        public String getItemUrl() {
            return this.itemUrl;
        }

        public void setItemPriority(int priority) {
            this.itemPriority = priority;
        }

        public int getItemPriority() {
            return this.itemPriority;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public int getStatus() {
            return this.status;
        }

        public void setCreated(int created) {
            this.created = created;
        }

        public int getCreated() {
            return this.created;
        }

        public void setUpdated(int updated) {
            this.updated = updated;
        }

        public int getUpdated() {
            return this.updated;
        }
    }

    static class SortByPriority implements Comparator<InternalItem> {
        public int compare(InternalItem item1, InternalItem item2) {
            return Integer.compare(item1.getItemPriority(), item2.getItemPriority());
        }
    }

}
