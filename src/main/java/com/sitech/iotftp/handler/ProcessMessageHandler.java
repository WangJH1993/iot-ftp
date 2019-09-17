package com.sitech.iotftp.handler;

import com.sitech.iotftp.Entry.UserImage;
import com.sitech.iotftp.data.UserImageCountListData;
import com.sitech.iotftp.repositories.DeviceInfoRepository;
import com.sitech.iotftp.repositories.UserImageRepository;
import com.sitech.iotftp.utils.EncryptionUtil;
import com.sitech.iotftp.webSocket.WebSocketServer;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Example;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.util.*;
import java.util.function.Predicate;

@Component
public class ProcessMessageHandler implements ProcessMessage {

    private static Logger logger = Logger.getLogger(ProcessMessageHandler.class);

    @Autowired
    private UserImageRepository userImageRepository;
    @Autowired
    private WebSocketServer webSocketServer;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private DeviceInfoRepository deviceInfoRepository;

    @Value("${YX_GET_SIGN_URL}")
    private String getSignUrl;

    @Value("${YX_SIGN_URL}")
    private String signUrl;

    private final static Object obj1 = new Object();
    private final static Object obj2 = new Object();
    private final static Object obj3 = new Object();
    private final static Object obj4 = new Object();
    private final static Object obj5 = new Object();


    @Override
    @Async("taskExecutor")  //使用单线程
    public void process(JSONObject paramsJson) {

        try {
            long currentTime = System.currentTimeMillis();
            String userId = paramsJson.getJSONObject("result").getJSONArray("user_list").getJSONObject(0).getString("user_id");
            String group_id = paramsJson.getJSONObject("result").getJSONArray("user_list").getJSONObject(0).getString("group_id");
            String similarity = paramsJson.getJSONObject("result").getJSONArray("user_list").getJSONObject(0).get("score") + "";
            String deviceId = paramsJson.getString("deviceId");
            String staff = paramsJson.getJSONObject("officeMap").getString("staff");  //考勤机工号
            similarity = new BigDecimal(similarity).setScale(2,BigDecimal.ROUND_HALF_UP).toString();

            synchronized (obj5){
                //TODO 人流量统计
                Integer countAll = UserImageCountListData.userCount.get(staff)+1;
                UserImageCountListData.userCount.put(staff,countAll );
                //TODO 统计每个时刻的人流量
                Date date = new Date();
                UserImageCountListData.hourCount.get(staff)[date.getHours()/3] = ++UserImageCountListData.hourCount.get(staff)[date.getHours()/3];
            }


            //TODO 1 调用门禁接口
            String doorProductKey = paramsJson.getJSONObject("doorDeviceMap").get("product_key")+"";
            String doorDeviceName = paramsJson.getJSONObject("doorDeviceMap").get("device_name")+"";
            openDoorThroughIot(doorProductKey,doorDeviceName);


            //TODO 2 签到、签退 参数：appid、simId（手机号）、corpID（企业ID）、empId（员工ID）、notesID、signFlag、经度、纬度
            Optional<UserImage> userImage = userImageRepository.findOne(Example.of(UserImage.of(userId, group_id)));
            String empName = userImage.get().getEmpName();
            Map<String, Object> checkMap = new LinkedHashMap<>();
            checkMap.put("empName", empName);
            checkMap.put("baseImage", userImage.get().getImageUrl());
            checkMap.put("time", currentTime);

            //TODO 2.1 查询该员工的签到记录
            synchronized (obj2) {
                UserImageCountListData.checkList.get(staff).add(checkMap);
                Predicate<List> predicate = s -> s.size() > 6;
                while (predicate.test(UserImageCountListData.checkList.get(staff))) {
                    UserImageCountListData.checkList.get(staff).remove(0);
                }
            }
            //将信息添加到  最早签到  数组中
            synchronized (obj3) {
                if (UserImageCountListData.firstList.get(staff).size() < 4) {
                    UserImageCountListData.firstList.get(staff).add(checkMap);
                }
            }
            synchronized (obj4) {
                //12点之前可以签退  签退之前必须查询是否已经签到
                List<Map> list = UserImageCountListData.latestList.get(staff);
                for (int i=0;i<list.size();i++){
                    if (list.get(i).get("empName").equals(empName)){  //如果已经包含该人员 则重新排序
                        list.remove(i);
                    }
                }
                UserImageCountListData.latestList.get(staff).add(checkMap);
                while (UserImageCountListData.latestList.get(staff).size() > 4) {
                    UserImageCountListData.latestList.get(staff).remove(0);
                }
            }

            //TODO 3 将以实别人员 和 以签到人员发送给 考勤机
            Map<String, Object> imageMap = new LinkedHashMap<>();
            imageMap.put("empName", empName);
            imageMap.put("baseImage", userImage.get().getImageUrl());
            imageMap.put("currentImage", paramsJson.getString("currentImage"));
            imageMap.put("similarity", similarity);
            imageMap.put("time", currentTime);
            UserImageCountListData.identificationList.get(staff).add(imageMap);
            synchronized (obj1) {
                if (UserImageCountListData.identificationList.get(staff).size() > 5) {
                    Predicate<List> predicate = s -> s.size() > 5;
                    while (predicate.test(UserImageCountListData.identificationList.get(staff))) {
                        UserImageCountListData.identificationList.get(staff).remove(0);
                    }
                }
            }
            imageMap.put("broadcast", "欢迎"+empName);
            imageMap.put("checkIn", false);  //是否是第一次签到
            List<Map> imageList = new ArrayList<>();
            imageList.add(imageMap);
            JSONObject imageJson = new JSONObject();
            imageJson.put("imageMap",imageList);
            imageJson.put("flag", 8);
            webSocketServer.sendMessageToAllForStaff(staff,imageJson.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @Method:         openDoorThroughIot
     * @Author:         WJH
     * @CreateDate:     2019/7/24 17:22
     * @UpdateUser:     WJH
     * @UpdateDate:     2019/7/24 17:22
     * @UpdateRemark:   调用iot对外开放接口控制门禁开关
     * @Version:        1.0
     */
    public void openDoorThroughIot(String productKey, String deviceName) {
        //拼装参数调用加密算法
        String data = "AccessKey18310724310ActionInvokeThingServiceSignatureMethodHmacSHA1SignatureVersion1.0";
        String key = "9b958a7cd51abc90eda21506ad46f87ecc8e3655";
        String signature = EncryptionUtil.genHMAC(data,key);

        Map map = new HashMap();
        Map args = new HashMap();
        map.put("ProductKey", productKey);
        map.put("DeviceName", deviceName);
        map.put("Identifier", "opendoor");
        args.put("action","open");
        map.put("Args",args);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
        HttpEntity<Map> httpEntity = new HttpEntity<>(map, httpHeaders);
        String resultJson = restTemplate.postForObject("http://172.18.232.163:10002/?Action=InvokeThingService&AccessKey=18310724310&SignatureVersion=1.0&SignatureMethod=HmacSHA1&Signature="+signature, httpEntity, String.class);
        System.out.println("门禁以开：" + resultJson);
    }

    /**
     * @Method: connServer
     * @Author: WJH
     * @CreateDate: 2018/12/14 19:50
     * @UpdateUser: WJH
     * @UpdateDate: 2018/12/14 19:50
     * @UpdateRemark: 修改内容改内容
     * @Version: 1.0
     */
    public static String connServer(String ip, int port, String data) {
        String dataStr = "";
        OutputStream outputStream = null;
        InputStream inputStream = null;
        BufferedReader bufferedReader = null;
        InputStreamReader inputStreamReader = null;
        Socket socket = null;
        try {
            long nowTime = System.currentTimeMillis();
            byte m_end[] = {3};
            //创建Socket对象
            socket = new Socket(ip, port);
            //根据输入输出流和服务端连接
            outputStream = socket.getOutputStream();//获取一个输出流，向服务端发送信息
            outputStream.write(data.getBytes());
            outputStream.write(m_end);
            logger.info("开门命令已发送(" + nowTime + "): " + data);


            byte[] datas = new byte[2048];
            //从服务端程序接收数据
            socket.getInputStream().read(datas);
            logger.info("接收返回消息：" + new String(datas));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeIO(socket, inputStream, outputStream, bufferedReader, inputStreamReader);
        }
        return dataStr;
    }

    /**
     * @Method: closeIO
     * @Author: WJH
     * @CreateDate: 2018/12/14 19:50
     * @UpdateUser: WJH
     * @UpdateDate: 2018/12/14 19:50
     * @UpdateRemark: 修改内容改内容
     * @Version: 1.0
     */
    protected static void closeIO(Socket socket, InputStream input, OutputStream output, BufferedReader bufferedReader, InputStreamReader inputStreamReader) {
        try {
            if (input != null) {
                input.close();
            }
            if (output != null) {
                output.close();
            }
            if (socket != null) {
                socket.close();
            }
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (inputStreamReader != null) {
                inputStreamReader.close();
            }
        } catch (IOException e) {
            logger.error("资源关闭异常：" + e.getMessage());
        }
    }

    /**
     * @Method: sendSms
     * @Author: WJH
     * @CreateDate: 2019/5/29 16:20
     * @UpdateUser: WJH
     * @UpdateDate: 2019/5/29 16:20
     * @UpdateRemark: 发送http协议消息
     * @Version: 1.0
     */
    public String sendSms(String url, String body, String method) {
        HttpURLConnection con = null;
        OutputStream os = null;
        BufferedReader res = null;
        StringBuilder response = new StringBuilder();
        try {
            URL command = new URL(url);
            con = (HttpURLConnection) command.openConnection();
            con.setRequestMethod(method);
            con.setDoOutput(false);
            con.setConnectTimeout(20000);
            con.setReadTimeout(20000);
            con.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            res = new BufferedReader(new InputStreamReader(con.getInputStream()));
            for (String responseLine = res.readLine(); responseLine != null; responseLine =
                    res.readLine()) {
                response.append(responseLine);
            }
            logger.info("数据发送成功,收到响应： " + response.toString());
        } catch (Exception e) {
            logger.error("数据包发送第三方失败：" + e.getMessage());
        } finally {
            try {
                if (con != null) {
                    con.disconnect();
                }
                if (os != null) {
                    os.close();
                }
                if (res != null) {

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return response.toString();
    }
}
