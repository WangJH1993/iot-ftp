package com.sitech.iotftp.controller;


import com.sitech.iotftp.Entry.WorkOffice;
import com.sitech.iotftp.repositories.WorkOfficeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/WorkOffice")
public class WorkOfficeController extends BaseController{

    @Autowired
    private WorkOfficeRepository workOfficeRepository;


    //TODO 1 办公区信息查询

    //TODO 2 办公区信息添加
    @PostMapping
    public String addOffice(@RequestBody WorkOffice workOffice){
        WorkOffice resultMSG = workOfficeRepository.save(workOffice);
        if (resultMSG == null){
            return responseError(300,"添加失败！");
        }
        return responseMessage(200);
    }

    //TODO 3 办公区信息删除


}
