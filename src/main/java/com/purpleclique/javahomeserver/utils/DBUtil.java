package com.purpleclique.javahomeserver.utils;

import com.purpleclique.javahomeserver.models.auth.User;
import com.purpleclique.javahomeserver.models.devices.Device;
import com.purpleclique.javahomeserver.models.auth.Token;
import com.purpleclique.javahomeserver.threads.LoggingThread;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class DBUtil {

    private static final String DEVICE_DATABASE_FILENAME = "jdbc:sqlite:ServerDatabase.sqlite";

    private static DBUtil instance;

    public static DBUtil getInstance() {
        if (instance == null) {
            instance = new DBUtil();
        }
        return instance;
    }

    private DBUtil() {
        databaseInit();
    }

    private void databaseInit() {
        try (Connection connection = DriverManager.getConnection(DEVICE_DATABASE_FILENAME);
             Statement statement = connection.createStatement()) {
            String sql = """
                        create table if not exists main.devices
                        (
                            id        integer               not null
                                primary key autoincrement
                                unique,
                            type      text                  not null,
                            name      text,
                            location  text,
                            ipAddress text                  not null
                                unique,
                            isOn      boolean default false not null,
                            data      text
                        );
                        """;
            statement.execute(sql);
            sql = """
                        create table if not exists main.tokens
                        (
                            id        integer               not null
                                primary key autoincrement
                                unique,
                            token         text                  not null,
                            revoked       boolean,
                            expired       boolean,
                            tokenHolderId integer                  not null,
                            tokenType     text                  not null
                        );
                        """;
            statement.execute(sql);
            sql = """
                        create table if not exists main.users
                        (
                            id        integer               not null
                                primary key autoincrement
                                unique,
                            type     text                  not null,
                            username text                  not null
                                unique,
                            password text                  not null
                        );
                        """;
            statement.execute(sql);
            sql = """
                        create table if not exists main.home
                        (
                            homeName text not null unique
                        );
                        """;
            statement.execute(sql);
        } catch (SQLException e) {
            LoggingThread.logError("Cannot initialize database: " + e.getMessage());
        }
    }

    public Optional<User> findUserByUsername(String username) {
        String sql = """
                SELECT id, type, password FROM users WHERE username = ?;
                """;
        try (Connection connection = DriverManager.getConnection(DEVICE_DATABASE_FILENAME);
             PreparedStatement queryStatement = connection.prepareStatement(sql)) {
            queryStatement.setString(1, username);
            ResultSet resultSet = queryStatement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(User.builder()
                        .username(username)
                        .id(resultSet.getInt("id"))
                        .userType(User.UserType.valueOf(resultSet.getString("type")))
                        .password(resultSet.getString("password"))
                        .build());
            }
        } catch (SQLException e) {
            LoggingThread.logError("Cannot find User in the database: " + e.getMessage());
        }
        return Optional.empty();
    }

    public Set<User> getAllUsers() {
        String sql = """
                SELECT id, type, password, username FROM users;
                """;
        Set<User> users = new HashSet<>();
        try (Connection connection = DriverManager.getConnection(DEVICE_DATABASE_FILENAME);
             PreparedStatement queryStatement = connection.prepareStatement(sql)) {
            ResultSet resultSet = queryStatement.executeQuery();
            while (resultSet.next()) {
                users.add(User.builder()
                                .id(resultSet.getInt("id"))
                                .username(resultSet.getString("username"))
                                .userType(User.UserType.valueOf(resultSet.getString("type")))
                                .password(resultSet.getString("password"))
                        .build());
            }
        } catch (SQLException e) {
            LoggingThread.logError("Cannot get all Users from the database: " + e.getMessage());
        }
        return users;
    }

    public Optional<User> findUserById(int id) {
        String sql = """
                SELECT username, type, password FROM users WHERE id = ?;
                """;
        try (Connection connection = DriverManager.getConnection(DEVICE_DATABASE_FILENAME);
             PreparedStatement queryStatement = connection.prepareStatement(sql)) {
            queryStatement.setInt(1, id);
            ResultSet resultSet = queryStatement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(User.builder()
                                .username(resultSet.getString("username"))
                                .id(id)
                                .password(resultSet.getString("password"))
                                .userType(User.UserType.valueOf(resultSet.getString("type")))
                        .build());
            }
        } catch (SQLException e) {
            LoggingThread.logError("Cannot find User in the database: " + e.getMessage());
        }
        return Optional.empty();
    }

    public void updateUser(@NotNull User user) {
        String sql = """
                UPDATE main.users SET type=?,
                username=?,
                password=?
                WHERE id=?;
                """;
        try (Connection connection = DriverManager.getConnection(DEVICE_DATABASE_FILENAME);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getUserType().name());
            statement.setString(2, user.getUsername());
            statement.setString(3, user.getPassword());
            statement.setInt(4, user.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            LoggingThread.logError("Cannot update User in the database: " + e.getMessage());
        }
    }

    public void saveNewUser(@NotNull User user) {
        String sql = """
                INSERT INTO main.users
                (
                    type, username, password
                )
                VALUES(?,?,?);
                """;
        String sql2 = """
                SELECT id FROM users WHERE username = ?;
                """;
        try (Connection connection = DriverManager.getConnection(DEVICE_DATABASE_FILENAME);
             PreparedStatement selectStatement = connection.prepareStatement(sql);
             PreparedStatement queryStatement = connection.prepareStatement(sql2)) {
            selectStatement.setString(1, user.getUserType().name());
            selectStatement.setString(2, user.getUsername());
            selectStatement.setString(3, user.getPassword());
            selectStatement.executeUpdate();
            queryStatement.setString(1, user.getUsername());
            ResultSet resultSet = queryStatement.executeQuery();
            user.setId(resultSet.getInt("id"));
        } catch (SQLException e) {
            LoggingThread.logError("Cannot save User in the database: " + e.getMessage());
        }
    }

    public void saveHome(String homeName) {
        String sql = """
                INSERT INTO main.home
                (
                    homeName
                )
                VALUES(?);
                """;
        try (Connection connection = DriverManager.getConnection(DEVICE_DATABASE_FILENAME);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, homeName);
            statement.executeUpdate();
        } catch (SQLException e) {
            LoggingThread.logError("Cannot save Home in the database: " + e.getMessage());
        }
    }

    public Optional<String> getHomeName() {
        String sql = """
                SELECT homeName FROM home;
                """;
        try (Connection connection = DriverManager.getConnection(DEVICE_DATABASE_FILENAME);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(resultSet.getString("homeName"));
            }
        } catch (SQLException e) {
            LoggingThread.logError("Cannot save Home in the database: " + e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<Token> findTokenById(int id) {
        String sql = """
                SELECT token, revoked, expired, tokenHolderId, tokenType FROM tokens WHERE id = ?;
                """;
        try (Connection connection = DriverManager.getConnection(DEVICE_DATABASE_FILENAME);
             PreparedStatement queryStatement = connection.prepareStatement(sql)) {
            queryStatement.setInt(1, id);
            ResultSet resultSet = queryStatement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(Token.builder()
                        .id(id)
                        .token(resultSet.getString("token"))
                        .expired(resultSet.getBoolean("expired"))
                        .revoked(resultSet.getBoolean("revoked"))
                        .tokenHolderId(resultSet.getInt("tokenHolderId"))
                        .tokenType(resultSet.getString("tokenType"))
                        .build());
            }
        } catch (SQLException e) {
            LoggingThread.logError("Cannot find Token in the database: " + e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<Token> findToken(String token) {
        String sql = """
                SELECT id, revoked, expired, tokenHolderId, tokenType FROM tokens WHERE token = ?;
                """;
        try (Connection connection = DriverManager.getConnection(DEVICE_DATABASE_FILENAME);
        PreparedStatement queryStatement = connection.prepareStatement(sql)) {
            queryStatement.setString(1, token);
            ResultSet resultSet = queryStatement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(Token.builder()
                        .id(resultSet.getInt("id"))
                        .token(token)
                        .expired(resultSet.getBoolean("expired"))
                        .revoked(resultSet.getBoolean("revoked"))
                        .tokenHolderId(resultSet.getInt("tokenHolderId"))
                        .tokenType(resultSet.getString("tokenType"))
                        .build());
            }
        } catch (SQLException e) {
            LoggingThread.logError("Cannot find Token in the database: " + e.getMessage());
        }
        return Optional.empty();
    }

    public void updateToken(@NotNull Token token) {
        String sql = """
                UPDATE main.tokens SET token=?,
                revoked=?,
                expired=?,
                tokenHolderId=?,
                tokenType=?
                WHERE id=?;
                """;
        try (Connection connection = DriverManager.getConnection(DEVICE_DATABASE_FILENAME);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, token.getToken());
            statement.setBoolean(2, token.isRevoked());
            statement.setBoolean(3, token.isExpired());
            statement.setInt(4, token.getTokenHolderId());
            statement.setString(5, token.getTokenType());
            statement.setInt(6, token.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            LoggingThread.logError("Cannot update Token in the database: " + e.getMessage());
        }
    }

    public void saveNewToken(@NotNull Token token) {
        String sql = """
                INSERT INTO main.tokens
                (
                    token,
                    revoked,
                    expired,
                    tokenHolderId,
                    tokenType
                )
                VALUES(?,?,?,?,?);
                """;
        String sql2 = """
                SELECT id FROM tokens WHERE token = ?;
                """;
        try (Connection connection = DriverManager.getConnection(DEVICE_DATABASE_FILENAME);
             PreparedStatement selectStatement = connection.prepareStatement(sql);
             PreparedStatement queryStatement = connection.prepareStatement(sql2)) {
            selectStatement.setString(1, token.getToken());
            selectStatement.setBoolean(2, token.isRevoked());
            selectStatement.setBoolean(3, token.isExpired());
            selectStatement.setInt(4, token.getTokenHolderId());
            selectStatement.setString(5, token.getTokenType());
            selectStatement.executeUpdate();
            queryStatement.setString(1, token.getToken());
            ResultSet resultSet = queryStatement.executeQuery();
            token.setId(resultSet.getInt("id"));
        } catch (SQLException e) {
            LoggingThread.logError("Cannot save Token in the database: " + e.getMessage());
        }
    }

    public void deleteAllValidTokensByPersonId(int userId) {
        String sql = """
                DELETE FROM tokens
                WHERE tokenHolderId = ?
                """;
        try (Connection connection = DriverManager.getConnection(DEVICE_DATABASE_FILENAME);
             PreparedStatement queryStatement = connection.prepareStatement(sql)) {
            queryStatement.setInt(1, userId);
            queryStatement.execute();
        } catch (SQLException e) {
            LoggingThread.logError("Cannot delete Tokens in the database: " + e.getMessage());
        }
    }

    public void deleteDeviceById(int id) {
        String sql = """
                DELETE FROM devices
                WHERE id = ?
                """;
        try (Connection connection = DriverManager.getConnection(DEVICE_DATABASE_FILENAME);
        PreparedStatement queryStatement = connection.prepareStatement(sql)) {
            queryStatement.setInt(1, id);
            queryStatement.execute();
        } catch (SQLException e) {
            LoggingThread.logError("Cannot delete Device in the database: " + e.getMessage());
        }
    }

    // TODO: replace @Nullable Device with Optional<Device> for methods below

    public @Nullable Device findDeviceById(int id) {
        String sql = """
                SELECT name, location, ipAddress, isOn, data, type FROM devices WHERE id = ?;
                """;
        try (Connection connection = DriverManager.getConnection(DEVICE_DATABASE_FILENAME);
        PreparedStatement queryStatement = connection.prepareStatement(sql)) {
            queryStatement.setInt(1, id);
            ResultSet resultSet = queryStatement.executeQuery();
            if (resultSet.next()) {
                Class<Device> deviceClass = (Class<Device>) Class.forName(resultSet.getString("type"));
                Device device = deviceClass.getDeclaredConstructor().newInstance();
                device.setId(id);
                device.setLocation(resultSet.getString("location"));
                device.setData(resultSet.getString("data"));
                device.setName(resultSet.getString("name"));
                device.setIpAddress(resultSet.getString("ipAddress"));
                device.setIsOn(resultSet.getBoolean("isOn"));
                return device;
            }
        } catch (Exception e) {
            LoggingThread.log("Cannot find Device with id '" + id + "'.");
        }
        return null;
    }

    public @Nullable Device findDeviceByIpAddress(String ipAddress) {
        String sql = """
                SELECT name, location, id, isOn, data, type FROM  devices WHERE ipAddress = ?;
                """;
        try (Connection connection = DriverManager.getConnection(DEVICE_DATABASE_FILENAME);
        PreparedStatement queryStatement = connection.prepareStatement(sql)) {
            queryStatement.setString(1, ipAddress);
            ResultSet resultSet = queryStatement.executeQuery();
            if (resultSet.next()) {
                Class<Device> deviceClass = (Class<Device>) Class.forName(resultSet.getString("type"));
                Device device = deviceClass.getDeclaredConstructor().newInstance();
                device.setId(resultSet.getInt("id"));
                device.setIsOn(resultSet.getBoolean("isOn"));
                device.setIpAddress(ipAddress);
                device.setName(resultSet.getString("name"));
                device.setData(resultSet.getString("data"));
                device.setLocation(resultSet.getString("location"));
                return device;
            }
        } catch (Exception e) {
            LoggingThread.log("Cannot find Device with ipAddress '" + ipAddress + "'.");
        }
        return null;
    }

    public void saveNewDevice(@NotNull Device device) {
        String sql = """
                INSERT INTO main.devices
                (
                    type,
                    name,
                    location,
                    ipAddress,
                    isOn,
                    data
                )
                VALUES(?,?,?,?,?,?);
                """;
        String sql2 = """
                SELECT id FROM devices WHERE ipAddress = ?;
                """;
        try (Connection connection = DriverManager.getConnection(DEVICE_DATABASE_FILENAME);
        PreparedStatement selectStatement = connection.prepareStatement(sql);
        PreparedStatement queryStatement = connection.prepareStatement(sql2)) {
            selectStatement.setString(1, device.getClass().getCanonicalName());
            selectStatement.setString(2, device.getName());
            selectStatement.setString(3, device.getLocation());
            selectStatement.setString(4, device.getIpAddress());
            selectStatement.setBoolean(5, device.isOn());
            selectStatement.setString(6, device.getData());
            selectStatement.executeUpdate();
            queryStatement.setString(1, device.getIpAddress());
            ResultSet resultSet = queryStatement.executeQuery();
            device.setId(resultSet.getInt("id"));
            LoggingThread.log("New Device with ID '" + device.getId() + "' registered successfully.");
        } catch (SQLException e) {
            LoggingThread.logError("Cannot save Device in the database: " + e.getMessage());
        }
    }

    public void updateDevice(@NotNull Device device) {
        String sql = """
                UPDATE main.devices SET name = ?,
                location = ?,
                ipAddress = ?,
                isOn = ?,
                data = ?
                WHERE id = ?;
                """;
        try (Connection connection = DriverManager.getConnection(DEVICE_DATABASE_FILENAME);
        PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, device.getName());
            statement.setString(2, device.getLocation());
            statement.setString(3, device.getIpAddress());
            statement.setBoolean(4, device.isOn());
            statement.setString(5, device.getData());
            statement.setInt(6, device.getId());
            statement.executeUpdate();
            LoggingThread.log("Device with ID '" + device.getId() + "' updated successfully.");
        } catch (SQLException e) {
            LoggingThread.logError("Cannot update Device in the database: " + e.getMessage());
        }
    }

    public Set<Device> loadDevices() {
        Set<Device> devices = new HashSet<>();
        String sql = """
                SELECT type, id, name, location, ipAddress, isOn, data FROM devices;
                """;
        try (Connection connection = DriverManager.getConnection(DEVICE_DATABASE_FILENAME);
        PreparedStatement statement = connection.prepareStatement(sql)){
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Class<Device> deviceClass = (Class<Device>) Class.forName(resultSet.getString("type"));
                Device device = deviceClass.getDeclaredConstructor().newInstance();
                device.setId(resultSet.getInt("id"));
                device.setName(resultSet.getString("name"));
                device.setLocation(resultSet.getString("location"));
                device.setIpAddress(resultSet.getString("ipAddress"));
                device.setIsOn(resultSet.getBoolean("isOn"));
                device.setData(resultSet.getString("data"));
                devices.add(device);
            }
        } catch (Exception e) {
            LoggingThread.logError("Cannot load Devices from database: " + e.getMessage());
        }
        return devices;
    }

}
