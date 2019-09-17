package com.sitech.iotftp.repositories;

import com.sitech.iotftp.Entry.UserImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserImageRepository extends JpaRepository<UserImage, Integer> {

    @Query(value = "SELECT image_url FROM `iot_user_images` AS t1 JOIN (SELECT ROUND(RAND() * ((SELECT MAX(id) FROM `iot_user_images`)-(SELECT MIN(id) FROM `iot_user_images`))+(SELECT MIN(id) FROM `iot_user_images`)) AS id) AS t2 WHERE t1.id >= t2.id and t1.id <> ?1 ORDER BY t1.id LIMIT 8;",nativeQuery = true)
    List<String> findEightUsers(Integer id);
}
