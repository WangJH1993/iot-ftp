package com.sitech.iotftp.webSocket;

import com.sitech.iotftp.IotFtpApplication;
import com.sitech.iotftp.data.UserImageCountListData;
import com.sitech.iotftp.repositories.DeviceInfoRepository;
import org.json.JSONObject;
import org.springframework.beans.BeansException;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@ServerEndpoint(value = "/websocket")
@Component
public class WebSocketServer {

    //concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。
    private static Map<String, CopyOnWriteArraySet<WebSocketServer>> webSocketMap = new ConcurrentHashMap<>();
//    private static CopyOnWriteArraySet<WebSocketServer> webSocketSet = new CopyOnWriteArraySet<WebSocketServer>();

    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;

    /**
    * @Method:         openConnect
    * @Author:         WJH
    * @CreateDate:     2019/7/11 13:12
    * @UpdateUser:     WJH
    * @UpdateDate:     2019/7/11 13:12
    * @UpdateRemark:   连接成功后 将websocket对象存储到set集合中
    * @Version:        1.0
    */
    @OnOpen
    public void openConnect(Session session){
//        this.session = session;
//        webSocketMap.get("a21001").add(this);
    }


    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息*/
    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.println("来自客户端的消息:" + message);

        String staff = message;
        this.session = session;
        if (webSocketMap.containsKey(staff)){
            webSocketMap.get(staff).add(this);
        } else {
            CopyOnWriteArraySet<WebSocketServer> webSocketSet = new CopyOnWriteArraySet<WebSocketServer>();
            webSocketSet.add(this);
            webSocketMap.put(staff,webSocketSet);
        }

        //将所有数据推送给新打开的页面
        //人员总计
        JSONObject jsonObject5 = new JSONObject();
        jsonObject5.put("flag",5);
        jsonObject5.put("totalCount", UserImageCountListData.userCount.get(staff));
        sendMessage(jsonObject5.toString());
        //每小时人员统计
        JSONObject jsonObject6 = new JSONObject();
        jsonObject6.put("flag",6);
        jsonObject6.put("hourCount",UserImageCountListData.hourCount.get(staff));
        sendMessage(jsonObject6.toString());
        //最早签到
        JSONObject jsonObject3 = new JSONObject();
        jsonObject3.put("flag",3);
        jsonObject3.put("firstList", UserImageCountListData.firstList.get(staff));
        sendMessage(jsonObject3.toString());
        //最晚签退
        JSONObject jsonObject2 = new JSONObject();
        jsonObject2.put("flag",2);
        jsonObject2.put("latestList", UserImageCountListData.latestList.get(staff));
        sendMessage(jsonObject2.toString());
        //已识别、已签到
        JSONObject jsonObject1 = new JSONObject();
        jsonObject1.put("flag",1);
        jsonObject1.put("identificationList", UserImageCountListData.identificationList.get(staff));
        jsonObject1.put("checkList", UserImageCountListData.checkList.get(staff));
        jsonObject1.put("broadcast", "欢迎使用ThingHub智慧考勤系统");
        sendMessage(jsonObject1.toString());

        //查询该办公区的下班时间
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("flag",9);
        String endTime = IotFtpApplication.ctx.getBean(DeviceInfoRepository.class).findEndTimeByStaff(staff);
        jsonObject.put("endTime",endTime);
        //查询该工号所属办公区
        Map<String,String> local = IotFtpApplication.ctx.getBean(DeviceInfoRepository.class).findLocalByStaff(staff);
        if (!local.isEmpty()){
            jsonObject.put("lcoal",local.get("office_local"));
        } else {
            jsonObject.put("lcoal","思特奇");
        }
        sendMessage(jsonObject.toString());

    }

    @OnError
    public void onError(Session session, Throwable error) {
        System.out.println("发生错误,客户端链接未正常退出！");
        error.printStackTrace();
    }

    @OnClose
    public void onClose(){
        Set<Map.Entry<String, CopyOnWriteArraySet<WebSocketServer>>> set = webSocketMap.entrySet();
        for (Map.Entry<String, CopyOnWriteArraySet<WebSocketServer>> iss: set) {
                for (WebSocketServer i:iss.getValue()) {
                    try {
                        if (i.session == this.session){
                            iss.getValue().remove(i);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    }
            }
        }
    }

    /**
    * @Method:         sendMessage
    * @Author:         WJH
    * @CreateDate:     2019/7/11 13:21x
    * @UpdateUser:     WJH
    * @UpdateDate:     2019/7/11 13:21
    * @UpdateRemark:   发送消息给单个webSocket客户端
    * @Version:        1.0
    */
    public void sendMessage(String message){
        try {
            this.session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (BeansException e){
            e.printStackTrace();
        }
    }


    /**
    * @Method:         sendMessageToAll
    * @Author:         WJH
    * @CreateDate:     2019/7/11 13:21
    * @UpdateUser:     WJH
    * @UpdateDate:     2019/7/11 13:21
    * @UpdateRemark:   发送消息给某个办公区的所有的webSocket客户端
    * @Version:        1.0
    */
    public void sendMessageToAllForStaff(String staff, String message){

        Set<Map.Entry<String, CopyOnWriteArraySet<WebSocketServer>>> set = webSocketMap.entrySet();
        for (Map.Entry<String, CopyOnWriteArraySet<WebSocketServer>> iss: set) {
            if (iss.getKey().equals(staff)){
                for (WebSocketServer i:iss.getValue()) {
                    try {
                        i.sendMessage(message);
                    } catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    }
                }
            }
        }
    }


    /**
    * @Method:         sendMessageToAll
    * @Author:         WJH
    * @CreateDate:     2019/7/31 17:38
    * @UpdateUser:     WJH
    * @UpdateDate:     2019/7/31 17:38
    * @UpdateRemark:   发送消息给所有的webSocket客户端
    * @Version:        1.0
    */
    public void sendMessageToAll(String message){
        Set<Map.Entry<String, CopyOnWriteArraySet<WebSocketServer>>> set = webSocketMap.entrySet();
        for (Map.Entry<String, CopyOnWriteArraySet<WebSocketServer>> iss: set) {
                for (WebSocketServer i:iss.getValue()) {
                    i.sendMessage(message);
                }
        }

    }
}
