package com.muxin.gateway.admin.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.SmartLifecycle;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.File;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@ConditionalOnProperty(name = "spring.profiles.active", havingValue = "sqlite")
public class DatabaseUpgrader implements SmartLifecycle {

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    private final AtomicBoolean running = new AtomicBoolean(false);

    public DatabaseUpgrader(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void start() {
        if (running.compareAndSet(false, true)) {
            upgradeDatabase();
        }
    }

    @Override
    public void stop() {
        running.set(false);
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    @Override
    public int getPhase() {
        return Integer.MIN_VALUE + 1;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    private void upgradeDatabase() {
        upgradeDataScope();
    }

    private void upgradeDataScope() {
        try {
            if (isColumnExists("sys_role", "data_scope")) {
                log.debug("data_scope column already exists");
                return;
            }

            log.info("Upgrading database: adding data_scope support...");
            
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.setContinueOnError(true);
            populator.setIgnoreFailedDrops(true);
            
            ClassPathResource upgradeResource = new ClassPathResource("sql/upgrade_data_scope.sql");
            if (upgradeResource.exists()) {
                populator.addScript(upgradeResource);
                populator.execute(dataSource);
                log.info("Database upgrade completed: data_scope support added");
            }
        } catch (Exception e) {
            log.warn("Database upgrade check failed: {}", e.getMessage());
        }
    }

    private boolean isColumnExists(String tableName, String columnName) {
        try (Connection conn = dataSource.getConnection()) {
            ResultSet rs = conn.getMetaData().getColumns(null, null, tableName, columnName);
            return rs.next();
        } catch (Exception e) {
            log.debug("Column check failed: {}", e.getMessage());
            return false;
        }
    }
}