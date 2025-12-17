package org.openmrs.module.mohbilling.db;

import org.apache.commons.dbcp2.BasicDataSource;
import org.openmrs.api.context.Context;

import java.util.Properties;

public class ConnectionPoolManager {

    private static final BasicDataSource dataSource = new BasicDataSource();
    private static ConnectionPoolManager instance = null;
    private final Properties properties;

    // Reusable fields
    private final String username;
    private final String password;

    private ConnectionPoolManager() {
        this.properties = Context.getRuntimeProperties();
        this.username = getProperty("mambaetl.analysis.db.username", getProperty("connection.username"));
        this.password = getProperty("mambaetl.analysis.db.password", getProperty("connection.password"));
        configureDataSource();
    }

    public static synchronized ConnectionPoolManager getInstance() {
        if (instance == null) {
            instance = new ConnectionPoolManager();
        }
        return instance;
    }

    private void configureDataSource() {
        dataSource.setUrl(getProperty("connection.url"));
        dataSource.setDriverClassName(getProperty("connection.driver_class"));
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setInitialSize(4);
        dataSource.setMaxTotal(20);
    }

    private String getProperty(String key) {
        return properties.getProperty(key);
    }

    private String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public BasicDataSource getDefaultDataSource() {
        return dataSource;
    }

    public BasicDataSource getEtlDataSource() {
        String etlDatabase = getProperty("mambaetl.analysis.db.etl_database", "openmrs");
        String modifiedUrl = getModifiedUrl(dataSource.getUrl(), etlDatabase);

        BasicDataSource etlDataSource = new BasicDataSource();
        etlDataSource.setDefaultSchema(etlDatabase);
        etlDataSource.setUrl(modifiedUrl);
        etlDataSource.setDriverClassName(dataSource.getDriverClassName());
        etlDataSource.setUsername(username);
        etlDataSource.setPassword(password);
        etlDataSource.setInitialSize(dataSource.getInitialSize());
        etlDataSource.setMaxTotal(dataSource.getMaxTotal());

        return etlDataSource;
    }

    private String getModifiedUrl(String originalUrl, String newDatabase) {
        return originalUrl.replaceAll("(/)[^/?]+(?=\\?|$)", "$1" + newDatabase);
    }
}