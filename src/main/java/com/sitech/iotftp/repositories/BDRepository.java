package com.sitech.iotftp.repositories;

import com.sitech.iotftp.Entry.DeviceInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BDRepository extends JpaRepository<DeviceInfo, Long> {
}
