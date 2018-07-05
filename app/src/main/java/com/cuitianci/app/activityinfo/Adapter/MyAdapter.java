package com.cuitianci.app.activityinfo.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cuitianci.app.activityinfo.DAO.Comment;
import com.cuitianci.app.activityinfo.R;

import java.util.List;

/**
 * Created by 77214 on 2018/6/13.
 */

public class MyAdapter extends BaseAdapter {

    private LinearLayout mLinearLayout;
    private Context mContext;
    private List<Comment> mList;

    public MyAdapter(Context mContext, List<Comment> mList) {
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
        ViewHolder viewHolder;
        if (convertView == null) {//判断view是否可以重载
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(mContext);
            mLinearLayout = (LinearLayout) inflater.inflate(R.layout.adapter_comment, null);
            //控件绑定
            viewHolder.commentCiv = mLinearLayout.findViewById(R.id.comment_user_image);
            viewHolder.commentUsernameTv = mLinearLayout.findViewById(R.id.adapter_comment_username_tv);
            viewHolder.commentContentTv = mLinearLayout.findViewById(R.id.adapter_comment_content_tv);
            viewHolder.commentTimeTv = mLinearLayout.findViewById(R.id.adapter_comment_time_tv);
            viewHolder.commentSupportNumTv = mLinearLayout.findViewById(R.id.adapter_comment_support_num_tv);
            viewHolder.commentSupportIv = mLinearLayout.findViewById(R.id.adapter_comment_support_iv);
            //设置数据
            viewHolder.commentUsernameTv.setText(mList.get(position).getCreater());
            viewHolder.commentContentTv.setText(mList.get(position).getContent());
            viewHolder.commentTimeTv.setText(mList.get(position).getTime());
            //点赞图片已经有了
            //缺少点赞数量。
            //使用glide加载图片
            Glide.with(mContext)
                    .load(mList.get(position).getImgUrl()) //加载地址
                    .placeholder(R.drawable.ic_head)//加载未完成时显示占位图
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(viewHolder.commentCiv);//显示的位置
            //标记当前view
            mLinearLayout.setTag(viewHolder);
        }else {//可以重载则直接使用原来的view
            mLinearLayout = (LinearLayout) convertView;
            viewHolder = (ViewHolder) mLinearLayout.getTag();
            //获取id
            //控件绑定
            viewHolder.commentCiv = mLinearLayout.findViewById(R.id.comment_user_image);
            viewHolder.commentUsernameTv = mLinearLayout.findViewById(R.id.adapter_comment_username_tv);
            viewHolder.commentContentTv = mLinearLayout.findViewById(R.id.adapter_comment_content_tv);
            viewHolder.commentTimeTv = mLinearLayout.findViewById(R.id.adapter_comment_time_tv);
            viewHolder.commentSupportNumTv = mLinearLayout.findViewById(R.id.adapter_comment_support_num_tv);
            viewHolder.commentSupportIv = mLinearLayout.findViewById(R.id.adapter_comment_support_iv);
            //设置数据
            viewHolder.commentUsernameTv.setText(mList.get(position).getCreater());
            viewHolder.commentContentTv.setText(mList.get(position).getContent());
            viewHolder.commentTimeTv.setText(mList.get(position).getTime());
            //点赞图片已经有了
            //缺少点赞数量。
            //使用glide加载图片
            Glide.with(mContext)
                    .load(mList.get(position).getImgUrl()) //加载地址
                    .placeholder(R.drawable.ic_head)//加载未完成时显示占位图
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(viewHolder.commentCiv);//显示的位置
        }
        return mLinearLayout;
        }

        static class ViewHolder {
            TextView commentUsernameTv;
            TextView commentContentTv;
            TextView commentSupportNumTv;
            TextView commentTimeTv;
            ImageView commentSupportIv;
            ImageView commentCiv;
        }
    }
