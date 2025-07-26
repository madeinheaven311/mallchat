package com.d4c.www;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

/**
 * @author zhongzb
 * @date 2021/05/27
 */
@SpringBootApplication(scanBasePackages = {"com.d4c.www"})
//@MapperScan({"com.d4c.www.**.mapper"})
@ServletComponentScan
@MapperScan({"com.d4c.www.**.mapper"})
public class MallchatCustomApplication {

    //master
    public static void main(String[] args) {
        SpringApplication.run(MallchatCustomApplication.class,args);
    }

}