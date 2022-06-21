package com.ymbok.pay;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Spring Boot 应用启动类
 * @author ymbok.com(ym6745476)
 * @date 2020-06-30 14:38
 */
@SpringBootApplication
@MapperScan("com.ymbok.pay.dao")
@ComponentScan("com.ymbok.pay")
public class PayApplication {
    public static void main(String[] args) {
        SpringApplication.run(PayApplication.class, args);
    }
}
