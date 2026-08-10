package com.relay.common.jpa;

/**
 * Column definitions aligned with Flyway migrations (MySQL 8).
 */
public final class JpaColumnDefinitions {

    public static final String UUID_CHAR_36 = "CHAR(36)";

    public static final String ORG_ROLE = "ENUM('OWNER','ADMIN','MEMBER')";

    public static final String WORKSPACE_ROLE = "ENUM('OWNER','ADMIN','MEMBER')";

    private JpaColumnDefinitions() {
    }
}
