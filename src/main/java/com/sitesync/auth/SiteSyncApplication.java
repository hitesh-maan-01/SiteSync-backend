package com.sitesync.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Root application class - must live at com.sitesync (not nested inside
 * com.sitesync.auth) so Spring Boot's component scanning, which defaults
 * to scanning the package this class lives in and everything below it,
 * picks up com.sitesync.auth.* automatically without extra @ComponentScan
 * configuration. Future modules (projects, sites, vendors) should follow
 * the same com.sitesync.<module> pattern alongside auth.
 */
@SpringBootApplication(scanBasePackages = "com.sitesync")
@EnableJpaRepositories(basePackages = "com.sitesync") 
@EntityScan(basePackages = "com.sitesync")
public class SiteSyncApplication {
    public static void main(String[] args) {
        SpringApplication.run(SiteSyncApplication.class, args);
    }
}