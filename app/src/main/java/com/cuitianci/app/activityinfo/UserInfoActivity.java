package com.cuitianci.app.activityinfo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.cuitianci.app.activityinfo.DAO.User;

public class UserInfoActivity extends AppCompatActivity {
    private TextView name,nickname,mobile,department,grade;
    User receivedUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        receivedUser = (User)getIntent().getSerializableExtra("user");
        name = (TextView)findViewById(R.id.user_info_name_tv);
        nickname = (TextView)findViewById(R.id.user_info_nickname_tv);
        mobile = (TextView)findViewById(R.id.user_info_mobile_tv);
        department = (TextView)findViewById(R.id.user_info_department_tv);
        grade = (TextView)findViewById(R.id.user_info_grade_tv);

        name.setText(receivedUser.getRealName());
        nickname.setText(receivedUser.getNickname());
        mobile.setText(receivedUser.getMobile());
        department.setText(receivedUser.getDepartment());
        grade.setText(receivedUser.getAdmission());

        mobile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if(ContextCompat.checkSelfPermission(UserInfoActivity.this,Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
                   ActivityCompat.requestPermissions(UserInfoActivity.this,new String[]{Manifest.permission.CALL_PHONE},1);
               }else{
                   call();
               }
            }
        });
    }
    private void call(){
        try{
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + mobile.getText()));
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
                    Toast.makeText(UserInfoActivity.this,"你拒绝了该权限",Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }
}



