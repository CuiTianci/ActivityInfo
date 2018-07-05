package com.cuitianci.app.activityinfo;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cuitianci.app.activityinfo.Adapter.HomeAdapter;
import com.cuitianci.app.activityinfo.Adapter.MyAdapter;
import com.cuitianci.app.activityinfo.DAO.Activity;
import com.cuitianci.app.activityinfo.DAO.Comment;
import com.cuitianci.app.activityinfo.DAO.PersonalLoginInfo;
import com.cuitianci.app.activityinfo.Util.CodeUtil;
import com.cuitianci.app.activityinfo.Util.GradeUtil;
import com.cuitianci.app.activityinfo.Util.HttpUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends BaseActivity implements View.OnClickListener{
    private List<Activity> availableActivityList;
    private Activity commentActivity;
    //菜单
    private Menu menu;
    //message
    private int currentTv = 1;
    MyAdapter commentAdapter;
    private List<Activity> myActivityList;
    private List<Comment> commentList;
    private TextView myActivity,myComment;
    private String currentFragment = "activity";
    //全局变量
    private String activityName,activityStatus,activityDepartmentId,activityGrade;
    private SearchView homeSearchView;
    private  String httpUrl;
    private List<Activity> activityList,noticeList;
    //三个frame的listView
    private ListView activityListView,noticeListView,commentListView,myActivityLisyView;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    //获取当前登陆人的信息
    private final PersonalLoginInfo currentUserInfo = DataSupport.findLast(PersonalLoginInfo.class);
    //左侧导航栏
    private Menu leftNavMenu;
    private TextView leftNavNicknameTv;
    private TextView leftNavMobileTv;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener

            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    if(currentUserInfo.getRoleId() == 3){
                        menu.findItem(R.id.home_item_add).setVisible(false);
                    }else {
                        menu.findItem(R.id.home_item_add).setVisible(true);
                    }
                    setHomeFragment();
                    activityName = "";
                    currentFragment = "activity";
                    MainActivity.this.setTitle("活动");
                    return true;
                case R.id.navigation_dashboard:
                    if(currentUserInfo.getRoleId() == 3){
                        menu.findItem(R.id.home_item_add).setVisible(false);
                    }else {
                        menu.findItem(R.id.home_item_add).setVisible(true);
                    }
                    setNoticeFragment();
                    activityName = "";
                    currentFragment = "notice";
                    MainActivity.this.setTitle("通知");
                    return true;
                case R.id.navigation_notifications:
                    menu.findItem(R.id.home_item_add).setVisible(false);
                    setMessageFragment();
                    activityName = "";
                    currentFragment = "message";
                    MainActivity.this.setTitle("我的");
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //supportRequestWindowFeature(Window.FEATURE_NO_TITLE);设置无标题栏，因为在manifest中做了相关操作，于是不需要了。
        setContentView(R.layout.activity_main);
        MainActivity.this.setTitle("活动");
        //设置actionBar为我们自定义Toolbar
        Toolbar toolbar = (Toolbar)findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        //加载默认fragment
        setHomeFragment();
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
//            actionBar.setHomeAsUpIndicator(R.drawable.ic_infomation);  待解决图片大小问题。
        }
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        //左部导航栏
        navigationView = (NavigationView)findViewById(R.id.left_nav);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.nav_exchange:
                        Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                        startActivity(intent);
                        finish();
                       break;
                    case R.id.nav_log_out:
                        DataSupport.deleteAll(PersonalLoginInfo.class,"userId = ?",currentUserInfo.getUserId().toString());
                        PersonalLoginInfo emptyPersonInfo = new PersonalLoginInfo();
                        emptyPersonInfo.save();
                        Intent intent2 = new Intent(MainActivity.this,LoginActivity.class);
                        startActivity(intent2);
                        finish();
                        break;
                    case R.id.nav_exit:
                        finish();
                }
                return true;
            }
        });
    }
    //加载menu布局
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.home,menu);
        this.menu = menu;
        if(currentUserInfo.getRoleId() == 3){
            menu.findItem(R.id.home_item_add).setVisible(false);
        }
        return true;
    }
    //menu点击监听
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case android.R.id.home:     //标题栏最左侧的item的id被android默认设置为home。
                //打开左侧导航栏
                drawerLayout.openDrawer(GravityCompat.START);
                //加载左侧导航栏信息
                leftNavNicknameTv = (TextView)findViewById(R.id.left_nav_nickname);
                leftNavMobileTv = (TextView)findViewById(R.id.left_nav_mobile);
                leftNavMobileTv.setText(currentUserInfo.getMobile());
                leftNavNicknameTv.setText(currentUserInfo.getNickname());
                break;
            case R.id.home_item_add:
                Intent intent = new Intent();
                if(currentFragment.equals("activity")){
                    intent.putExtra("addOrEdit","add");
                    intent.setClass(MainActivity.this,ActivityAddActivity.class);
                }else if(currentFragment.equals("notice")){
                    intent.putExtra("addOrEdit","add");
                    intent.setClass(MainActivity.this,NoticeAddActivity.class);
                }
                    startActivity(intent);
                break;
            case R.id.add:
            default:
        }
        return true;
    }

    private Handler homeHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {

            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    HomeAdapter adapter = new HomeAdapter(activityList);
                    activityListView = (ListView)findViewById(R.id.home_listView);
                    activityListView.setAdapter(adapter);
                    activityListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Activity activity = activityList.get(position);
                            Intent intent = new Intent(MainActivity.this,SignUpActivity.class);
                            intent.putExtra("activity",activity);//Activity类实现了Serializable接口，才可以传。
                            startActivity(intent);
                        }
                    });
                    final String currentDepartmentId = String.valueOf(currentUserInfo.getDepartmentId() + 1);
                    final String currentGrade = String.valueOf(GradeUtil.currentGrade(Integer.valueOf(currentUserInfo.getAdmission())) + 1);
                    //搜索框和分类导航栏
                    final Spinner statusSpinner = (Spinner)findViewById(R.id.activity_status_spinner);
                     final Spinner gradeSpinner = (Spinner)findViewById(R.id.activity_grade_spinner);
                     final Spinner departmentSpinner = (Spinner)findViewById(R.id.activity_department_spinner);
                    final CheckBox checkBox = (CheckBox)findViewById(R.id.home_checkbox);
                    homeSearchView = (SearchView)findViewById(R.id.home_searchView);
                    homeSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener(){

                        @Override
                        public boolean onQueryTextSubmit(String query) {
                            return false;
                        }

                        @Override
                        public boolean onQueryTextChange(String newText) {
                            activityName = newText;
                            activityStatus = String.valueOf(statusSpinner.getSelectedItemId());
                            activityGrade = String.valueOf(gradeSpinner.getSelectedItemId());
                            activityDepartmentId = String.valueOf(departmentSpinner.getSelectedItemId());
                            if(!checkBox.isChecked()){
                                refreshHomeData(activityName,activityStatus,activityDepartmentId,activityGrade);
                            }else{
                                refreshAvailableData(activityName,activityStatus,activityDepartmentId,activityGrade,currentDepartmentId,currentGrade);
                            }
                            return false;
                        }
                    });
                    //分类查询导航栏
                    ArrayAdapter<String> status_arr_adapter;
                    //数据
                    ArrayList<String> status_data_list = new ArrayList<String>();
                    status_data_list.add("全部");
                    status_data_list.add("进行中");
                    status_data_list.add("已结束");
                    //适配器
                    status_arr_adapter= new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, status_data_list);
                    //设置样式
                    status_arr_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    //加载适配器
                    statusSpinner.setAdapter(status_arr_adapter);

                    //类型下拉列表
                    ArrayAdapter<String> grade_arr_adapter;
                    //数据
                    ArrayList<String> grade_data_list = new ArrayList<String>();
                    grade_data_list.add("全部");
                    grade_data_list.add("所有年级");
                    grade_data_list.add("大一");
                    grade_data_list.add("大二");
                    grade_data_list.add("大三");
                    grade_data_list.add("大四");
                    //适配器
                    grade_arr_adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, grade_data_list);
                    //设置样式
                    grade_arr_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    //加载适配器
                    gradeSpinner.setAdapter(grade_arr_adapter);

                    //分类查询导航栏
                    ArrayAdapter<String> department_arr_adapter;
                    //数据
                    ArrayList<String> department_data_list = new ArrayList<String>();
                    department_data_list.add("全部");
                    department_data_list.add("所有学院");
                    department_data_list.add("软件学院");
                    department_data_list.add("电信学院");
                    department_data_list.add("电控学院");
                    //适配器
                    department_arr_adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, department_data_list);
                    //设置样式
                    department_arr_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    //加载适配器
                    departmentSpinner.setAdapter(department_arr_adapter);
                    gradeSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {//选择item的选择点击监听事件
                        public void onItemSelected(AdapterView<?> arg0, View arg1,
                                                   int arg2, long arg3) {
                            activityStatus = String.valueOf(statusSpinner.getSelectedItemId());
                            activityGrade = String.valueOf(gradeSpinner.getSelectedItemId());
                            activityDepartmentId = String.valueOf(departmentSpinner.getSelectedItemId());
                            if(!checkBox.isChecked()){
                                refreshHomeData(activityName,activityStatus,activityDepartmentId,activityGrade);
                            }else{
                                refreshAvailableData(activityName,activityStatus,activityDepartmentId,activityGrade,currentDepartmentId,currentGrade);
                            }
                        }

                        public void onNothingSelected(AdapterView<?> arg0) {
                            Toast.makeText(MainActivity.this,"无",Toast.LENGTH_LONG).show();
                        }
                    });
                    statusSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {//选择item的选择点击监听事件
                        public void onItemSelected(AdapterView<?> arg0, View arg1,
                                                   int arg2, long arg3) {
                            // 将所选mySpinner 的值带入myTextView 中
                            activityStatus = String.valueOf(statusSpinner.getSelectedItemId());
                            activityGrade = String.valueOf(gradeSpinner.getSelectedItemId());
                            activityDepartmentId = String.valueOf(departmentSpinner.getSelectedItemId());
                            if(!checkBox.isChecked()){
                                refreshHomeData(activityName,activityStatus,activityDepartmentId,activityGrade);
                            }else{
                                refreshAvailableData(activityName,activityStatus,activityDepartmentId,activityGrade,currentDepartmentId,currentGrade);
                            }
                        }

                        public void onNothingSelected(AdapterView<?> arg0) {
                            Toast.makeText(MainActivity.this,"无",Toast.LENGTH_LONG).show();
                        }

                    });
                    departmentSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {//选择item的选择点击监听事件
                        public void onItemSelected(AdapterView<?> arg0, View arg1,
                                                   int arg2, long arg3) {
                            // 将所选mySpinner 的值带入myTextView 中
                            activityStatus = String.valueOf(statusSpinner.getSelectedItemId());
                            activityGrade = String.valueOf(gradeSpinner.getSelectedItemId());
                            activityDepartmentId = String.valueOf(departmentSpinner.getSelectedItemId());
                            if(!checkBox.isChecked()){
                                refreshHomeData(activityName,activityStatus,activityDepartmentId,activityGrade);
                            }else{
                                refreshAvailableData(activityName,activityStatus,activityDepartmentId,activityGrade,currentDepartmentId,currentGrade);
                            }
                        }

                        public void onNothingSelected(AdapterView<?> arg0) {
                            Toast.makeText(MainActivity.this,"无",Toast.LENGTH_LONG).show();
                        }

                    });
                    //可参与
                    checkBox.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            activityStatus = String.valueOf(statusSpinner.getSelectedItemId());
                            activityGrade = String.valueOf(gradeSpinner.getSelectedItemId());
                            activityDepartmentId = String.valueOf(departmentSpinner.getSelectedItemId());
                            if(!checkBox.isChecked()){
                                refreshHomeData(activityName,activityStatus,activityDepartmentId,activityGrade);
                            }else{
                                refreshAvailableData(activityName,activityStatus,activityDepartmentId,activityGrade,currentDepartmentId,currentGrade);
                            }
                        }
                    });
                    break;
                case 1:
                    adapter = new HomeAdapter(activityList);
                    activityListView = (ListView)findViewById(R.id.home_listView);
                    activityListView.setAdapter(adapter);
                    break;
                case 2:
                    //加载左侧导航栏信息
                    leftNavNicknameTv = (TextView)findViewById(R.id.left_nav_nickname);
                    leftNavMobileTv = (TextView)findViewById(R.id.left_nav_mobile);
                    leftNavMobileTv.setText(currentUserInfo.getMobile());
                    leftNavNicknameTv.setText(currentUserInfo.getNickname());
                    HomeAdapter noticeAdapter = new HomeAdapter(noticeList);
                    noticeListView = (ListView)findViewById(R.id.notice_listView);
                    noticeListView.setAdapter(noticeAdapter);
                    noticeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Activity activity = noticeList.get(position);
                            Intent intent = new Intent(MainActivity.this,NoticeInfoActivity.class);
                            intent.putExtra("activity",activity);//Activity类实现了Serializable接口，才可以传。
                            startActivity(intent);
                        }
                    });
                    break;
                case 5:
                    adapter = new HomeAdapter(myActivityList);
                    myActivityLisyView = (ListView)findViewById(R.id.message_listView);
                    myActivityLisyView.setAdapter(adapter);
                    myActivityLisyView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            if(currentTv == 1){
                                Activity activity = myActivityList.get(position);
                                Intent intent = new Intent(MainActivity.this,SignUpActivity.class);
                                intent.putExtra("activity",activity);//Activity类实现了Serializable接口，才可以传。
                                startActivity(intent);
                            }
                        }
                    });

                    //监听
                    myActivity = (TextView)findViewById(R.id.message_activity);
                    myComment = (TextView)findViewById(R.id.message_comment);
                    myActivity.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            initMessageData();
                            myActivity.setBackgroundColor(Color.WHITE);
                            myComment.setBackgroundColor(getResources().getColor(R.color.colorPrimary,null));
                            currentTv = 1;
                        }
                    });
                    myComment.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            initComments();
                            myActivity.setBackgroundColor(getResources().getColor(R.color.colorPrimary,null));
                            myComment.setBackgroundColor(Color.WHITE);
                            currentTv = 2;
                        }
                    });
                    break;
                case 6:
                    commentAdapter = new MyAdapter(MainActivity.this,commentList);
                    commentListView = (ListView)findViewById(R.id.message_listView);
                    commentListView.setAdapter(commentAdapter);
                    commentListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            httpUrl = CodeUtil.url + "/select_activity_by_comment";
                            FormBody.Builder formBody = new FormBody.Builder();//创建表单请求体
                            formBody.add("commentId",commentList.get(position).getId().toString());
                            RequestBody requestBody = formBody.build();
                            HttpUtil.sendOkHttpRequestWithRequestBody(httpUrl,requestBody,new okhttp3.Callback(){

                                @Override
                                public void onFailure(Call call, IOException e) {

                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    Gson gson = new Gson();
                                    commentActivity = gson.fromJson(response.body().string(), Activity.class);
                                    homeHandler.obtainMessage(99).sendToTarget();
                                }
                            });

                        }
                    } );

                    //监听
                    myActivity = (TextView)findViewById(R.id.message_activity);
                    myComment = (TextView)findViewById(R.id.message_comment);
                    myActivity.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            initMessageData();
                            myActivity.setBackgroundColor(Color.WHITE);
                            myComment.setBackgroundColor(getResources().getColor(R.color.colorPrimary,null));
                            currentTv = 1;
                        }
                    });
                    myComment.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            initComments();
                            myActivity.setBackgroundColor(getResources().getColor(R.color.colorPrimary,null));
                            myComment.setBackgroundColor(Color.WHITE);
                            currentTv = 2;
                        }
                    });
                    break;
                case 99:
                    Intent intent = new Intent();
                    if(commentActivity.getType() == 1){
                        intent.setClass(MainActivity.this,SignUpActivity.class);
                        intent.putExtra("activity",commentActivity);
                    }else{
                        intent.setClass(MainActivity.this,NoticeInfoActivity.class);
                        intent.putExtra("activity",commentActivity);
                    }
                    startActivity(intent);
                    break;
                default:
                    break;
            }
        }

    };
    //加载HomeFragment
    private void setHomeFragment()
    {
        initHomeData();
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        HomeFragment homeFragment = new HomeFragment();
        transaction.replace(R.id.main_content,homeFragment);
        transaction.commit();
    }
    //加载NoticeFragment
    private void setNoticeFragment()
    {
        initNoticeData();
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        NoticeFragment noticeFragment = new NoticeFragment();
        transaction.replace(R.id.main_content,noticeFragment);
        transaction.commit();
    }
    //加载MessageFragment
    private void setMessageFragment()
    {
        initMessageData();
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        MessageFragment messageFragment = new MessageFragment();
        transaction.replace(R.id.main_content,messageFragment);
        transaction.commit();
    }
    //加载
    private void initHomeData() {
        activityList = new ArrayList<Activity>();
//      httpUrl  = "http://litchiapi.jstv.com/api/GetFeeds?column=3&PageSize=20&pageIndex=1&val=100511D3BE5301280E0992C73A9DEC41";
       httpUrl = CodeUtil.url + "/init_activity";
        FormBody.Builder formBody = new FormBody.Builder();//创建表单请求体
        formBody.add("activityName","");
        RequestBody requestBody = formBody.build();
        HttpUtil.sendOkHttpRequestWithRequestBody(httpUrl,requestBody,new okhttp3.Callback(){

            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String imageUrl = "http://litchiapi.jstv.com/Attachs/Article/288328/65b28e724f3b4a65ae7bb3f5c97699ab_padmini.JPG";
                    Gson gson = new Gson();
                    List<Activity> activities = gson.fromJson(response.body().string(),new TypeToken<List<Activity>>(){}.getType());
                    for(Activity activity : activities){
//                        activity.setImageUrl(imageUrl);
                        if(!TextUtils.isEmpty(activity.getTime()))
                        Log.e("time",activity.getTime());
                        activityList.add(activity);
                    }

                homeHandler.obtainMessage(0).sendToTarget();
            }
        });

    }

    //刷新 String activityName,String status,String type,String departmentId,String admission
    private void refreshHomeData(String activityName,String activityStatus,String activityDepartmentId,String activityGrade) {
        if(TextUtils.isEmpty(activityName)){
            activityName = "";
        }
        activityList = new ArrayList<Activity>();
//      httpUrl  = "http://litchiapi.jstv.com/api/GetFeeds?column=3&PageSize=20&pageIndex=1&val=100511D3BE5301280E0992C73A9DEC41";
        httpUrl = CodeUtil.url + "/refresh_activity";
        FormBody.Builder formBody = new FormBody.Builder();//创建表单请求体
        formBody.add("activityName",activityName);
        formBody.add("status",activityStatus);
//        formBody.add("type",activityType);
        formBody.add("departmentId",activityDepartmentId);
        formBody.add("grade",activityGrade);
        RequestBody requestBody = formBody.build();
        HttpUtil.sendOkHttpRequestWithRequestBody(httpUrl,requestBody,new okhttp3.Callback(){

            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String imageUrl = "http://litchiapi.jstv.com/Attachs/Article/288328/65b28e724f3b4a65ae7bb3f5c97699ab_padmini.JPG";
                Gson gson = new Gson();
                List<Activity> activities = gson.fromJson(response.body().string(),new TypeToken<List<Activity>>(){}.getType());
                activityList.clear();
                    activityList = activities;

                homeHandler.obtainMessage(1).sendToTarget();
            }
        });

    }

    private void refreshAvailableData(String activityName,String activityStatus,String activityDepartmentId,String activityGrade,String currentDepartmentId,String currentGrade) {
        if(TextUtils.isEmpty(activityName)){
            activityName = "";
        }
        activityList = new ArrayList<Activity>();
//      httpUrl  = "http://litchiapi.jstv.com/api/GetFeeds?column=3&PageSize=20&pageIndex=1&val=100511D3BE5301280E0992C73A9DEC41";
        httpUrl = CodeUtil.url + "/available_activity";
        FormBody.Builder formBody = new FormBody.Builder();//创建表单请求体
        formBody.add("activityName",activityName);
        formBody.add("status",activityStatus);
        formBody.add("departmentId",activityDepartmentId);
        formBody.add("grade",activityGrade);
        formBody.add("currentDepartmentId",currentDepartmentId);
        formBody.add("currentGrade",currentGrade);
        RequestBody requestBody = formBody.build();
        HttpUtil.sendOkHttpRequestWithRequestBody(httpUrl,requestBody,new okhttp3.Callback(){

            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String imageUrl = "http://litchiapi.jstv.com/Attachs/Article/288328/65b28e724f3b4a65ae7bb3f5c97699ab_padmini.JPG";
                Gson gson = new Gson();
                List<Activity> activities = gson.fromJson(response.body().string(),new TypeToken<List<Activity>>(){}.getType());
                activityList.clear();
                for(Activity activity : activities){
//                        activity.setImageUrl(imageUrl);
//                    if(!TextUtils.isEmpty(activity.getTime()))
//                        Log.e("time",activity.getTime());
                    activityList.add(activity);
                }

                homeHandler.obtainMessage(1).sendToTarget();
            }
        });

    }

    //加载
    private void initNoticeData() {
        noticeList = new ArrayList<Activity>();
//      httpUrl  = "http://litchiapi.jstv.com/api/GetFeeds?column=3&PageSize=20&pageIndex=1&val=100511D3BE5301280E0992C73A9DEC41";
        httpUrl = CodeUtil.url + "/select_notice";
        FormBody.Builder formBody = new FormBody.Builder();//创建表单请求体
        formBody.add("activityName","");
        RequestBody requestBody = formBody.build();
        HttpUtil.sendOkHttpRequestWithRequestBody(httpUrl,requestBody,new okhttp3.Callback(){

            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String imageUrl = "http://litchiapi.jstv.com/Attachs/Article/288328/65b28e724f3b4a65ae7bb3f5c97699ab_padmini.JPG";
                Gson gson = new Gson();
                List<Activity> activities = gson.fromJson(response.body().string(),new TypeToken<List<Activity>>(){}.getType());
                for(Activity activity : activities){
//                        activity.setImageUrl(imageUrl);
                    if(!TextUtils.isEmpty(activity.getTime()))
                        Log.e("time",activity.getTime());
                    noticeList.add(activity);
                }

                homeHandler.obtainMessage(2).sendToTarget();
            }
        });

    }

    private void initMessageData() {
        myActivityList = new ArrayList<Activity>();
//      httpUrl  = "http://litchiapi.jstv.com/api/GetFeeds?column=3&PageSize=20&pageIndex=1&val=100511D3BE5301280E0992C73A9DEC41";
        httpUrl = CodeUtil.url + "/my_activity";
        FormBody.Builder formBody = new FormBody.Builder();//创建表单请求体
        formBody.add("userId",currentUserInfo.getUserId().toString());
        RequestBody requestBody = formBody.build();
        HttpUtil.sendOkHttpRequestWithRequestBody(httpUrl,requestBody,new okhttp3.Callback(){

            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String imageUrl = "http://litchiapi.jstv.com/Attachs/Article/288328/65b28e724f3b4a65ae7bb3f5c97699ab_padmini.JPG";
                Gson gson = new Gson();
                List<Activity> activities = gson.fromJson(response.body().string(),new TypeToken<List<Activity>>(){}.getType());
               /* for(Activity activity : activities){
//                        activity.setImageUrl(imageUrl);
                    if(!TextUtils.isEmpty(activity.getTime()))
                        Log.e("time",activity.getTime());
                    activityList.add(activity);
                }*/
               myActivityList = activities;

                homeHandler.obtainMessage(5).sendToTarget();
            }
        });

    }

    private void initComments() {
        commentList = new ArrayList<Comment>();
//      httpUrl  = "http://litchiapi.jstv.com/api/GetFeeds?column=3&PageSize=20&pageIndex=1&val=100511D3BE5301280E0992C73A9DEC41";
        httpUrl = CodeUtil.url + "/select_comment";
        FormBody.Builder formBody = new FormBody.Builder();//创建表单请求体
        formBody.add("createrId", currentUserInfo.getUserId().toString());
        RequestBody requestBody = formBody.build();
        HttpUtil.sendOkHttpRequestWithRequestBody(httpUrl, requestBody, new okhttp3.Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                homeHandler.obtainMessage(0).sendToTarget();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String imgUrl = "http://litchiapi.jstv.com/Attachs/Article/288328/65b28e724f3b4a65ae7bb3f5c97699ab_padmini.JPG";
                Gson gson = new Gson();
                List<Comment> comments = gson.fromJson(response.body().string(), new TypeToken<List<Comment>>() {
                }.getType());
                    commentList = comments;

                homeHandler.obtainMessage(6).sendToTarget();
            }
        });
    }

    private List<Activity> getAvailableActivityList(){
        if(TextUtils.isEmpty(activityName)){
            activityName = "";
        }
        activityList = new ArrayList<Activity>();
//      httpUrl  = "http://litchiapi.jstv.com/api/GetFeeds?column=3&PageSize=20&pageIndex=1&val=100511D3BE5301280E0992C73A9DEC41";
        httpUrl = CodeUtil.url + "/real_available_activity";
        FormBody.Builder formBody = new FormBody.Builder();//创建表单请求体
        formBody.add("currentDepartmentId",currentUserInfo.getDepartmentId().toString());
        formBody.add("currentGrade",String.valueOf(GradeUtil.currentGrade(Integer.valueOf(currentUserInfo.getAdmission())) + 1));
        RequestBody requestBody = formBody.build();
        HttpUtil.sendOkHttpRequestWithRequestBody(httpUrl,requestBody,new okhttp3.Callback(){

            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String imageUrl = "http://litchiapi.jstv.com/Attachs/Article/288328/65b28e724f3b4a65ae7bb3f5c97699ab_padmini.JPG";
                Gson gson = new Gson();
                availableActivityList = gson.fromJson(response.body().string(),new TypeToken<List<Activity>>(){}.getType());
            }
        });
        return availableActivityList;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case 0:
                break;
            default:
                break;
        }
    }


    //三个Fragment
    public static class HomeFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState)
        {
            return inflater.inflate(R.layout.fragment_home, container, false);
        }
    }

    public static class NoticeFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState)
        {
            return inflater.inflate(R.layout.fragment_notice, container, false);
        }
    }

    public static class MessageFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState)
        {
            return inflater.inflate(R.layout.fragment_message, container, false);
        }
    }

}
