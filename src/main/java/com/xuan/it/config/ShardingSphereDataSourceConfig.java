package com.xuan.it.config;

import org.apache.shardingsphere.driver.api.yaml.YamlShardingSphereDataSourceFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 将 ShardingSphere YAML 规则内联在 application-sharding.yml 中，
 * 避免使用 jdbc:shardingsphere:classpath:xxx.yaml 的跳转配置。
 */
@Configuration
public class ShardingSphereDataSourceConfig {

    @Bean
    @Primary
    public DataSource dataSource(@Value("${app.shardingsphere.yaml}") final String yaml) throws Exception {
        // ShardingSphere 官方工厂方法以 File 作为输入，这里用临时文件承载内联 YAML。
        Path tmp = Files.createTempFile("shardingsphere-", ".yaml");
        Files.write(tmp, yaml.getBytes(StandardCharsets.UTF_8));
        tmp.toFile().deleteOnExit();
        return YamlShardingSphereDataSourceFactory.createDataSource(tmp.toFile());
    }
}

