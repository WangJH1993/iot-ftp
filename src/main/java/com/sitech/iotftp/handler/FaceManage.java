package com.sitech.iotftp.handler;

import com.baidu.aip.face.AipFace;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.util.HashMap;


@Component
public class FaceManage {

    //设置APPID/AK/SK
    public static final String APP_ID = "16751581";
    public static final String API_KEY = "F4GOyU8RvAmOSG0EgxcTCIK2";
    public static final String SECRET_KEY = "GOQaC8myPTGG0cpMa9yL65MvWUtkEmcf";

//    public static void main(String[] args){
//        AipFace client = new AipFace(APP_ID, API_KEY, SECRET_KEY);
//        faceRegistry(client);  //注册人员
//        faceUpdate(client);  //更新人员
//        faceDelete(client);  //删除人员  删除人员时 需要face_token参数
//        findUserInfo(client);  //查询人员信息
//
//    }

    /**
    * @Method:         faceRegistry
    * @Author:         WJH
    * @CreateDate:     2019/7/10 14:04
    * @UpdateUser:     WJH
    * @UpdateDate:     2019/7/10 14:04
    * @UpdateRemark:   人员注册接口调用
    * @Version:        1.0
    */
    public static JSONObject faceRegistry(AipFace client, String image, String groupId, String userId) {
        // 传入可选参数调用接口
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("user_info", "2654");  //用户资料 长度限制256b
        options.put("quality_control", "NORMAL");  //图片质量控制 NONE: 不进行控制 LOW:较低的质量要求 NORMAL: 一般的质量要求 HIGH: 较高的质量要求 默认 NONE
        options.put("liveness_control", "LOW");  //活体检测控制 NONE: 不进行控制 LOW:较低的活体要求(高通过率 低攻击拒绝率) NORMAL: 一般的活体要求(平衡的攻击拒绝率, 通过率) HIGH: 较高的活体要求(高攻击拒绝率 低通过率) 默认NONE
        options.put("action_type", "REPLACE");  //活体检测控制 NONE: 不进行控制 LOW:较低的活体要求(高通过率 低攻击拒绝率) NORMAL: 一般的活体要求(平衡的攻击拒绝率, 通过率) HIGH: 较高的活体要求(高攻击拒绝率 低通过率) 默认NONE

        String imageType = "BASE64";

        // 人脸注册
        JSONObject res = client.addUser(image, imageType, groupId, userId, options);
//        System.out.println(res.toString(2));
        return res;
    }



    /**
    * @Method:         faceUpdate
    * @Author:         WJH
    * @CreateDate:     2019/7/10 14:14
    * @UpdateUser:     WJH
    * @UpdateDate:     2019/7/10 14:14
    * @UpdateRemark:   更新底库信息
    * @Version:        1.0
    */
    public static String faceUpdate(AipFace client) {
        // 传入可选参数调用接口
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("user_info", "user's info");
        options.put("quality_control", "NORMAL");
        options.put("liveness_control", "LOW");
        options.put("action_type", "REPLACE");

        String image = "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";
        String imageType = "BASE64";
        String groupId = "sitech";
        String userId = "yangdh_bill";

        // 人脸更新
        JSONObject res = client.updateUser(image, imageType, groupId, userId, options);
        System.out.println(res.toString(2));
        return res.toString();
    }



    /**
    * @Method:         faceDelete
    * @Author:         WJH
    * @CreateDate:     2019/7/10 14:21
    * @UpdateUser:     WJH
    * @UpdateDate:     2019/7/10 14:21
    * @UpdateRemark:   删除底库人员信息
    * @Version:        1.0
    */
    public static String faceDelete(AipFace client) {
        // 传入可选参数调用接口
        HashMap<String, String> options = new HashMap<String, String>();

        String userId = "yangdh_bill";
        String groupId = "sitech";
        String faceToken = "64f0e25442f8e6cd17c2b90e317007ff";

        // 人脸删除
        JSONObject res = client.faceDelete(userId, groupId, faceToken, options);
        System.out.println(res.toString(2));
        return res.toString();
    }


    /**
    * @Method:         findUserInfo
    * @Author:         WJH
    * @CreateDate:     2019/7/10 14:25
    * @UpdateUser:     WJH
    * @UpdateDate:     2019/7/10 14:25
    * @UpdateRemark:   查找用户信息
    * @Version:        1.0
    */
    public static String findUserInfo(AipFace client) {
        // 传入可选参数调用接口
        HashMap<String, String> options = new HashMap<String, String>();

        String userId = "yanghd_bill";
        String groupId = "sitech";

        // 用户信息查询
        JSONObject res = client.getUser(userId, groupId, options);
        System.out.println(res.toString(2));
        return res.toString();
    }

}
