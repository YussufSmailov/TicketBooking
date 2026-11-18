package com.ticketbooking.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * Только для профиля prod (хостинг). Локально schema.sql и data.sql выполняются при каждом запуске
 * и пересоздают базу. На хостинге так нельзя — пропадут пользователи и брони.
 * Поэтому здесь таблицы и тестовые данные создаются один раз: если в базе ещё нет таблицы users.
 */
@Component
@Profile("prod")
public class ProdDatabaseInitializer implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(ProdDatabaseInitializer.class);

    private final DataSource dataSource;

    public ProdDatabaseInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void afterPropertiesSet() {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        String usersTable = jdbc.queryForObject("SELECT to_regclass('public.users')::text", String.class);
        if (usersTable != null) {
            log.info("База уже создана — schema.sql и data.sql не выполняются");
            return;
        }

        log.info("Пустая база: создаю таблицы и тестовые данные");
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(
                new ClassPathResource("schema.sql"), new ClassPathResource("data.sql"));
        populator.setSqlScriptEncoding("UTF-8");
        populator.execute(dataSource);
    }
}
