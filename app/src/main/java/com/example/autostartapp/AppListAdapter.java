package com.example.autostartapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class AppListAdapter extends BaseAdapter {

    private Context context;
    private List<AppInfo> appList;
    private LayoutInflater inflater;

    public AppListAdapter(Context context, List<AppInfo> appList) {
        this.context = context;
        this.appList = appList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return appList != null ? appList.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return appList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_app, parent, false);
            holder = new ViewHolder();
            holder.ivIcon = (ImageView) convertView.findViewById(R.id.iv_app_icon);
            holder.tvName = (TextView) convertView.findViewById(R.id.tv_app_name);
            holder.tvPackage = (TextView) convertView.findViewById(R.id.tv_app_package);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        AppInfo appInfo = appList.get(position);
        holder.tvName.setText(appInfo.getAppName());
        holder.tvPackage.setText(appInfo.getPackageName());
        if (appInfo.getAppIcon() != null) {
            holder.ivIcon.setImageDrawable(appInfo.getAppIcon());
        }

        return convertView;
    }

    static class ViewHolder {
        ImageView ivIcon;
        TextView tvName;
        TextView tvPackage;
    }
}
