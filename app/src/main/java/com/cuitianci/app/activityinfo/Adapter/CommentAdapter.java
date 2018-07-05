package com.cuitianci.app.activityinfo.Adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.cuitianci.app.activityinfo.DAO.Comment;
import com.cuitianci.app.activityinfo.R;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by 77214 on 2018/6/11.
 */

public class CommentAdapter extends BaseAdapter {

    private List<Comment> list;
    private ListView listview;
    private LruCache<String, BitmapDrawable> mImageCache;

    public CommentAdapter(List<Comment> list) {
        super();
        this.list = list;
        int maxCache = (int) Runtime.getRuntime().maxMemory();
        int cacheSize = maxCache / 8;
        mImageCache = new LruCache<String, BitmapDrawable>(cacheSize) {
            @Override
            protected int sizeOf(String key, BitmapDrawable value) {
                return value.getBitmap().getByteCount();
            }
        };

    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (listview == null) {
            listview = (ListView) parent;
        }
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.adapter_comment, null);
            holder = new ViewHolder();
            holder.commentCiv = (ImageView) convertView.findViewById(R.id.comment_user_image);
            holder.commentUsernameTv = (TextView) convertView.findViewById(R.id.adapter_comment_username_tv);
            holder.commentContentTv = (TextView) convertView.findViewById(R.id.adapter_comment_content_tv);
            holder.commentTimeTv = (TextView) convertView.findViewById(R.id.adapter_comment_time_tv);
            holder.commentSupportNumTv = (TextView)convertView.findViewById(R.id.adapter_comment_support_num_tv);
            holder.commentSupportIv = (ImageView)convertView.findViewById(R.id.adapter_comment_support_iv);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Comment comment = list.get(position);
        holder.commentUsernameTv.setText(comment.getCreater());
        holder.commentContentTv.setText(comment.getContent());
        holder.commentTimeTv.setText(comment.getTime());
//        holder.commentCiv.setTag(comment.getImgUrl());
        //缺一个点赞数量。
        // 如果本地已有缓存，就从本地读取，否则从网络请求数据
      /*  if (mImageCache.get(comment.getImgUrl()) != null) {
            holder.commentCiv.setImageDrawable(mImageCache.get(comment.getImgUrl()));
        } else {
           ImageTask it = new ImageTask();
            it.execute(comment.getImgUrl());
        }*/
        return convertView;
    }

    class ImageTask extends AsyncTask<String, Void, BitmapDrawable> {

        private String imageUrl;

        @Override
        protected BitmapDrawable doInBackground(String... params) {
            imageUrl = params[0];
            Bitmap bitmap = downloadImage();
            BitmapDrawable db = new BitmapDrawable(listview.getResources(),
                    bitmap);
            // 如果本地还没缓存该图片，就缓存
            if (mImageCache.get(imageUrl) == null) {
                mImageCache.put(imageUrl, db);
            }
            return db;
        }

        @Override
        protected void onPostExecute(BitmapDrawable result) {
            // 通过Tag找到我们需要的ImageView，如果该ImageView所在的item已被移出页面，就会直接返回null
            ImageView iv = (ImageView) listview.findViewWithTag(imageUrl);
            if (iv != null && result != null) {
                iv.setImageDrawable(result);
            }
        }

        /**
         * 根据url从网络上下载图片
         *
         * @return
         */
        private Bitmap downloadImage() {
            HttpURLConnection con = null;
            Bitmap bitmap = null;
            try {
                URL url = new URL(imageUrl);
                con = (HttpURLConnection) url.openConnection();
                con.setConnectTimeout(5 * 1000);
                con.setReadTimeout(10 * 1000);
                bitmap = BitmapFactory.decodeStream(con.getInputStream());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (con != null) {
                    con.disconnect();
                }
            }

            return bitmap;
        }

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
