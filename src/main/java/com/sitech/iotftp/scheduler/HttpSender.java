package com.sitech.iotftp.scheduler;

import com.google.gson.JsonObject;
import com.sitech.iotftp.messaging.EventPublisher;
import com.sitech.iotftp.utils.ImageBase64Handler;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class HttpSender {

    private final static Logger logger = Logger.getLogger(HttpSender.class);

    @Autowired
    private EventPublisher publisher;

    @Value("${url}")
    private String url;

    //不能使用异步 可能当前文件没处理完  下一次扫描 又 扫面到
//    @Async("taskExecutor")
    public void process(Map<String, List<File>> params) {
        JsonObject jsonObject = null;
        try {
            //TODO 1 遍历map拼装json报文
            Set<Map.Entry<String, List<File>>> resultSet = params.entrySet();
            for (Map.Entry<String, List<File>> entry : resultSet) {
                List<File> images = entry.getValue();
                for (File file: images) {
                    if (!file.isHidden() && file.length()<100000){  //判断不是隐藏文件
                        jsonObject = new JsonObject();
                        jsonObject.addProperty("deviceId", entry.getKey());
                        InputStream input = new FileInputStream(file);
                        String imageFile = ImageBase64Handler.imageToBase64Str(input);
                        jsonObject.addProperty("image", imageFile);
                        jsonObject.addProperty("time",System.currentTimeMillis());
                        //TODO 2 将数据发送到ZeroMQ的队列中
                        publisher.sendEventMessage(jsonObject.toString());
                        file.delete();
                    } else if (!file.isHidden() && file.length()>=100000) {  //表示是 大华抓拍人脸的的背景图片
                        file.delete();
                    }
                }
            }
        } catch (Exception e) {
            logger.error("文件获取失败："+e.getMessage());
        } finally {
        }
    }

    public static String sendSms(String url, String body, String method) {
        HttpURLConnection con = null;
        OutputStream os = null;
        BufferedReader res = null;
        StringBuilder response = new StringBuilder();
        try {
            URL command = new URL(url);
            con = (HttpURLConnection) command.openConnection();
            if (method.equals("POST") || method.equals("PUT")) {
                con.setRequestMethod(method);
                con.setDoOutput(true);
                con.setConnectTimeout(10000);
                con.setReadTimeout(10000);
                con.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                con.setRequestProperty("Content-Length", Integer.toString(body.length()));
                os = con.getOutputStream();
                os.write(body.getBytes());
            }
            res = new BufferedReader(new InputStreamReader(con.getInputStream()));
            for (String responseLine = res.readLine(); responseLine != null; responseLine =
                    res.readLine()) {
                response.append(responseLine);
            }
            logger.info("数据发送成功,收到响应： "+ response.toString());
        } catch (Exception e) {
            logger.error(body);
            logger.error("数据包发送第三方失败："+ e.getMessage());
        }finally {
            try {
                if (con != null) {
                    con.disconnect();
                }
                if (os!=null){
                    os.close();
                }
                if (res != null){

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return response.toString();
    }
}
