package com.sitech.iotftp.utils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Set;

public class DataUtil {

    /**
     * @Method: jsonToMap
     * @Author: WJH
     * @CreateDate: 2018/11/20 11:46
     * @UpdateUser: WJH
     * @UpdateDate: 2019/03/13 11:16
     * @UpdateRemark: 修改内容改内容
     * @Version: 1.0
     */
    public static void jsonToMap(Map<String, String> result, String jsonStr) {
        JsonObject jsonObject = (JsonObject) new JsonParser().parse(jsonStr);
        //递归实现jsonToMap
        Set<String> keys = jsonObject.keySet();
        for (String key : keys) {
            JsonElement value = jsonObject.get(key);
            if (value.isJsonObject()) {
                jsonToMap(result, value.toString());
            } else {
                result.put(key, value.getAsString());  //默认将所有value全部转为String类型
            }
        }
    }

    /**
     * @Method: MapToJson
     * @Author: WJH
     * @CreateDate: 2018/12/6 14:51
     * @UpdateUser: WJH
     * @UpdateDate: 2019/03/13 11:16
     * @UpdateRemark: 修改内容改内容
     * @Version: 1.0
     */
    public static String mapToJson(Map map) {
        String result = new Gson().toJson(map);
        return result;
    }


    /**
    * @Method:         entryToJson
    * @Author:         WJH
    * @CreateDate:     2019/3/18 21:22
    * @UpdateUser:     WJH
    * @UpdateDate:     2019/3/18 21:22
    * @UpdateRemark:   实体类转json
    * @Version:        1.0
    */
    public static String entryToJson(Object obj) {
        String result = new Gson().toJson(obj);
        return result;
    }


    /**
     * @Method:         jsonToMap
     * @Author:         WJH
     * @CreateDate:     2019/5/29 16:43
     * @UpdateUser:     WJH
     * @UpdateDate:     2019/5/29 16:43
     * @UpdateRemark:   Json转Map
     * @Version:        1.0
     */
    public static  Map jsonToMap(String json){
        Gson gson = new Gson();
        Map resultMap = gson.fromJson(json,Map.class);
        return resultMap;
    }

    /**
    * @Method:         compared
    * @Author:         WJH
    * @CreateDate:     2019/4/20 18:45
    * @UpdateUser:     WJH
    * @UpdateDate:     2019/4/20 18:45
    * @UpdateRemark:   冒泡排序
    * @Version:        1.0
    */
    public Object[] compared(Object[] array){
        String flag="";
        for (int i=0; i<array.length;i++){
            for (int j=i; j<array.length; j++){
                if (Long.valueOf((String) array[i])<Long.valueOf((String) array[j])){
                    flag= (String) array[i];
                    array[i]=array[j];
                    array[j]=array;
                }
            }
        }
        return null;
    }


    public static String request(String path, String method) {
        InputStream is = null;
        InputStreamReader isr = null;
        BufferedReader in = null;
        HttpURLConnection http = null;
        StringBuffer result = new StringBuffer();
        try {
            URL url = new URL(path);
            http = (HttpURLConnection) url.openConnection();
            http.setConnectTimeout(20000);
            http.setReadTimeout(20000);
            http.setRequestMethod("GET");
            http.setRequestProperty("Proxy-Connection", "Keep-Alive");
            http.setRequestProperty("Content-Type", "application/json");
            http.setDoOutput(false);
            http.setDoInput(true);
            http.connect();
//            OutputStreamWriter wr = new OutputStreamWriter(http
//                        .getOutputStream(), "utf-8");
//            wr.write("{}");
//            wr.flush();
//            wr.close();
            is = http.getInputStream();
            isr = new InputStreamReader(is, "utf-8");
            in = new BufferedReader(isr);
            for (String line; (line = in.readLine()) != null;) {
                result.append(line + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.setLength(0);
        } finally {
            close(http, is, isr, in);
        }
        return result.toString().trim();
    }









    private static void close(HttpURLConnection http, InputStream is,
                              InputStreamReader isr, BufferedReader in) {
        if (http != null) {
            http.disconnect();
        }
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (isr != null) {
            try {
                isr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
