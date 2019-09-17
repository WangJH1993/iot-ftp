package com.sitech.iotftp.repositories;

import com.sitech.iotftp.Entry.DeviceInfo;
import org.springframework.context.annotation.Scope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Time;
import java.util.Map;


public interface DeviceInfoRepository extends JpaRepository<DeviceInfo, String> {
    //根据设备id查询企业id和办公区的上下班时间以及办公区坐标（后期查询天气使用）
    @Query(value = "select iod.staff,iow.corp_id,iow.start_time,iow.end_time,iow.local from iot_office_deviceinfo iod " +
            "left join iot_office_workoffice iow on iod.belong_office = iow.office_id " +
            "where device_id =:deviceId",nativeQuery = true)
    Map<String,String> findCropIDById(@Param("deviceId") String deviceId);


    @Query(value = "select device_name,product_key from iot_office_deviceinfo where flag = 1 and staff = " +
            "(select staff from iot_office_deviceinfo where device_id = :deviceId)",nativeQuery = true)
    Map findProductKeyAndDeviceNameById(@Param("deviceId") String deviceId);

    @Query(value = "select DISTINCT iow.office_local from iot_office_deviceinfo iod " +
            "left join iot_office_workoffice iow on iod.belong_office = iow.office_id " +
            "where staff = :staff",nativeQuery = true)
    Map<String, String> findLocalByStaff(@Param("staff") String staff);

    @Query(value = "select DISTINCT iow.end_time from iot_office_deviceinfo iod " +
            "left join iot_office_workoffice iow on iod.belong_office = iow.office_id " +
            "where staff = :staff",nativeQuery = true)
    String findEndTimeByStaff(@Param("staff") String staff);
}
