package com.cuitianci.app.activityinfo.Util;

import java.util.Calendar;

/**
 * Created by 77214 on 2018/6/15.
 */

public class GradeUtil {

    public static int currentGrade(int admission) {
        Calendar ca = Calendar.getInstance();
        int mYear = ca.get(Calendar.YEAR);
        int mMonth = ca.get(Calendar.MONTH);
        if (mMonth >= 9) {
            if (mYear - admission == 0) {
                return 1;
            }
            if (mYear - admission == 1) {
                return 2;
            }
            if (mYear - admission == 2) {
                return 3;
            }
            if (mYear - admission == 3) {
                return 4;
            }
        } else {
            if (mYear - admission == 1) {
                return 1;
            }
            if (mYear - admission == 2) {
                return 2;
            }
            if (mYear - admission == 3) {
                return 3;
            }
            if (mYear - admission == 4) {
                return 4;
            }
        }
        return 0;
    }
    //年级code转换
    public static String gradeCodeToTag(int gradeCode){
        if(gradeCode == 1){
            return "全部年级";
        }
        if(gradeCode == 2){
            return "大一";
        }
        if(gradeCode == 3){
            return "大二";
        }
        if(gradeCode == 4){
            return "大三";
        }
        if(gradeCode == 5){
            return "大四";
        }
        return "";
    }

    public static String departmentCodeToTag(int departmentCode){
        if(departmentCode == 1){
            return "所有学院";
        }
        if(departmentCode == 2){
            return "软件学院";
        }
        if(departmentCode == 3){
            return "电信学院";
        }
        if(departmentCode == 4){
            return "电控学院";
        }
        return "";
    }
}
