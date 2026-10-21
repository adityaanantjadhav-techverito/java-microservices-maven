package com.octiq.test.shared;

import org.flywaydb.core.Flyway;

public final class Migrations {
    private Migrations() { }

    public static void run() {
        String url = System.getenv("DB_URL");
        if (url == null || url.isBlank()) {
            return;
        }

        String user = System.getenv().getOrDefault("DB_USER", "");
        String password = System.getenv().getOrDefault("DB_PASSWORD", "");

        Flyway.configure()
                .dataSource(url, user, password)
                .locations("classpath:db/migration")
                .load()
                .migrate();
    }
}
