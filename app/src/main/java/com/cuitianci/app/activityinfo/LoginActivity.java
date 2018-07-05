package com.cuitianci.app.activityinfo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cuitianci.app.activityinfo.DAO.PersonalLoginInfo;
import com.cuitianci.app.activityinfo.DAO.User;
import com.cuitianci.app.activityinfo.Util.CodeUtil;
import com.cuitianci.app.activityinfo.Util.HttpUtil;
import com.google.gson.Gson;

import org.litepal.LitePal;
import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

;import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends BaseActivity implements View.OnClickListener {
    private EditText usernameEdit;
    private EditText passwordEdit;
    private CheckBox rememberPwCheckBox;
    private CheckBox autoLoginCheckBox;
    private Button loginButton;
    private PersonalLoginInfo personalLoginInfo;
    private TextView resetPwTv;
    private static String newPw;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //数据库操作
        LitePal.getDatabase();
        setContentView(R.layout.activity_login);
        usernameEdit = (EditText)findViewById(R.id.login_username);
        passwordEdit = (EditText)findViewById(R.id.login_password);
        rememberPwCheckBox = (CheckBox)findViewById(R.id.login_checkbox);
        autoLoginCheckBox = (CheckBox)findViewById(R.id.auto_login_checkbox);
        loginButton = (Button)findViewById(R.id.login_login);
        resetPwTv = (TextView)findViewById(R.id.resetPw);
        //加载最近一条登陆信息
        personalLoginInfo = DataSupport.findLast(PersonalLoginInfo.class);
//        Log.e("dada",String.valueOf(personalLoginInfo.isAutoLogin()));
        if(personalLoginInfo != null){
            usernameEdit.setText(personalLoginInfo.getUsername());
            if(personalLoginInfo.isRememberPw()){
                passwordEdit.setText(personalLoginInfo.getPassword());
            }
            rememberPwCheckBox.setChecked(personalLoginInfo.isRememberPw());
        }
        //添加监听
        loginButton.setOnClickListener(this);
        resetPwTv.setOnClickListener(this);
        usernameEdit.addTextChangedListener(new TextWatcher() {
            String sourceInput;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                sourceInput = usernameEdit.getText().toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                /*String input = usernameEdit.getText().toString();
                Pattern pattern = Pattern.compile("[a-zA-Z]{1}[a-zA-Z0-9_]{1,15}");
                Matcher matcher = pattern.matcher(input);
                if(!matcher.matches()){
                    usernameEdit.setText(sourceInput);
                    return;
                }*/
            }

            @Override
            public void afterTextChanged(Editable s) {
                List<PersonalLoginInfo> personalLoginInfoList = DataSupport.where("username = ?",usernameEdit.getText().toString()).find(PersonalLoginInfo.class);
                if(personalLoginInfoList.size() == 1){
                    if(personalLoginInfoList.get(0).isRememberPw()){
                        passwordEdit.setText(personalLoginInfoList.get(0).getPassword());
                    }
                    rememberPwCheckBox.setChecked(personalLoginInfoList.get(0).isRememberPw());
                }else if(personalLoginInfoList.size() > 1){
                    Toast.makeText(LoginActivity.this,"数据库错误：username不唯一",Toast.LENGTH_SHORT).show();
                }else{
                }
            }
        });

    }
    public void onClick(View v){
        switch (v.getId()) {
            case R.id.login_login:{
                //判空
                if (TextUtils.isEmpty(usernameEdit.getText().toString())) {
                    Toast.makeText(LoginActivity.this, "请填写账号", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(passwordEdit.getText().toString())) {
                    Toast.makeText(LoginActivity.this, "请填写密码", Toast.LENGTH_SHORT).show();
                    return;
                }
                //创建/更新数据库（只要进行任意一次数据库操作，自动根据litePal.xml修改数据库，更新时，把版本号加一。）
                LitePal.getDatabase();
                //获取账号密码以及记住密码
                final String username = usernameEdit.getText().toString();
                final String password = passwordEdit.getText().toString();
                final boolean rememberPw = rememberPwCheckBox.isChecked();
                final boolean autoLogin = autoLoginCheckBox.isChecked();
                //要传输的数据
                FormBody.Builder formBody = new FormBody.Builder();//创建表单请求体
                formBody.add("username", username);//传递键值对参数
                RequestBody requestBody = formBody.build();
                HttpUtil.sendOkHttpRequestWithRequestBody(CodeUtil.url + "/login", requestBody, new okhttp3.Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(LoginActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, final Response response) throws IOException {
                        Gson gson = new Gson();
                        final User user = gson.fromJson(response.body().string(), User.class);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //获取response中的返回值
                                if (user == null) {
                                    Toast.makeText(LoginActivity.this, "该账号不存在", Toast.LENGTH_SHORT).show();
                                } else if (!username.equals(user.getUsername()) || !password.equals(user.getPassword())) {
                                    Toast.makeText(LoginActivity.this, "账号或密码错误", Toast.LENGTH_SHORT).show();
                                } else {
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    //删除该用户名之前的数据，重新保存，一是因为有可能有更改，二是为了保证最新登陆的数据处于数据库最后一条，以便默认显示。
//                                    DataSupport.deleteAll(PersonalLoginInfo.class,"1 = 1");
                                    DataSupport.deleteAll(PersonalLoginInfo.class, "username = ? or username = ''", username);
                                    personalLoginInfo = new PersonalLoginInfo();
                                    personalLoginInfo.setUserId(user.getId());
                                    personalLoginInfo.setUsername(user.getUsername());
                                    personalLoginInfo.setNickname(user.getNickname());
                                    personalLoginInfo.setRealName(user.getRealName());
                                    personalLoginInfo.setPassword(user.getPassword());
                                    personalLoginInfo.setSalt(user.getSalt());
                                    personalLoginInfo.setMobile(user.getMobile());
                                    personalLoginInfo.setType(user.getType());
                                    personalLoginInfo.setStatus(user.getStatus());
                                    personalLoginInfo.setShowName(user.getShowName());
                                    personalLoginInfo.setAdmission(user.getAdmission());
                                    personalLoginInfo.setDepartmentId(user.getDepartmentId());
                                    personalLoginInfo.setDepartment(user.getDepartment());
                                    personalLoginInfo.setImgUrl(user.getImgUrl());
                                    personalLoginInfo.setRoleId(user.getRoleId());
                                    personalLoginInfo.setRole(user.getRole());
                                    personalLoginInfo.setRememberPw(rememberPw);
                                    personalLoginInfo.setAutoLogin(autoLogin);
                                    personalLoginInfo.save();
                                    finish();
                                }

                            }
                        });
                    }
                });
                break;
        }
            case R.id.resetPw: {
                if (TextUtils.isEmpty(usernameEdit.getText().toString())) {
                    Toast.makeText(LoginActivity.this, "请输入账号", Toast.LENGTH_SHORT).show();
                    break;
                }
                //要传输的数据
                FormBody.Builder formBodyTest = new FormBody.Builder();//创建表单请求体
                formBodyTest.add("username", usernameEdit.getText().toString());//传递键值对参数
                RequestBody requestBodyTest = formBodyTest.build();
                HttpUtil.sendOkHttpRequestWithRequestBody(CodeUtil.url + "/login", requestBodyTest, new okhttp3.Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        Gson gson = new Gson();
                        final User user = gson.fromJson(response.body().string(), User.class);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (user == null) {
                                    Toast.makeText(LoginActivity.this, "该账号不存在", Toast.LENGTH_SHORT).show();
                                }else{
                                    final String usernameForReset = usernameEdit.getText().toString();
                                    final View view = getLayoutInflater().inflate(R.layout.layout_reset_pw, null);
                                    final AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                                    builder.setView(view);
                                    builder.setPositiveButton("确认", null);//确认的点击事件另写。
                                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            //默认为关闭对话框
                                        }
                                    });
                                    final AlertDialog resetDialog = builder.create();
                                    resetDialog.show();
                                    resetDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            final TextView dialogMessage = ((TextView) ((ViewGroup) view).getChildAt(0));
                                            final EditText dialogInput = ((EditText) ((ViewGroup) view).getChildAt(1));
                                            if (TextUtils.isEmpty(dialogInput.getText().toString())) {
                                                Toast.makeText(LoginActivity.this, "请输入密码", Toast.LENGTH_SHORT).show();
                                            } else {

                                                if ("请输入原始密码".equals(dialogMessage.getText().toString())) {
                                                    //要传输的数据
                                                    FormBody.Builder formBody = new FormBody.Builder();//创建表单请求体
                                                    formBody.add("username", usernameForReset);//传递键值对参数
                                                    RequestBody requestBody = formBody.build();
                                                    HttpUtil.sendOkHttpRequestWithRequestBody(CodeUtil.url + "/login", requestBody, new okhttp3.Callback() {
                                                        @Override
                                                        public void onFailure(Call call, IOException e) {
                                                            runOnUiThread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    Toast.makeText(LoginActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                        }
                                                        @Override
                                                        public void onResponse(Call call, Response response) throws IOException {
                                                            Gson gson = new Gson();
                                                            final User user = gson.fromJson(response.body().string(), User.class);
                                                            runOnUiThread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    if (user.getPassword().equals(dialogInput.getText().toString())) {
                                                                        dialogMessage.setText("请输入新的密码");
                                                                        dialogInput.setText("");
                                                                    } else {
                                                                        Toast.makeText(LoginActivity.this, "密码错误", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    });
                                                } else if ("请输入新的密码".equals(dialogMessage.getText().toString())) {
                                                    newPw = dialogInput.getText().toString();
                                                    dialogMessage.setText("请确认您的密码");
                                                    dialogInput.setText("");
                                                } else {
                                                    if (!TextUtils.isEmpty(newPw) && newPw.equals(dialogInput.getText().toString())) {
                                                        //要传输的数据
                                                        FormBody.Builder formBodyReset = new FormBody.Builder();//创建表单请求体
                                                        formBodyReset.add("username", usernameForReset);//传递键值对参数
                                                        formBodyReset.add("password", newPw);//传递键值对参数
                                                        RequestBody requestBodyReset = formBodyReset.build();
                                                        HttpUtil.sendOkHttpRequestWithRequestBody(CodeUtil.url + "/reset_password", requestBodyReset, new okhttp3.Callback(){

                                                            @Override
                                                            public void onFailure(Call call, IOException e) {

                                                            }

                                                            @Override
                                                            public void onResponse(Call call, final Response response) throws IOException {
                                                                final String responseData = response.body().string();
                                                                runOnUiThread(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                            if("success".equals(responseData)){
                                                                                Toast.makeText(LoginActivity.this,"密码修改成功",Toast.LENGTH_SHORT).show();
                                                                                resetDialog.dismiss();
                                                                            }else{
                                                                                Toast.makeText(LoginActivity.this,"未知错误，修改失败",Toast.LENGTH_SHORT).show();
                                                                                resetDialog.dismiss();
                                                                            }
                                                                    }
                                                                });
                                                            }
                                                        });
                                                    } else {
                                                        Toast.makeText(LoginActivity.this, "两次密码不一致", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            }

                                        }
                                    });
                                }
                            }
                        });
                    }
                });
                break;
            }
                default:
        }
    }
}
