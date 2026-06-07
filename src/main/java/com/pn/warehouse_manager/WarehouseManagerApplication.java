package com.pn.warehouse_manager;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * mapper接口扫描器，指明mapper接口所在包，然后就会自动为mapper接口创建代理对象并加入到IOC容器中
 */
@MapperScan(basePackages = "com.pn.warehouse_manager.mapper")
@SpringBootApplication
public class WarehouseManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(WarehouseManagerApplication.class, args);
    }

}
