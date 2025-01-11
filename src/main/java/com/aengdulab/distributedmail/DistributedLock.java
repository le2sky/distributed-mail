package com.aengdulab.distributedmail;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.stereotype.Component;

@Component
public class DistributedLock {

    public boolean tryLock(Connection connection, String key, int timeout) {
        String sql = "select get_lock(?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, key);
            preparedStatement.setInt(2, timeout);
            return getResult(preparedStatement);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean getResult(PreparedStatement preparedStatement) throws SQLException {
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            if (!resultSet.next()) {
                return false;
            }

            int result = resultSet.getInt(1);
            return result == 1;
        }
    }

    public void releaseLock(Connection connection, String key) {
        String sql = "select release_lock(?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, key);
            preparedStatement.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
