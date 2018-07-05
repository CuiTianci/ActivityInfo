package com.cuitianci.app.activityinfo;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.cuitianci.app.activityinfo.Adapter.UserAdapter;
import com.cuitianci.app.activityinfo.DAO.Activity;
import com.cuitianci.app.activityinfo.DAO.ActivityUser;
import com.cuitianci.app.activityinfo.DAO.User;
import com.cuitianci.app.activityinfo.Util.CodeUtil;
import com.cuitianci.app.activityinfo.Util.HttpUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ActivityUserListActivity extends AppCompatActivity {

    private List<User> userList;
    private String activityId;
    private ListView listView;
    private UserAdapter userAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        activityId = getIntent().getStringExtra("activityId");
        initListView(activityId);
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            switch (msg.what){
                case 0:
                    break;
                case 1:
                    listView = (ListView)findViewById(R.id.activity_user_list_lv);
                    userAdapter = new UserAdapter(ActivityUserListActivity.this,userList);
                    listView.setAdapter(userAdapter);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            User user = userList.get(position);
                            Intent intent = new Intent(ActivityUserListActivity.this,UserInfoActivity.class);
                            intent.putExtra("user",user);//Activity类实现了Serializable接口，才可以传。
                            startActivity(intent);
                        }
                    });
                    break;
                default:
                    break;
            }
        }
    };

    private void initListView(String activityId){
       String httpUrl = CodeUtil.url + "/get_activity_user_list";
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
                userList = gson.fromJson(response.body().string(),new TypeToken<List<User>>(){}.getType());
                handler.obtainMessage(1).sendToTarget();
            }
        });
    }
}
