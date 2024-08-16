package org.openmrs.module.mohbilling.db;

import org.apache.commons.dbcp2.BasicDataSource;
import org.openmrs.api.context.Context;
import java.util.Properties;

public class ConnectionPoolManager {

    private static ConnectionPoolManager instance = null;

    private static final BasicDataSource dataSource = new BasicDataSource();

    Properties properties = Context.getRuntimeProperties();

    private ConnectionPoolManager() {

        dataSource.setDriverClassName(properties.getProperty("connection.driver_class"));
        dataSource.setUsername(properties.getProperty("mambaetl.analysis.db.username",
                properties.getProperty("connection.username")));
        dataSource.setPassword(properties.getProperty("mambaetl.analysis.db.password",
                properties.getProperty("connection.password")));
        dataSource.setUrl(properties.getProperty("connection.url"));

        dataSource.setInitialSize(4);
        dataSource.setMaxTotal(20);
    }

    public static ConnectionPoolManager getInstance() {
        if (instance == null) {
            instance = new ConnectionPoolManager();
        }
        return instance;
    }

    public BasicDataSource getDefaultDataSource() {
        return dataSource;
    }

    public BasicDataSource getEtlDataSource() {
        dataSource.setDefaultSchema(properties.getProperty("mambaetl.analysis.db.etl_database", "analysis_db"));
        return dataSource;
    }
}