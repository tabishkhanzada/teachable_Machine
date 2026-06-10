package com.banking.database;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DatabaseConnectionManager {

    private static DatabaseConnectionManager instance;
    private final DataSource dataSource;

    @Autowired
    public DatabaseConnectionManager(DataSource dataSource) {
        this.dataSource = dataSource;
        instance = this;
    }

    public static DatabaseConnectionManager getInstance() {
        return instance;
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}
