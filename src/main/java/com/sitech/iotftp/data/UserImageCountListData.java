package com.sitech.iotftp.data;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class UserImageCountListData {

    //扫脸人员
    public static Map<String,List<Map>> identificationList = new HashMap<>();
    //签到人员
    public static Map<String,List<Map>> checkList = new HashMap<>();
    //最早签到人员
    public static Map<String,List<Map>> firstList = new HashMap<>();
    //最晚签退人员
    public static Map<String,List<Map>> latestList = new HashMap<>();
    //总人数
    public static Map<String,Integer> userCount = new HashMap<>();
    //每6个小时人数
    public static Map<String,int[]> hourCount = new HashMap<>();

    static {
        identificationList.put("a21001",new ArrayList<>());
        checkList.put("a21001",new ArrayList<>());
        firstList.put("a21001",new ArrayList<>());
        latestList.put("a21001",new ArrayList<>());
        userCount.put("a21001",0);
        hourCount.put("a21001",new int[8]);
    }
}
