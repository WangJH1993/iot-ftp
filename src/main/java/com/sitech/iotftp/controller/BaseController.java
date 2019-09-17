package com.sitech.iotftp.controller;


import com.sitech.iotftp.utils.DataUtil;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;

import java.util.LinkedHashMap;
import java.util.Map;

@Controller
public class BaseController {

    private static Logger logger = Logger.getLogger(BaseController.class);

    private Map<String,Object>  messageMap = new LinkedHashMap<>();
    private String errorCode ;
    private String message;


    /**
    * @Method:         responseMessage
    * @Author:         WJH
    * @CreateDate:     2019/3/15 15:33
    * @UpdateUser:     WJH
    * @UpdateDate:     2019/3/15 15:33
    * @UpdateRemark:   格式化操作响应信息
    * @Version:        1.0
    */
    public String responseMessage(int flag){
        message = flag>0?"操作成功！":"操作失败！";
        errorCode = flag>0?"200":"300";
        messageMap.put("errorCode",errorCode);
        messageMap.put("errorMessage",message);
        return DataUtil.mapToJson(messageMap);
    }


    /**
    * @Method:         responseError
    * @Author:         WJH
    * @CreateDate:     2019/3/15 15:34
    * @UpdateUser:     WJH
    * @UpdateDate:     2019/3/15 15:34
    * @UpdateRemark:   格式化错误响应信息
    * @Version:        1.0
    */
    public String responseError(int errorCode, String message){
        messageMap.put("errorCode",errorCode);
        messageMap.put("errorMessage",message);
        return DataUtil.mapToJson(messageMap);
    }



    public String responseSuccess(Object obj){
        messageMap.put("errorCode",200);
        messageMap.put("errorMessage","SUCCESS");
        messageMap.put("result",obj);
        return DataUtil.mapToJson(messageMap);
    }

}
