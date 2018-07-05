package com.cuitianci.app.activityinfo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.cuitianci.app.activityinfo.Adapter.MyAdapter;
import com.cuitianci.app.activityinfo.Adapter.NestedListView;
import com.cuitianci.app.activityinfo.DAO.Activity;
import com.cuitianci.app.activityinfo.DAO.ActivityUser;
import com.cuitianci.app.activityinfo.DAO.Comment;
import com.cuitianci.app.activityinfo.DAO.PersonalLoginInfo;
import com.cuitianci.app.activityinfo.DAO.User;
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

public class SignUpActivity extends BaseActivity implements GestureDetector.OnGestureListener {
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private Activity refreshActivity;
    private Comment deleteTempComment;
    private List<Activity> availableActivities;
    private  Activity activity;
    private final PersonalLoginInfo personalLoginInfo = DataSupport.findLast(PersonalLoginInfo.class);
    //图片
    private Bitmap bitmap;
    //适配器
    private MyAdapter adapter;
    //listView
    private NestedListView listView;
    //list
    private List<Comment> commentList;
    private static final int UPDATE_IMAGE = 1;
    private String httpUrl;
    private String activityId;

    private TextView timeTv;
    private TextView placeTv;
    private TextView organizationTv;
    private TextView contentTv;
    private TextView mobileTv;
    private Button signUpButton;

    private GestureDetector detector;

    //手指按下的点为(x1, y1)手指离开屏幕的点为(x2, y2)
    private float x1 = 0;
    private float x2 = 0;
    private float y1 = 0;
    private float y2 = 0;


    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    Toast.makeText(SignUpActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
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
                    adapter = new MyAdapter(SignUpActivity.this,commentList);
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
                case 3:
                    Toast.makeText(SignUpActivity.this,"报名成功",Toast.LENGTH_SHORT).show();
                    signUpButton.setText("取消报名");
                    break;
                case 4:
                    Toast.makeText(SignUpActivity.this,"已取消报名",Toast.LENGTH_SHORT).show();
                    signUpButton.setText("报名");
                    break;
                case 6:
                    timeTv.setText(refreshActivity.getTime());
                    placeTv.setText(refreshActivity.getPlace());
                    organizationTv.setText(refreshActivity.getOrganization());
                    contentTv.setText(refreshActivity.getActivityDescription());
                    mobileTv.setText(refreshActivity.getMobile());
                    collapsingToolbarLayout.setTitle(refreshActivity.getActivityName());
                    break;
                case 100:
                    signUpButton.setText("取消报名");
                    break;
                case 200:
                    boolean ava = false;
                    for(int j = 0;j < availableActivities.size();j ++){
                        if(String.valueOf(availableActivities.get(j).getId()).equals(activityId)){
                            ava = true;
                        }
                    }
                    if(!ava){
                        signUpButton.setVisibility(View.GONE);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        //屏幕手势
        detector = new GestureDetector(this, this);
        listView = (NestedListView) findViewById(R.id.sign_up_comment_listView);
        activity = (Activity) getIntent().getSerializableExtra("activity");
        activityId = activity.getId().toString();
        //加载报名状态
        initHttpData();
        //加载评论
        initComments();
        Toolbar toolbar = (Toolbar)findViewById(R.id.sign_up_toolbar);
        collapsingToolbarLayout = (CollapsingToolbarLayout)findViewById(R.id.collapsing_toolbar);
        ImageView imageView = (ImageView)findViewById(R.id.sign_up_iv);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        //时间地点组织赋值
        timeTv = (TextView)findViewById(R.id.sign_up_time_tv);
        placeTv = (TextView)findViewById(R.id.sign_up_place_tv);
        organizationTv = (TextView)findViewById(R.id.sign_up_organize_tv);
        contentTv = (TextView)findViewById(R.id.sign_up_content_tv);
        timeTv.setText(activity.getTime());
        placeTv.setText(activity.getPlace());
        mobileTv =(TextView)findViewById(R.id.sign_up_mobile_tv);
        mobileTv.setText(activity.getMobile());
        //电话
        mobileTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(SignUpActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(SignUpActivity.this,new String[]{Manifest.permission.CALL_PHONE},1);
                }else{
                    call();
                }
            }
        });
        organizationTv.setText(activity.getOrganization());
        contentTv.setText(activity.getActivityDescription());
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        collapsingToolbarLayout.setTitle(activity.getActivityName());
        //报名按钮
        signUpButton = (Button)findViewById(R.id.sign_up_btn);
        getAvailableActivityList();//隐藏button
        if(!getIntent().getBooleanExtra("available",true)){
            signUpButton.setVisibility(View.GONE);
        }
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(signUpButton.getText().equals("取消报名")){
                    httpUrl = CodeUtil.url + "/cancel_sign_up";
                    FormBody.Builder formBody = new FormBody.Builder();//创建表单请求体
                    formBody.add("activityId",activityId);
                    formBody.add("userId",personalLoginInfo.getUserId().toString());
                    RequestBody requestBody = formBody.build();
                    HttpUtil.sendOkHttpRequestWithRequestBody(httpUrl,requestBody,new okhttp3.Callback(){

                        @Override
                        public void onFailure(Call call, IOException e) {
                            handler.obtainMessage(0).sendToTarget();
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            handler.obtainMessage(4).sendToTarget();
                        }
                    });
                }else{
                    httpUrl = CodeUtil.url + "/sign_up";
                    FormBody.Builder formBody = new FormBody.Builder();//创建表单请求体
                    formBody.add("activityId",activityId);
                    formBody.add("userId",personalLoginInfo.getUserId().toString());
                    RequestBody requestBody = formBody.build();
                    HttpUtil.sendOkHttpRequestWithRequestBody(httpUrl,requestBody,new okhttp3.Callback(){

                        @Override
                        public void onFailure(Call call, IOException e) {
                            handler.obtainMessage(0).sendToTarget();
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            handler.obtainMessage(3).sendToTarget();
                        }
                    });
                }
            }
        });
        //加载资源文件
//        int resourceId = R.drawable.ic_activity_picture;
//        Glide.with(this).load(resourceId).into(imageView);
        Glide.with(this).load(activity.getImageUrl()).into(imageView);
    }

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
        if(personalLoginInfo.getRoleId() == 3){
            menu.findItem(R.id.activity_info_user_iv).setVisible(false);
            menu.findItem((R.id.activity_info_edit_item)).setVisible(false);
        }
        if(personalLoginInfo.getRoleId() == 2 && personalLoginInfo.getUserId().longValue() != activity.getCreaterId().longValue()){
            menu.findItem(R.id.activity_info_user_iv).setVisible(false);
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
            case R.id.activity_info_user_iv:
                Intent intent = new Intent(SignUpActivity.this,ActivityUserListActivity.class);
                intent.putExtra("activityId",activityId);
                startActivity(intent);
                break;
            case R.id.activity_info_edit_item:
                Intent intent2 = new Intent(SignUpActivity.this,ActivityAddActivity.class);
                intent2.putExtra("addOrEdit","edit");
                intent2.putExtra("activity",activity);
                startActivity(intent2);
                break;
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
//                String imgUrl = "http://litchiapi.jstv.com/Attachs/Article/288328/65b28e724f3b4a65ae7bb3f5c97699ab_padmini.JPG";
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




    private void initHttpData(){
        PersonalLoginInfo personalLoginInfo = DataSupport.findLast(PersonalLoginInfo.class);
        httpUrl = CodeUtil.url + "/check_sign_up";
        FormBody.Builder formBody = new FormBody.Builder();//创建表单请求体
        formBody.add("activityId",activityId);
        formBody.add("userId",personalLoginInfo.getUserId().toString());
        RequestBody requestBody = formBody.build();
        HttpUtil.sendOkHttpRequestWithRequestBody(httpUrl,requestBody,new okhttp3.Callback(){

            @Override
            public void onFailure(Call call, IOException e) {
                handler.obtainMessage(0).sendToTarget();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Gson gson = new Gson();
                ActivityUser activityUser = gson.fromJson(response.body().string(),ActivityUser.class);
                if(activityUser != null){
                    handler.obtainMessage(100).sendToTarget();
                }
            }
        });
    }
//屏幕动作
    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        float startX = e1.getX();
        float startY = e1.getY();
        float endX = e2.getX();
        float endY = e2.getY();

        float poorX = endX - startX;
        float poorY = endY - startY;

        if (Math.abs(poorX) > Math.abs(poorY) && Math.abs(poorX) > 100) {
            if(poorY > 0);{
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
                        formBody.add("imgUrl",personalLoginInfo.getImgUrl());
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
            }
        }
        return false;
    }

    private void getAvailableActivityList(){
//      httpUrl  = "http://litchiapi.jstv.com/api/GetFeeds?column=3&PageSize=20&pageIndex=1&val=100511D3BE5301280E0992C73A9DEC41";
        httpUrl = CodeUtil.url + "/real_available_activity";
        FormBody.Builder formBody = new FormBody.Builder();//创建表单请求体
        formBody.add("currentDepartmentId",personalLoginInfo.getDepartmentId().toString());
        formBody.add("currentGrade",String.valueOf(GradeUtil.currentGrade(Integer.valueOf(personalLoginInfo.getAdmission())) + 1));
        RequestBody requestBody = formBody.build();
        HttpUtil.sendOkHttpRequestWithRequestBody(httpUrl,requestBody,new okhttp3.Callback(){

            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String imageUrl = "http://litchiapi.jstv.com/Attachs/Article/288328/65b28e724f3b4a65ae7bb3f5c97699ab_padmini.JPG";
                Gson gson = new Gson();
                availableActivities = gson.fromJson(response.body().string(),new TypeToken<List<Activity>>(){}.getType());
                handler.obtainMessage(200).sendToTarget();
            }
        });
    }


    private void call(){
        try{
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + mobileTv.getText()));
            startActivity(intent);
        }catch (SecurityException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults){
        switch (requestCode){
            case 1:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    call();
                }else {
                    Toast.makeText(SignUpActivity.this,"你拒绝了该权限",Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
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
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
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
                Toast.makeText(SignUpActivity.this, "向上滑", Toast.LENGTH_SHORT).show();
            } else if(y2 - y1 > 50) {
                Toast.makeText(SignUpActivity.this, "向下滑", Toast.LENGTH_SHORT).show();
            } else if(x1 - x2 > 50) {
                Toast.makeText(SignUpActivity.this, "向左滑", Toast.LENGTH_SHORT).show();
            } else if(x2 - x1 > 50) {
                Toast.makeText(SignUpActivity.this, "向右滑", Toast.LENGTH_SHORT).show();
            }
        }
        return super.onTouchEvent(event);
    }
}
