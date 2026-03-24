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
public class SqliteInitializer implements SmartLifecycle {

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    private final AtomicBoolean running = new AtomicBoolean(false);

    public SqliteInitializer(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void start() {
        if (running.compareAndSet(false, true)) {
            initializeDatabaseIfNeeded();
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
        return Integer.MIN_VALUE;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    private void initializeDatabaseIfNeeded() {
        String dbPath = extractDbPath();
        if (dbPath == null) {
            log.warn("Cannot extract database path from URL: {}", datasourceUrl);
            return;
        }

        File dbFile = Paths.get(dbPath).toFile();

        if (dbFile.exists() && isDatabaseInitialized()) {
            log.info("SQLite database already initialized: {}", dbFile.getAbsolutePath());
            return;
        }

        if (!dbFile.exists()) {
            log.info("SQLite database file not found, creating: {}", dbFile.getAbsolutePath());
            try {
                File parentDir = dbFile.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    boolean created = parentDir.mkdirs();
                    if (created) {
                        log.info("Created data directory: {}", parentDir.getAbsolutePath());
                    }
                }
            } catch (Exception e) {
                log.error("Failed to create data directory", e);
            }
        } else {
            log.info("SQLite database file exists but tables not found, initializing: {}", dbFile.getAbsolutePath());
        }

        try {
            initializeDatabase();
            log.info("SQLite database initialized successfully at: {}", dbFile.getAbsolutePath());
        } catch (Exception e) {
            log.error("Failed to initialize SQLite database", e);
            throw new RuntimeException("Failed to initialize SQLite database", e);
        }
    }

    private boolean isDatabaseInitialized() {
        try (Connection conn = dataSource.getConnection()) {
            ResultSet rs = conn.getMetaData().getTables(null, null, "sys_user", null);
            return rs.next();
        } catch (Exception e) {
            log.debug("Database check failed: {}", e.getMessage());
            return false;
        }
    }

    private String extractDbPath() {
        if (datasourceUrl == null) {
            return null;
        }
        
        String url = datasourceUrl;
        
        if (url.contains("${user.dir}")) {
            String userDir = System.getProperty("user.dir");
            url = url.replace("${user.dir}", userDir);
        }
        
        if (url.startsWith("jdbc:sqlite:")) {
            return url.substring("jdbc:sqlite:".length());
        }
        
        return null;
    }

    private void initializeDatabase() {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.setContinueOnError(true);
        populator.setIgnoreFailedDrops(true);
        
        ClassPathResource schemaResource = new ClassPathResource("sql/sqlite_schema.sql");
        ClassPathResource dataResource = new ClassPathResource("sql/sqlite_data.sql");
        
        if (schemaResource.exists()) {
            populator.addScript(schemaResource);
            log.info("Added schema script: sql/sqlite_schema.sql");
        } else {
            log.warn("Schema script not found: sql/sqlite_schema.sql");
        }
        
        if (dataResource.exists()) {
            populator.addScript(dataResource);
            log.info("Added data script: sql/sqlite_data.sql");
        } else {
            log.warn("Data script not found: sql/sqlite_data.sql");
        }
        
        populator.execute(dataSource);
        
        log.info("Database initialization scripts executed");
    }
}