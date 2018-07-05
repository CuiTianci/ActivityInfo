package com.cuitianci.app.activityinfo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cuitianci.app.activityinfo.Adapter.MyAdapter;
import com.cuitianci.app.activityinfo.Adapter.NestedListView;
import com.cuitianci.app.activityinfo.DAO.Activity;
import com.cuitianci.app.activityinfo.DAO.Comment;
import com.cuitianci.app.activityinfo.DAO.PersonalLoginInfo;
import com.cuitianci.app.activityinfo.Util.CodeUtil;
import com.cuitianci.app.activityinfo.Util.DateUtil;
import com.cuitianci.app.activityinfo.Util.GradeUtil;
import com.cuitianci.app.activityinfo.Util.HttpUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NoticeInfoActivity extends AppCompatActivity {
    private Activity refreshActivity;
    private Comment deleteTempComment;
    //手指按下的点为(x1, y1)手指离开屏幕的点为(x2, y2)
    private float x1 = 0;
    private float x2 = 0;
    private float y1 = 0;
    private float y2 = 0;
    //图片
    private Bitmap bitmap;
    //适配器
    private MyAdapter adapter;
    //listView
    private ListView listView;
    //list
    private List<Comment> commentList;
    private static final int UPDATE_IMAGE = 1;
    private String httpUrl;
    private String activityId;

    private TextView allGradeTv,allDepartmentTv,titleTv,contentTv,timeTv,organizationTv;

    private  Activity activity;
    private final PersonalLoginInfo personalLoginInfo = DataSupport.findLast(PersonalLoginInfo.class);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice_info);
        NoticeInfoActivity.this.setTitle("通知详情");
        //活动信息
        activity = (Activity) getIntent().getSerializableExtra("activity");
        activityId = activity.getId().toString();
        //绑定控件
        allGradeTv = (TextView)findViewById(R.id.notice_all_grade);
        allDepartmentTv = (TextView)findViewById(R.id.notice_all_department);
        titleTv = (TextView)findViewById(R.id.notice_info_title);
        contentTv = (TextView)findViewById(R.id.notice_info_content);
        timeTv = (TextView)findViewById(R.id.notice_info_time);
        organizationTv = (TextView)findViewById(R.id.notice_info_organization);
        //加载数据
        allGradeTv.setText(GradeUtil.gradeCodeToTag(activity.getGrade()));
        allDepartmentTv.setText(activity.getDepartment());
        titleTv.setText(activity.getActivityName());
        contentTv.setText(activity.getActivityDescription());
        timeTv.setText(activity.getStartTime());
        organizationTv.setText(activity.getOrganization());

        listView = (NestedListView) findViewById(R.id.notice_info_comment_listView);

        //加载评论
        initComments();

        //标题栏
        Toolbar toolbar = (Toolbar)findViewById(R.id.notice_info_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    Toast.makeText(NoticeInfoActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    Toast.makeText(getApplicationContext(),"评论成功",Toast.LENGTH_SHORT).show();
                    try {
                        Thread.sleep(Long.valueOf(100));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //发表后刷新listView的adapter
                    initComments();
                    break;
                case 2:
                    adapter = new MyAdapter(NoticeInfoActivity.this,commentList);
                    int i = adapter.getCount();
                    listView.setAdapter(adapter);
                    listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                        @Override
                        public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                            if(!(personalLoginInfo.getRoleId() == 1) && !(activity.getCreaterId().longValue() == personalLoginInfo.getUserId().longValue()) && !(commentList.get(position).getCreaterId().longValue() == personalLoginInfo.getUserId().longValue())){
                                return false;
                            }//既不是管理员也不是活动发起者也不是发表评论者
                            new DeleteDialog(new DeleteDialog.DeleteListener() {
                                class MyOnClickListener implements View.OnClickListener {
                                    private final int position;

                                    public MyOnClickListener(int position) {
                                        this.position = position;
                                    }

                                    @Override
                                    public void onClick(View v) {
                                        commentList.add(position, deleteTempComment);
                                        adapter.notifyDataSetChanged();
                                    }
                                }

                                @Override
                                public void delete() {
                                    deleteTempComment = commentList.get(position);
                                    commentList.remove(position);
                                    adapter.notifyDataSetChanged();
                                    Snackbar snackBar = Snackbar.make(listView, "是否撤销删除？", Snackbar.LENGTH_LONG);
                                    snackBar.setAction("撤销", new MyOnClickListener(position));
                                    snackBar.addCallback(new Snackbar.Callback(){
                                        @Override
                                        public void onDismissed(Snackbar snackbar, int event) {
                                            super.onDismissed(snackbar, event);
                                            if (event == DISMISS_EVENT_SWIPE || event == DISMISS_EVENT_TIMEOUT || event ==
                                                    DISMISS_EVENT_CONSECUTIVE) {
                                                deleteComment(deleteTempComment);
                                            }
                                        }
                                    });
                                    snackBar.show();
                                }
                            }, new DeleteDialog.CancelListener() {
                                @Override
                                public void cancel() {
                                }
                            }).show(getSupportFragmentManager(), "delete");
                            return false;
                        }
                    });
                    break;
                case 6:
                    timeTv.setText(refreshActivity.getTime());
                    organizationTv.setText(refreshActivity.getOrganization());
                    contentTv.setText(refreshActivity.getActivityDescription());
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onResume(){
        super.onResume();
        httpUrl = CodeUtil.url + "/find_activity_by_id";
        FormBody.Builder formBody = new FormBody.Builder();//创建表单请求体
        formBody.add("activityId",activityId);
        RequestBody requestBody = formBody.build();
        HttpUtil.sendOkHttpRequestWithRequestBody(httpUrl,requestBody,new okhttp3.Callback(){

            @Override
            public void onFailure(Call call, IOException e) {
                handler.obtainMessage(0).sendToTarget();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Gson gson = new Gson();
                refreshActivity = gson.fromJson(response.body().string(), Activity.class);
                handler.obtainMessage(6).sendToTarget();
            }
        });
    }

    //加载menu布局
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.activity_info,menu);
        menu.findItem(R.id.activity_info_user_iv).setVisible(false);
        if(personalLoginInfo.getRoleId() == 3){
            menu.findItem((R.id.activity_info_edit_item)).setVisible(false);
        }
        if(personalLoginInfo.getRoleId() == 2 && personalLoginInfo.getUserId().longValue() != activity.getCreaterId().longValue()){
            menu.findItem((R.id.activity_info_edit_item)).setVisible(false);
        }
        return true;
    }
    //menu点击监听
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:     //标题栏最左侧的item的id被android默认设置为home。
                finish();
                break;
            case R.id.activity_info_comment_iv:
                new CommentDialog("优质评论将会被优先展示", new CommentDialog.SendListener() {
                    @Override
                    public void sendComment(String inputText) {
                        //当前登陆人信息
                        httpUrl = CodeUtil.url + "/save_comment";
                        FormBody.Builder formBody = new FormBody.Builder();//创建表单请求体
                        formBody.add("activityId",activity.getId().toString());
                        formBody.add("content",inputText);
                        formBody.add("time", DateUtil.formate(new Date(System.currentTimeMillis()),3));
                        formBody.add("status","1");
                        formBody.add("type","1");
                        formBody.add("createrId",personalLoginInfo.getUserId().toString());
                        formBody.add("creater",personalLoginInfo.getNickname());
                        if(personalLoginInfo.getImgUrl() != null){
                            formBody.add("imgUrl",personalLoginInfo.getImgUrl());
                        }
                        RequestBody requestBody = formBody.build();
                        HttpUtil.sendOkHttpRequestWithRequestBody(httpUrl,requestBody,new okhttp3.Callback(){

                            @Override
                            public void onFailure(Call call, IOException e) {
                                handler.obtainMessage(0).sendToTarget();
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                handler.obtainMessage(1).sendToTarget();
                            }
                        });
                    }
                }).show(getSupportFragmentManager(), "comment");
                break;
            case R.id.activity_info_edit_item:
                Intent intent2 = new Intent(NoticeInfoActivity.this,NoticeAddActivity.class);
                intent2.putExtra("addOrEdit","edit");
                intent2.putExtra("activity",activity);
                startActivity(intent2);
            default:
        }
        return true;
    }

    private void initComments() {
        commentList = new ArrayList<Comment>();
//      httpUrl  = "http://litchiapi.jstv.com/api/GetFeeds?column=3&PageSize=20&pageIndex=1&val=100511D3BE5301280E0992C73A9DEC41";
        httpUrl = CodeUtil.url + "/select_comment";
        FormBody.Builder formBody = new FormBody.Builder();//创建表单请求体
        formBody.add("activityId",activityId);
        RequestBody requestBody = formBody.build();
        HttpUtil.sendOkHttpRequestWithRequestBody(httpUrl,requestBody,new okhttp3.Callback(){

            @Override
            public void onFailure(Call call, IOException e) {
                handler.obtainMessage(0).sendToTarget();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Gson gson = new Gson();
                commentList = gson.fromJson(response.body().string(),new TypeToken<List<Comment>>(){}.getType());
               /* for(Comment comment : comments){
                    comment.setImgUrl(imgUrl);
                    commentList.add(comment);
                }*/

                handler.obtainMessage(2).sendToTarget();
            }
        });

    }

    private void  deleteComment(Comment deleteTempComment){
        httpUrl = CodeUtil.url + "/delete_comment";
        FormBody.Builder formBody = new FormBody.Builder();//创建表单请求体
        formBody.add("id",deleteTempComment.getId().toString());
        RequestBody requestBody = formBody.build();
        HttpUtil.sendOkHttpRequestWithRequestBody(httpUrl,requestBody,new okhttp3.Callback(){

            @Override
            public void onFailure(Call call, IOException e) {
                handler.obtainMessage(0).sendToTarget();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //继承了Activity的onTouchEvent方法，直接监听点击事件
        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            //当手指按下的时候
            x1 = event.getX();
            y1 = event.getY();
        }
        if(event.getAction() == MotionEvent.ACTION_UP) {
            //当手指离开的时候
            x2 = event.getX();
            y2 = event.getY();
            if(y1 - y2 > 50) {
                Toast.makeText(NoticeInfoActivity.this, "向上滑", Toast.LENGTH_SHORT).show();
            } else if(y2 - y1 > 50) {
                Toast.makeText(NoticeInfoActivity.this, "向下滑", Toast.LENGTH_SHORT).show();
            } else if(x1 - x2 > 50) {
                Toast.makeText(NoticeInfoActivity.this, "向左滑", Toast.LENGTH_SHORT).show();
            } else if(x2 - x1 > 50) {
                Toast.makeText(NoticeInfoActivity.this, "向右滑", Toast.LENGTH_SHORT).show();
            }
        }
        return super.onTouchEvent(event);
    }
}
