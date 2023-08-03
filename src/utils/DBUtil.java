package utils;

import models.devices.Device;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import threads.LoggingThread;

import java.sql.*;
import java.util.HashSet;
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
        } catch (SQLException e) {
            LoggingThread.logError("FATAL: Cannot initialize database: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

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
        } catch (SQLException e) {
            LoggingThread.logError("FATAL: Cannot save Device in the database: " + e.getMessage());
            throw new RuntimeException(e);
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
        } catch (SQLException e) {
            LoggingThread.logError("FATAL: Cannot update Device in the database: " + e.getMessage());
            throw new RuntimeException(e);
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
            LoggingThread.logError("FATAL: Cannot load Devices from database: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return devices;
    }

}
