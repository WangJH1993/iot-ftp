package com.sitech.iotftp.controller;


import com.sitech.iotftp.Entry.DeviceInfo;
import com.sitech.iotftp.repositories.DeviceInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/deviceInfo")
public class DeviceInfoController extends BaseController {


    @Autowired
    private DeviceInfoRepository deviceInfoRepository;


    //TODO 1 设备查询

    //TODO 2 设备添加
    @PostMapping
    public String addDevice(@RequestBody DeviceInfo deviceInfo){
//        DeviceInfo resultDevice = deviceInfoRepository.save(deviceInfo);
//        if (resultDevice == null){
//            return responseError(300,"设备添加失败！");
//        }
        return responseMessage(200);
    }


    //TODO 3 设备删除
}
