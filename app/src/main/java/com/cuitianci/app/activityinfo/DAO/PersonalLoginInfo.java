package com.cuitianci.app.activityinfo.DAO;

import org.litepal.crud.DataSupport;

import java.util.Date;

/**
 * Created by 77214 on 2018/5/26.
 */

public class PersonalLoginInfo extends DataSupport{
    private Long userId;

    private String username;

    private String nickname;

    private String realName;

    private String password;

    private String salt;

    private String mobile;

    private Integer type;

    private Integer status;

    private Integer showName;

    private String admission;

    private Long departmentId;

    private String department;

    private String imgUrl;

    private Long roleId;

    private String role;

    public String getUsername() {
        return username;
    }

    //非用户表中的内容
    private boolean rememberPw;
    private boolean autoLogin;


    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getShowName() {
        return showName;
    }

    public void setShowName(Integer showName) {
        this.showName = showName;
    }

    public String getAdmission() {
        return admission;
    }

    public void setAdmission(String admission) {
        this.admission = admission;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public boolean isRememberPw() {
        return rememberPw;
    }

    public void setRememberPw(boolean rememberPw) {
        this.rememberPw = rememberPw;
    }

    public boolean isAutoLogin() {
        return autoLogin;
    }

    public void setAutoLogin(boolean autoLogin) {
        this.autoLogin = autoLogin;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
