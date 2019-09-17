package com.sitech.iotftp.controller;

import com.baidu.aip.face.AipFace;
import com.sitech.iotftp.Entry.UserImage;
import com.sitech.iotftp.handler.FaceManage;
import com.sitech.iotftp.handler.ProcessMessage;
import com.sitech.iotftp.repositories.UserImageRepository;
import com.sitech.iotftp.utils.FTPUtil;
import com.sitech.iotftp.utils.ImageUtil;
import org.apache.commons.net.ftp.FTPClient;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;


@SuppressWarnings("all")
@RestController
@RequestMapping("api/v1/faceImage")
public class FaceImageController {


    @Autowired
    private ProcessMessage processMessage;
    @Autowired
    private FaceManage faceManage;
    @Autowired
    private UserImageRepository userImageRepository;

    @Value("${FTP_HOST}")
    private String ftpHost;
    @Value("${FTP_PORT}")
    private int ftpPort;
    @Value("${FTP_USERNAME}")
    private String ftpUserName;
    @Value("${FTP_PASSWORD}")
    private String ftpPassword;

    @Value("${BASE_URL}")
    private String baseUrl;

    @Value("${BD_APP_ID}")
    private String APP_ID;
    @Value("${BD_API_KEY}")
    private String API_KEY;
    @Value("${BD_SECRET_KEY}")
    private String SECRET_KEY;

    private static AipFace client = new AipFace("16751581", "F4GOyU8RvAmOSG0EgxcTCIK2", "GOQaC8myPTGG0cpMa9yL65MvWUtkEmcf");

    /**
     * @Method:         userRegistry
     * @Author:         WJH
     * @CreateDate:     2019/7/10 19:30
     * @UpdateUser:     WJH
     * @UpdateDate:     2019/7/10 19:30
     * @UpdateRemark:   人员注册
     * @Version:        1.0
     */
    //TODO 1 百度人脸识别底库人员注册，备份图片到ftp服务器中，将url存放到数据库中
    //appid、simId（手机号）、corpID（企业ID）、empId（员工ID）、notesID、signFlag、经度、纬度
    @PostMapping("/registry")
    public String  userRegistry(@RequestParam MultipartFile multipartFile,@RequestParam String appid,
                                @RequestParam String notesId,@RequestParam String simId,@RequestParam String corpId,
                                @RequestParam String longitude,@RequestParam String latitude,@RequestParam String empId,
                                @RequestParam String empName){
        ByteArrayInputStream bis = null;
        FTPClient ftpClient = null;
        try {
            //TODO 1 进行百度接口注册
            byte[] imageByte = multipartFile.getBytes();
            JSONObject contrastResult = faceManage.faceRegistry(client, Base64.getEncoder().encodeToString(imageByte),corpId,notesId);
            if ((int)contrastResult.get("error_code")==0){  //注册成功
                String path = corpId+"/"+multipartFile.getOriginalFilename();
                //TODO 2 将文件存储到ftp中，生成http地址
                ftpClient = FTPUtil.getFTPClient(ftpHost,ftpPort,ftpPassword,ftpUserName);
                // 设置PassiveMode传输
                ftpClient.enterLocalPassiveMode();
                // 设置以二进制流的方式传输
                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);

                ftpClient.changeWorkingDirectory("faceImages");
                bis = new ByteArrayInputStream(imageByte);
                ftpClient.mkd(corpId);  //以企业id为准创建目录
                ftpClient.storeFile(path,bis);

                //TODO 3 将图片的网络地址存储到mysql中
                String imageUrl = baseUrl+path;
                UserImage userImage = new UserImage(null,notesId,appid,simId,corpId,empId,imageUrl,empName);
                userImageRepository.save(userImage);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bis != null){
                    bis.close();
                }
                if (ftpClient != null){
                    ftpClient.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }


    //TODO 2 百度人脸识别底库人员信息更新

    //TODO 3 百度人脸识别底库人员信息删除

    //TODO 4 百度人脸识别地库人员信息单个搜索

    //TODO 5 人脸识别接口调用
    @PostMapping("/faceContrast")
    public String faceContrast(@RequestBody Map<String,Object> params){
        String engineFactory = params.get("engineFactory")+"";
        if (engineFactory.equals("baidu")){
            //TODO 5.1 调用人脸识别接口
            // 初始化一个AipFace
            AipFace client = new AipFace(APP_ID, API_KEY, SECRET_KEY);
            // 可选：设置网络连接参数
            client.setConnectionTimeoutInMillis(2000);
            client.setSocketTimeoutInMillis(60000);

            // 传入可选参数调用接口
            HashMap<String, String> options = new HashMap<String, String>();
            options.put("max_face_num", "1");  //最大检测照片中几张人脸   最大可以设置为10
            options.put("match_threshold", "80");  //匹配阈值（设置阈值后，score低于此阈值的用户信息将不会返回） 最大100 最小0 默认80 此阈值设置得越高，检索速度将会越快，推荐使用默认阈值80
            options.put("quality_control", "NORMAL");  //图片质量控制 NONE: 不进行控制 LOW:较低的质量要求 NORMAL: 一般的质量要求 HIGH: 较高的质量要求 默认 NONE
            options.put("liveness_control", "LOW");  //活体检测控制 NONE: 不进行控制 LOW:较低的活体要求(高通过率 低攻击拒绝率) NORMAL: 一般的活体要求(平衡的攻击拒绝率, 通过率) HIGH: 较高的活体要求(高攻击拒绝率 低通过率) 默认NONE
            options.put("max_user_num", "1");  //查找后返回的用户数量。返回相似度最高的几个用户，默认为1，最多返回50个。

            String groupIdList = "sitech";  //groupId可以当作企业标志，后续需要完善流程

            // 调用接口
            String image = params.get("image")+"";
            String imageType = "BASE64";

            // 人脸搜索
            JSONObject res = client.search(image, imageType, groupIdList, options);
            System.out.println(res.toString(2));
            res.put("currentImage",image);  //抓拍照片
            //TODO 5.2  //如果返回值为0证明有匹配结果，并且相似度大于80
            if ((int)res.get("error_code") == 0){
                processMessage.process(res);
            }
        }
        return "";
    }

}
