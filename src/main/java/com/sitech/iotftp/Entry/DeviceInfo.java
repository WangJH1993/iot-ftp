package com.sitech.iotftp.Entry;


import lombok.Getter;
import lombok.Setter;

import org.springframework.data.annotation.Id;
import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "iot_office_deviceinfo")
@Setter
@Getter
public class DeviceInfo implements Serializable {

    @Id
    @javax.persistence.Id
    @GeneratedValue
    private String device_id;  //设备ID
    private String device_name;  //设备名称
    private String product_key;  //productKey
    private String staff;  //所属工号
    private int flag;  //0:IPC设备、1:门禁设备、2:考勤机
    private String belong_office;  //所属办公区

}
