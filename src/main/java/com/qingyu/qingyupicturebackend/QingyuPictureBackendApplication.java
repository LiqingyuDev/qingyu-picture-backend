package com.qingyu.qingyupicturebackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@MapperScan("com.qingyu.qingyupicturebackend.mapper")
@EnableAspectJAutoProxy(exposeProxy = true) // 启用AspectJ自动代理，并暴露代理对象
@EnableAsync // 启用异步任务
public class QingyuPictureBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(QingyuPictureBackendApplication.class, args);
    }

}
