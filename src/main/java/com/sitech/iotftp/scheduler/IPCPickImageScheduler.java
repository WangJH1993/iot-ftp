package com.sitech.iotftp.scheduler;

import com.sitech.iotftp.data.UserImageCountListData;
import com.sitech.iotftp.webSocket.WebSocketServer;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;

@Component
public class IPCPickImageScheduler {

    private static Logger logger = Logger.getLogger(IPCPickImageScheduler.class);

    @Autowired
    private HttpSender httpSender;
    @Autowired
    private WebSocketServer webSocketServer;

    @Value("${BASEPATH}")
    private String basePath;

    private final static long pickFrequency = 500;  //每隔1S从文件系统中读取


//    @Scheduled(fixedRate = pickFrequency)
    public void startPick(){
        //TODO 1 查询所有照片并删除
        try {
//            String basePath = "C:\\Users\\25676\\Desktop\\自创文件\\1";
            //进入根目录
            File file = new File(basePath);
            Map<String,List<File>> map = new HashMap<>();
            //获取根目录下子节点
            File[] names = file.listFiles();
            //依次进入每个checkIn目录和 checkOut目录
            for (File str: names) {
                if (!str.isHidden()){
                    file = new File(basePath+"/"+str.getName());
                    //递归查询该目录下所有图片，并将图片存放到map中
                    List<File> files = recursiveForImages(file);
                    if (files.size()>0){
                        map.put(str.getName(),files);
                    }
                }
            }
            //TODO 3 异步发给第三方
            httpSender.process(map);
        } catch (Exception e) {
            logger.error("文件获取失败："+e.getMessage());
        } finally {
        }
    }

    /**
    * @Method:         recursiveForImages
    * @Author:         WJH
    * @CreateDate:     2019/7/27 14:22
    * @UpdateUser:     WJH
    * @UpdateDate:     2019/7/27 14:22
    * @UpdateRemark:   递归查询指定目录下的所有图片
    * @Version:        1.0
    */
    private List<File> recursiveForImages(File file) {
        List<File> fileList = new ArrayList<>();
        File[] files = file.listFiles();
        for (File f : files) {
            if (!f.isFile() && !f.isHidden()){
                fileList.addAll(recursiveForImages(f));
            }else if (!f.isHidden() && (f.getName().endsWith("jpg") || f.getName().endsWith("png"))){
                fileList.add(f);
            }
        }
        return fileList;
    }


//    每个小时执行一次,将客流量发送给前端
//    @Scheduled(cron = "0 0 0 * * ? ")
    public void hourCount(){
        //清除签到人员数组
        Set<Map.Entry<String, List<Map>>> firstSet = UserImageCountListData.firstList.entrySet();
        for (Map.Entry<String, List<Map>> map:firstSet) {
            map.getValue().clear();
        }
        logger.info("签到人员数组已清空");
        //清除总人流量
        Set<Map.Entry<String,Integer>> countSet = UserImageCountListData.userCount.entrySet();
        for (Map.Entry<String,Integer> map:countSet) {
            map.setValue(0);
        }
        logger.info("总人流量已清空");
        //清除每小时人流量
        Set<Map.Entry<String,int[]>> hourCountSet = UserImageCountListData.hourCount.entrySet();
        for (Map.Entry<String,int[]> map:hourCountSet) {
            for (int i = 0;i<map.getValue().length;i++){
                map.getValue()[i] = 0;
            }
        }
        logger.info("每小时人流量已清空");
    }


    //17:30时 清除签退人员数组
//    @Scheduled(cron = "0 30 17 * * ? ")
    public void clearList(){
        Set<Map.Entry<String, List<Map>>> latestSet = UserImageCountListData.latestList.entrySet();
        for (Map.Entry<String, List<Map>> map :latestSet) {
            map.getValue().clear();
        }
        logger.info("签退人员已清空");
    }

}
