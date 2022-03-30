package com.intive.patronage22.szczecin.retroboard.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.flyway.FlywayDataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.Arrays;

@Configuration
public class DataSourceConfiguration {

    @Bean
    @FlywayDataSource
    public DataSource getDataSource(@Value("${database.urlWithCredentials}") final String databaseUrlEnvVariable) {
        final String[] dataSourceArray = Arrays.stream(
                databaseUrlEnvVariable.split("(postgres)|(@)|(:)|(/{1,2})"))
                    .filter(e -> e.trim().length() > 0)
                    .toArray(String[]::new);
        final DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.url(String.format(
                "jdbc:postgresql://%s:%s/%s", dataSourceArray[2], dataSourceArray[3], dataSourceArray[4]));
        dataSourceBuilder.username(dataSourceArray[0]);
        dataSourceBuilder.password(dataSourceArray[1]);

        return dataSourceBuilder.build();
    }
}
