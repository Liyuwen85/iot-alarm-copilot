package com.example.iotalarmcopilot.bridge;

import java.sql.*;

/**
 * 使用 PostgreSQL advisory lock 做单活主节点选举。
 * 锁与连接绑定，连接断开时 PostgreSQL 会自动释放锁。
 */
public final class PostgresLeaderElector implements AutoCloseable {

    private final BridgeConfig config;
    private Connection connection;

    public PostgresLeaderElector(BridgeConfig config) {
        this.config = config;
    }

    /**
     * 尝试获取锁
     *
     * @return
     */
    public boolean tryAcquire() {
        try {
            ensureConnected();
            try (PreparedStatement statement = connection.prepareStatement("SELECT pg_try_advisory_lock(?)")) {
                statement.setLong(1, config.leaderLockKey());
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getBoolean(1);
                    }
                    return false;
                }
            }
        } catch (SQLException exception) {
            closeQuietly();
            throw new IllegalStateException("Failed to acquire PostgreSQL advisory lock", exception);
        }
    }

    /**
     * 检测锁是否健康
     *
     * @return
     */
    public boolean isLockHealthy() {
        if (connection == null) {
            return false;
        }
        try (PreparedStatement statement = connection.prepareStatement("SELECT 1")) {
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) == 1;
            }
        } catch (SQLException exception) {
            closeQuietly();
            return false;
        }
    }

    @Override
    public void close() {
        closeQuietly();
    }

    private void ensureConnected() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            return;
        }
        connection = DriverManager.getConnection(
                config.leaderJdbcUrl(),
                config.leaderJdbcUsername(),
                config.leaderJdbcPassword());
        connection.setAutoCommit(true);
    }

    private void closeQuietly() {
        if (connection == null) {
            return;
        }
        try {
            connection.close();
        } catch (SQLException ignored) {
        } finally {
            connection = null;
        }
    }
}
