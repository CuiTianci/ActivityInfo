 package com.cuitianci.app.activityinfo;

import android.app.DatePickerDialog;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.cuitianci.app.activityinfo.DAO.Activity;
import com.cuitianci.app.activityinfo.DAO.PersonalLoginInfo;
import com.cuitianci.app.activityinfo.Util.CodeUtil;
import com.cuitianci.app.activityinfo.Util.HttpUtil;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

 public class NoticeAddActivity extends AppCompatActivity implements View.OnClickListener{
     private Activity receivedActivity;
     private String addOrEdit;
     private EditText name, summary, description, startTime, endTime, organization, mobile;
     private Spinner department, grade;
     private Button button;
     private String whichTime = "1";
     private int mYear, mMonth, mDay;
     //当前登陆人
     private final PersonalLoginInfo currentUser = DataSupport.findLast(PersonalLoginInfo.class);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice_add);
        //绑定
        name = (EditText) findViewById(R.id.add_notice_name);
        summary = (EditText) findViewById(R.id.add_notice_summnary);
        description = (EditText) findViewById(R.id.add_notice_description);
        startTime = (EditText) findViewById(R.id.add_notice_start_time);
        endTime = (EditText) findViewById(R.id.add_notice_end_time);
        organization = (EditText) findViewById(R.id.notice_add_organization);
        mobile = (EditText) findViewById(R.id.add_notice_mobile);
        department = (Spinner) findViewById(R.id.add_notice_department_spinner);
        grade = (Spinner) findViewById(R.id.add_notice_grade_spinner);
        button = (Button) findViewById(R.id.add_notice_button);
        //添加点击监听
        startTime.setOnClickListener(this);
        endTime.setOnClickListener(this);
        button.setOnClickListener(this);
        //设置时间显示
        Calendar ca = Calendar.getInstance();
        mYear = ca.get(Calendar.YEAR);
        mMonth = ca.get(Calendar.MONTH);
        mDay = ca.get(Calendar.DAY_OF_MONTH);

        addOrEdit = getIntent().getStringExtra("addOrEdit");
        if(addOrEdit.equals("edit")){
            receivedActivity = (Activity) getIntent().getSerializableExtra("activity");
            name.setText(receivedActivity.getActivityName());
            summary.setText(receivedActivity.getSummary());
            description.setText(receivedActivity.getActivityDescription());
            startTime.setText(receivedActivity.getStartTime());
            endTime.setText(receivedActivity.getEndTime());
            organization.setText(receivedActivity.getOrganization());
            mobile.setText(receivedActivity.getMobile());
            grade.setSelection(receivedActivity.getGrade() - 1);
            department.setSelection(Integer.valueOf(String.valueOf(receivedActivity.getDepartmentId())) - 1);

        }
        //加载spinner
        initSpinners();
    }

     //Handler
     private Handler handler = new Handler() {
         @Override
         public void handleMessage(Message msg) {
             super.handleMessage(msg);
             switch (msg.what) {
                 case 0:
                     break;
                 case 1:
                     Toast.makeText(NoticeAddActivity.this,"保存成功",Toast.LENGTH_SHORT).show();
                     finish();
                     break;
                 default:
                     break;
             }
         }
     };

     @Override
     public void onClick(View v) {
         switch (v.getId()) {
             case R.id.add_notice_start_time:
                 showTimePicker();
                 whichTime = "1";
                 break;
             case R.id.add_notice_end_time:
                 showTimePicker();
                 whichTime = "2";
                 break;
             case R.id.add_notice_button:
                 saveData();
             default:
                 break;
         }
     }

     //打开时间选择对话框
     private void showTimePicker() {
         new DatePickerDialog(NoticeAddActivity.this, onDateSetListener, mYear, mMonth, mDay).show();
     }

     //时间对话框监听
     private DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {

         @Override
         public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
             mYear = year;
             mMonth = monthOfYear;
             mDay = dayOfMonth;
             String date;
             if (mMonth + 1 < 10) {
                 if (mDay < 10) {
                     date = new StringBuffer().append(mYear).append("-").append("0").
                             append(mMonth + 1).append("-").append("0").append(mDay).toString();
                 } else {
                     date = new StringBuffer().append(mYear).append("-").append("0").
                             append(mMonth + 1).append("-").append(mDay).toString();
                 }

             } else {
                 if (mDay < 10) {
                     date = new StringBuffer().append(mYear).append("-").
                             append(mMonth + 1).append("月").append("0").append(mDay).toString();
                 } else {
                     date = new StringBuffer().append(mYear).append("-").
                             append(mMonth + 1).append("-").append(mDay).toString();
                 }

             }
             if (whichTime.equals("1")) {
                 startTime.setText(date);
             } else {
                 endTime.setText(date);
             }
         }
     };

     //加载spinner
     private void initSpinners() {
         //学院
         ArrayAdapter<String> department_arr_adapter;
         //数据
         ArrayList<String> department_data_list = new ArrayList<String>();
         department_data_list.add("所有学院");
         department_data_list.add("软件学院");
         department_data_list.add("电信学院");
         department_data_list.add("电控学院");
         //适配器
         department_arr_adapter = new ArrayAdapter<String>(NoticeAddActivity.this, android.R.layout.simple_spinner_item, department_data_list);
         //设置样式
         department_arr_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         //加载适配器
         department.setAdapter(department_arr_adapter);
         //年级
         ArrayAdapter<String> grade_arr_adapter;
         //数据
         ArrayList<String> grade_data_list = new ArrayList<String>();
         grade_data_list.add("全部年级");
         grade_data_list.add("大一");
         grade_data_list.add("大二");
         grade_data_list.add("大三");
         grade_data_list.add("大四");
         //适配器
         grade_arr_adapter = new ArrayAdapter<String>(NoticeAddActivity.this, android.R.layout.simple_spinner_item, grade_data_list);
         //设置样式
         grade_arr_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         //加载适配器
         grade.setAdapter(grade_arr_adapter);
     }

     private void saveData() {
         if (TextUtils.isEmpty(name.getText().toString()) || TextUtils.isEmpty(summary.getText().toString()) || TextUtils.isEmpty(description.getText().toString()) || TextUtils.isEmpty(startTime.getText().toString()) ||
                 TextUtils.isEmpty(endTime.getText().toString()) || TextUtils.isEmpty(organization.getText().toString()) || TextUtils.isEmpty(mobile.getText().toString())){
             Toast.makeText(NoticeAddActivity.this,"数据不完整，保存失败",Toast.LENGTH_SHORT).show();
             return ;
         }
         String httpUrl = CodeUtil.url + "/save_activity";
         FormBody.Builder formBody = new FormBody.Builder();//创建表单请求体
         formBody.add("activityName",name.getText().toString() );
         formBody.add("summary", summary.getText().toString());
         formBody.add("activityDescription", description.getText().toString());
         formBody.add("startTime", startTime.getText().toString());
         formBody.add("endTime", endTime.getText().toString());
         formBody.add("organization", organization.getText().toString());
         formBody.add("mobile", mobile.getText().toString());
         formBody.add("departmentId", String.valueOf(department.getSelectedItemId() + 1));
         formBody.add("department", String.valueOf(department.getSelectedItem()));
         formBody.add("grade", String.valueOf(grade.getSelectedItemId() + 1));
         formBody.add("createrId", String.valueOf(currentUser.getUserId()));
         formBody.add("creater", String.valueOf(currentUser.getRealName()));
         formBody.add("type", "2");
         formBody.add("status","1");
         if(addOrEdit.equals("add")){
             httpUrl = CodeUtil.url + "/save_activity";
         }else{
             httpUrl = CodeUtil.url + "/edit_activity";
             formBody.add("id", String.valueOf(receivedActivity.getId()));
         }
         RequestBody requestBody = formBody.build();
         HttpUtil.sendOkHttpRequestWithRequestBody(httpUrl, requestBody, new okhttp3.Callback() {

             @Override
             public void onFailure(Call call, IOException e) {

             }

             @Override
             public void onResponse(Call call, Response response) throws IOException {
                 handler.obtainMessage(1).sendToTarget();
             }
         });
     }
 }
