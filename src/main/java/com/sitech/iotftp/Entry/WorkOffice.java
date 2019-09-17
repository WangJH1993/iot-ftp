package com.sitech.iotftp.Entry;


import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.sql.Time;

@Entity
@Table(name = "iot_office_workoffice")
@Setter
@Getter
public class WorkOffice implements Serializable {

    @Id
    @GeneratedValue
    private int office_id;  //办公区id
    private String office_name;  //办公区名称
    private int corp_id;  //企业id
    private String local;  //办公区地址（天气地址）
    private String office_local;  //办公区详细地址
    private Time start_time;  //上班时间
    private Time end_time;  //下班时间

}
