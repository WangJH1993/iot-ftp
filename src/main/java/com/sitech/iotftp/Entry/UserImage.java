package com.sitech.iotftp.Entry;


import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "iot_user_images")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor(staticName = "of")
public class UserImage implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @NonNull
    private String notesId;  //员工notesId,也是百度云底库id
    private String appid;  //APP_ID
    private String simId;  //手机号
    @NonNull
    private String corpId;  //企业ID
    private String empId;  //员工ID
    private String imageUrl;  //图片地址
    private String empName;  //员工姓名
}
