package com.sitech.iotftp.utils;

import org.json.JSONObject;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import javax.xml.crypto.Data;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class DateUtil {

    /**
     * 英文简写（默认）如：2010-12-01
     */
    public static String FORMAT_SHORT = "yyyy-MM-dd";

    /**
     * 英文全称  如：2010-12-01 23:15:06
     */
    public static String FORMAT_LONG = "yyyy-MM-dd HH:mm:ss";

    /**
     * 英文全称  如：20101201231506
     */
    public static String FORMAT_LONG_02 = "yyyyMMddHHmmss";


    /**
     * 精确到毫秒的完整时间    如：yyyy-MM-dd HH:mm:ss.S
     */
    public static String FORMAT_FULL = "yyyy-MM-dd HH:mm:ss.S";

    /**
     * 中文简写  如：2010年12月01日
     */
    public static String FORMAT_SHORT_CN = "yyyy年MM月dd";

    /**
     * 中文全称  如：2010年12月01日  23时15分06秒
     */
    public static String FORMAT_LONG_CN = "yyyy年MM月dd日  HH时mm分ss秒";

    /**
     * 精确到毫秒的完整中文时间
     */
    public static String FORMAT_FULL_CN = "yyyy年MM月dd日  HH时mm分ss秒SSS毫秒";


    /**
    * @Method:         getFormateDate
    * @Author:         WJH
    * @CreateDate:     2019/3/12 16:50
    * @UpdateUser:     WJH
    * @UpdateDate:     2019/3/12 16:50
    * @UpdateRemark:   获取当前时间，并且格式为：yyyyMMddHHmmss
    * @Version:        1.0
    */
    public static String getFormateDate(String startTime,String endTime){
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String result = sdf.format(now);
        result = result.substring(0,8)+startTime.replace(":","")+endTime.replace(":","");
        return result;
    }


    public static String getAsYMDHMS(){
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return sdf.format(now);
    }

    /**
    * @Method:         strToDate
    * @Author:         WJH
    * @CreateDate:     2019/3/12 16:49
    * @UpdateUser:     WJH
    * @UpdateDate:     2019/3/12 16:49
    * @UpdateRemark:   字符串转Date类型，格式为：yyyy-MM-dd HH:mm:ss
    * @Version:        1.0
    */
    public static Date strToDate(String dateStr){
        Date result = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            result = sdf.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
    * @Method:         betweenDates
    * @Author:         WJH
    * @CreateDate:     2019/3/12 16:48
    * @UpdateUser:     WJH
    * @UpdateDate:     2019/3/12 16:48
    * @UpdateRemark:   计算一天内的两个时间的时间差，并以 HH:mm:ss 的形式反馈
    * @Version:        1.0
    */
    public static String betweenDates(String dateSmaller, String dateBigger){
        SimpleDateFormat sdf01 = new SimpleDateFormat("HH:mm:ss");
        Date date01 = null;
        Date date02 = null;
        try {
            date01 = sdf01.parse(dateBigger);
            date02 = sdf01.parse(dateSmaller);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long ss = date01.getTime()-date02.getTime()-28800000;
        SimpleDateFormat sdf02 = new SimpleDateFormat("HH:mm:ss");
        String result = sdf02.format(ss);
        return result;
    }


    /**
    * @Method:         betweenDatesForMillis
    * @Author:         WJH
    * @CreateDate:     2019/3/28 13:28
    * @UpdateUser:     WJH
    * @UpdateDate:     2019/3/28 13:28
    * @UpdateRemark:   返回两个时间的时间差 毫秒
    * @Version:        1.0
    */
    public static long betweenDatesForMillis(String dateSmaller, String dateBigger){
        SimpleDateFormat sdf01 = new SimpleDateFormat("HH:mm:ss");
        Date date01 = null;
        Date date02 = null;
        try {
            date01 = sdf01.parse(dateBigger);
            date02 = sdf01.parse(dateSmaller);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long result = date01.getTime()-date02.getTime();
        return result;
    }


    /**
    * @Method:         getNowDate
    * @Author:         WJH
    * @CreateDate:     2019/3/14 15:11
    * @UpdateUser:     WJH
    * @UpdateDate:     2019/3/14 15:11
    * @UpdateRemark:   获取当前时间HH:mm:ss
    * @Version:        1.0
    */
    public static String getDateAsHMS(int seconds) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.SECOND, seconds);
        return sdf.format(calendar.getTime());
    }
    /**
    * @Method:         getWeek
    * @Author:         WJH
    * @CreateDate:     2019/3/14 15:11
    * @UpdateUser:     WJH
    * @UpdateDate:     2019/3/14 15:11
    * @UpdateRemark:   获取当前星期
    * @Version:        1.0
    */
    public static int getWeek() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        int reuslt = calendar.get(Calendar.DAY_OF_WEEK)-1;
        if (reuslt == 0)
            reuslt = 7;
        return reuslt;
    }

    /**
     * @Method:         getNowDate
     * @Author:         WJH
     * @CreateDate:     2019/3/14 15:11
     * @UpdateUser:     WJH
     * @UpdateDate:     2019/3/14 15:11
     * @UpdateRemark:   获取第一次执行定时任务的时间
     * @Version:        1.0
     */
    public static Date getNowDate(String startTime, String week) throws ParseException {
        //TODO 01 判断周几的任务
        Date date = new Date();
        SimpleDateFormat sdf01 = new SimpleDateFormat("HHmmss");
        SimpleDateFormat sdf02 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        String newTime = startTime.replace(":","");
        int dayOfWeek = (calendar.get(Calendar.DAY_OF_WEEK)-1);
        if (Integer.valueOf(week)>dayOfWeek||(Integer.valueOf(week)==dayOfWeek&&Integer.valueOf(newTime)>Integer.valueOf(sdf01.format(date)))){
            calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH)+(Integer.valueOf(week)-dayOfWeek));
        }else{
            calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH)+(Integer.valueOf(week)-dayOfWeek+7));
        }
        Date result = sdf02.parse(sdf02.format(calendar.getTime()).substring(0,11)+startTime);
        return result;
    }


    /**
    * @Method:         getAgoDate
    * @Author:         WJH
    * @CreateDate:     2019/4/3 17:22
    * @UpdateUser:     WJH
    * @UpdateDate:     2019/4/3 17:22
    * @UpdateRemark:   获取几天后的日期
    * @Version:        1.0
    */
    public static String getAgoDate(int number){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE, -number);
        return sdf.format(calendar.getTime());
    }
    
    /**
    * @Method:         compareTwoDate
    * @Author:         WJH
    * @CreateDate:     2019/4/4 10:32
    * @UpdateUser:     WJH
    * @UpdateDate:     2019/4/4 10:32
    * @UpdateRemark:   比较两个日期的大小
    * @Version:        1.0
    */
    public static boolean compareTwoDate(String firstDate, String secondDate){
        boolean result = false;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            result = sdf.parse(firstDate).before(sdf.parse(secondDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void main(String[] args) {
        String currentCellValue = "18310724310|18310724310";
        currentCellValue = currentCellValue.substring(0,11);
        System.out.println(currentCellValue);
    }


}
