package com.campus.trade;

import com.campus.trade.config.MyLocaleResolver;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.LocaleResolver;

@SpringBootApplication
@MapperScan("com.campus.trade.mapper")
@EnableAsync      // 异步任务支持
@EnableScheduling // 定时任务支持
public class CampusTradeApplication {

    @Bean
    public LocaleResolver localeResolver() {
        return new MyLocaleResolver();
    }

    public static void main(String[] args) {
        SpringApplication.run(CampusTradeApplication.class, args);
    }
}