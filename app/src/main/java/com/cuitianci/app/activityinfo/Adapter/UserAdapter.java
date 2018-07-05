package com.cuitianci.app.activityinfo.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.cuitianci.app.activityinfo.DAO.Comment;
import com.cuitianci.app.activityinfo.DAO.User;
import com.cuitianci.app.activityinfo.R;

import java.util.List;

/**
 * Created by 77214 on 2018/6/19.
 */

public class UserAdapter extends BaseAdapter {
    private LinearLayout mLinearLayout;
    private Context mContext;
    private List<User> mList;


    public UserAdapter(Context mContext, List<User> mList) {
        this.mContext = mContext;
        this.mList = mList;
    }
    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        UserAdapter.ViewHolder viewHolder;
        if (convertView == null) {//判断view是否可以重载
            viewHolder = new UserAdapter.ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(mContext);
            mLinearLayout = (LinearLayout) inflater.inflate(R.layout.adapter_user, null);
            viewHolder.nameTv = mLinearLayout.findViewById(R.id.user_name_tv);
            viewHolder.nameTv.setText(mList.get(position).getRealName());
            mLinearLayout.setTag(viewHolder);
        }else{
            mLinearLayout = (LinearLayout) convertView;
            viewHolder = (UserAdapter.ViewHolder) mLinearLayout.getTag();
            viewHolder.nameTv = mLinearLayout.findViewById(R.id.user_name_tv);
            viewHolder.nameTv.setText(mList.get(position).getRealName());
        }
        return mLinearLayout;
    }

    static class ViewHolder {
        TextView nameTv;
    }
}
