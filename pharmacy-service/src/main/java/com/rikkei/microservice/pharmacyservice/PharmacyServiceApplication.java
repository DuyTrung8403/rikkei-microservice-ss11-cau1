package com.rikkei.microservice.pharmacyservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

@SpringBootApplication
public class PharmacyServiceApplication {

    private static final Logger log = LoggerFactory.getLogger(PharmacyServiceApplication.class);
    @Value("${app.branch-name}")
    private String branchName;
    @Value("${app.hotline}")
    private String hotline;

    public static void main(String[] args) {
        SpringApplication.run(PharmacyServiceApplication.class, args);
    }

    @Bean
    public CommandLineRunner runner(DataSource dataSource) {
        return args -> {
            log.info("=================================================");
            log.info(">> Tên chi nhánh : {}", branchName);
            log.info(">> Hotline       : {}", hotline);
            log.info(">> Kết nối DB    : {}", dataSource.getConnection().getMetaData().getURL());
            log.info("=================================================");
        };
    }
}
