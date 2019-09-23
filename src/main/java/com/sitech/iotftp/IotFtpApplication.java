package com.sitech.iotftp;

import com.sitech.iotftp.messaging.impl.ZeroMQEventSubscriber;
import com.sitech.iotftp.repositories.DeviceInfoRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ImportResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync(proxyTargetClass = true)
@EnableScheduling
@SpringBootApplication
@EntityScan(basePackages = {"com.sitech.iotftp.Entry"})//最好添加
@EnableJpaRepositories(basePackages = {"com.sitech.iotftp.repositories"})//最好添加
@ImportResource("spring-config.xml")
public class IotFtpApplication {
    public static ConfigurableApplicationContext ctx = null;

    public static void main(String[] args) throws InterruptedException {
        ctx = SpringApplication.run(IotFtpApplication.class, args);
        ZeroMQEventSubscriber sub = ctx.getBean(ZeroMQEventSubscriber.class);
        sub.receive();
        System.out.println("当前分支是：test112==============");
    }

}


