package com.purpleclique.javahomeserver.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.mongodb.client.*;
import com.purpleclique.javahomeserver.models.auth.Token;
import com.purpleclique.javahomeserver.models.auth.User;
import com.purpleclique.javahomeserver.models.devices.Device;
import com.purpleclique.javahomeserver.models.dto.DeviceDTO;
import com.purpleclique.javahomeserver.threads.LoggingThread;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class DBUtil {

    public static final String DATABASE_URL = "mongodb://127.0.0.1:27017";
    private static final String DATABASE_NAME = "HomeServerDBTest2";

    public static final String DEVICE_COLLECTION = "devices";
    public static final String LOCATION_COLLECTION = "locations";
    public static final String HOME_COLLECTION = "home";
    public static final String USER_COLLECTION = "users";
    public static final String TOKEN_COLLECTION = "tokens";

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
        try (MongoClient mongoClient = MongoClients.create(DATABASE_URL)) {
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
            database.createCollection(DEVICE_COLLECTION);
            database.createCollection(LOCATION_COLLECTION);
            database.createCollection(HOME_COLLECTION);
            database.createCollection(USER_COLLECTION);
            database.createCollection(TOKEN_COLLECTION);
        }
    }

    public Optional<User> findUserByUsername(String username) {
        try (MongoClient mongoClient = MongoClients.create(DATABASE_URL)) {
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
            MongoCollection<Document> collection = database.getCollection(USER_COLLECTION);
            Document query = new Document("username", username);
            MongoCursor<Document> cursor = collection.find(query).iterator();
            if (cursor.hasNext()) {
                String json = cursor.next().toJson();
                ObjectMapper mapper = new ObjectMapper();
                cursor.close();
                return Optional.of(mapper.readValue(json, User.class));
            }
            cursor.close();
        } catch (Exception e) {
            LoggingThread.logError("Error getting User by username from database: " + e.getMessage());
        }
        return Optional.empty();
    }

    public Set<User> getAllUsers() {
        Set<User> userSet = new HashSet<>();
        try (MongoClient mongoClient = MongoClients.create(DATABASE_URL)) {
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
            MongoCollection<Document> collection = database.getCollection(USER_COLLECTION);
            MongoCursor<Document> cursor = collection.find().iterator();
            while (cursor.hasNext()) {
                String json = cursor.next().toJson();
                ObjectMapper mapper = new ObjectMapper();
                userSet.add(mapper.readValue(json, User.class));
            }
            cursor.close();
            return userSet;
        } catch (Exception e) {
            LoggingThread.logError("Error getting all Users from database: " + e.getMessage());
        }
        return userSet;
    }

    public Optional<User> findUserById(String id) {
        try (MongoClient mongoClient = MongoClients.create(DATABASE_URL)) {
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
            MongoCollection<Document> collection = database.getCollection(USER_COLLECTION);
            Document query = new Document("id", id);
            MongoCursor<Document> cursor = collection.find(query).iterator();
            if (cursor.hasNext()) {
                String json = cursor.next().toJson();
                ObjectMapper mapper = new ObjectMapper();
                cursor.close();
                return Optional.of(mapper.readValue(json, User.class));
            }
            cursor.close();
        } catch (Exception e) {
            LoggingThread.logError("Error getting User by id from database: " + e.getMessage());
        }
        return Optional.empty();
    }

    public void updateUser(@NotNull User user) {
        try (MongoClient mongoClient = MongoClients.create(DATABASE_URL)) {
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
            MongoCollection<Document> collection = database.getCollection(USER_COLLECTION);
            Document query = new Document("id", user.getId());
            ObjectMapper mapper = new ObjectMapper();
            ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
            String json = writer.writeValueAsString(user);
            collection.replaceOne(query, Document.parse(json));
        } catch (Exception e) {
            LoggingThread.logError("Error updating User by id in the database: " + e.getMessage());
        }
    }

    public void saveNewUser(@NotNull User user) {
        try (MongoClient mongoClient = MongoClients.create(DATABASE_URL)) {
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
            MongoCollection<Document> collection = database.getCollection(USER_COLLECTION);
            user.setId(ObjectId.get().toString());
            ObjectMapper mapper = new ObjectMapper();
            ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
            String json = writer.writeValueAsString(user);
            collection.insertOne(Document.parse(json));
        } catch (Exception e) {
            LoggingThread.logError("Error saving new User in the database: " + e.getMessage());
        }
    }

    public void saveHome(String homeName) {
        try (MongoClient mongoClient = MongoClients.create(DATABASE_URL)) {
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
            MongoCollection<Document> collection = database.getCollection(HOME_COLLECTION);
            collection.insertOne(new Document("homeName", homeName).append("main", true));
        }
    }

    public void updateHomeName(String homeName) {
        try (MongoClient mongoClient = MongoClients.create(DATABASE_URL)) {
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
            MongoCollection<Document> collection = database.getCollection(HOME_COLLECTION);
            Document query = new Document("main", true);
            Document data = new Document("homeName", homeName);
            collection.updateOne(query, new Document("$set", data));
        }
    }

    public Optional<String> getHomeName() {
        try (MongoClient mongoClient = MongoClients.create(DATABASE_URL)) {
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
            MongoCollection<Document> collection = database.getCollection(HOME_COLLECTION);
            MongoCursor<Document> mongoCursor = collection.find().iterator();
            if (mongoCursor.hasNext()) {
                String homeName = mongoCursor.next().getString("homeName");
                mongoCursor.close();
                return Optional.ofNullable(homeName);
            }
            mongoCursor.close();
        }
        return Optional.empty();
    }

    public Optional<Token> findTokenById(String id) {
        try (MongoClient mongoClient = MongoClients.create(DATABASE_URL)) {
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
            MongoCollection<Document> collection = database.getCollection(TOKEN_COLLECTION);
            Document query = new Document("id", id);
            MongoCursor<Document> cursor = collection.find(query).iterator();
            if (cursor.hasNext()) {
                String json = cursor.next().toJson();
                ObjectMapper mapper = new ObjectMapper();
                cursor.close();
                return Optional.of(mapper.readValue(json, Token.class));
            }
            cursor.close();
        } catch (Exception e) {
            LoggingThread.logError("Error getting Token by id from database: " + e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<Token> findToken(String token) {
        try (MongoClient mongoClient = MongoClients.create(DATABASE_URL)) {
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
            MongoCollection<Document> collection = database.getCollection(TOKEN_COLLECTION);
            Document query = new Document("token", token);
            MongoCursor<Document> cursor = collection.find(query).iterator();
            if (cursor.hasNext()) {
                String json = cursor.next().toJson();
                ObjectMapper mapper = new ObjectMapper();
                cursor.close();
                return Optional.of(mapper.readValue(json, Token.class));
            }
            cursor.close();
        } catch (Exception e) {
            LoggingThread.logError("Error getting User by username from database: " + e.getMessage());
        }
        return Optional.empty();
    }

    public void updateToken(@NotNull Token token) {
        try (MongoClient mongoClient = MongoClients.create(DATABASE_URL)) {
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
            MongoCollection<Document> collection = database.getCollection(TOKEN_COLLECTION);
            Document query = new Document("id", token.getId());
            ObjectMapper mapper = new ObjectMapper();
            ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
            String json = writer.writeValueAsString(token);
            collection.findOneAndReplace(query, Document.parse(json));
        } catch (Exception e) {
            LoggingThread.logError("Error updating Token in the database: " + e.getMessage());
        }
    }

    public void saveNewToken(@NotNull Token token) {
        try (MongoClient mongoClient = MongoClients.create(DATABASE_URL)) {
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
            MongoCollection<Document> collection = database.getCollection(TOKEN_COLLECTION);
            token.setId(ObjectId.get().toString());
            ObjectMapper mapper = new ObjectMapper();
            ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
            String json = writer.writeValueAsString(token);
            collection.insertOne(Document.parse(json));
        } catch (Exception e) {
            LoggingThread.logError("Error saving new Token in the database: " + e.getMessage());
        }
    }

    public void deleteAllValidTokensByPersonId(String userId) {
        try (MongoClient mongoClient = MongoClients.create(DATABASE_URL)) {
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
            MongoCollection<Document> collection = database.getCollection(TOKEN_COLLECTION);
            Document query = new Document("tokenHolderId", userId);
            collection.deleteMany(query);
        } catch (Exception e) {
            LoggingThread.logError("Error deleting Tokens in the database: " + e.getMessage());
        }
    }

    public void deleteDeviceById(String id) {
        try (MongoClient mongoClient = MongoClients.create(DATABASE_URL)) {
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
            MongoCollection<Document> collection = database.getCollection(DEVICE_COLLECTION);
            Document query = new Document("device", new Document("id", id));
            collection.findOneAndDelete(query);
        } catch (Exception e) {
            LoggingThread.logError("Error deleting Device by id in the database: " + e.getMessage());
        }
    }

    public void deleteAllDevices() {
        try (MongoClient mongoClient = MongoClients.create(DATABASE_URL)) {
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
            MongoCollection<Document> collection = database.getCollection(DEVICE_COLLECTION);
            Document query = new Document();
            collection.deleteMany(query);
        } catch (Exception e) {
            LoggingThread.logError("Error deleting Device by id in the database: " + e.getMessage());
        }
    }

    public Optional<Device> findDeviceById(String id) {
        try (MongoClient mongoClient = MongoClients.create(DATABASE_URL)) {
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
            MongoCollection<Document> collection = database.getCollection(DEVICE_COLLECTION);
            Document query = new Document("device", new Document("id", id));
            MongoCursor<Document> cursor = collection.find(query).iterator();
            if (cursor.hasNext()) {
                String json = cursor.next().toJson();
                ObjectMapper mapper = new ObjectMapper();
                cursor.close();
                return Optional.of(mapper.readValue(json, DeviceDTO.class).getDevice());
            }
            cursor.close();
        } catch (Exception e) {
            LoggingThread.logError("Error getting Device by id from database: " + e.getMessage());
        }
        return Optional.empty();
    }

    public Set<Device> getDevicesByLocation(String location) {
        Set<Device> deviceSet = new HashSet<>();
        try (MongoClient mongoClient = MongoClients.create(DATABASE_URL)) {
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
            MongoCollection<Document> collection = database.getCollection(DEVICE_COLLECTION);
            Document query = new Document("device", new Document("location", location));
            MongoCursor<Document> cursor = collection.find(query).iterator();
            while (cursor.hasNext()) {
                String json = cursor.next().toJson();
                ObjectMapper mapper = new ObjectMapper();
                deviceSet.add(mapper.readValue(json, DeviceDTO.class).getDevice());
            }
            cursor.close();
        } catch (Exception e) {
            LoggingThread.logError("Error getting Device by id from database: " + e.getMessage());
        }
        return deviceSet;
    }

    public Optional<Device> findDeviceByIpAddress(String ipAddress) {
        try (MongoClient mongoClient = MongoClients.create(DATABASE_URL)) {
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
            MongoCollection<Document> collection = database.getCollection(DEVICE_COLLECTION);
            Document query = new Document("device", new Document("ipAddress", ipAddress));
            MongoCursor<Document> cursor = collection.find(query).iterator();
            if (cursor.hasNext()) {
                String json = cursor.next().toJson();
                ObjectMapper mapper = new ObjectMapper();
                cursor.close();
                return Optional.of(mapper.readValue(json, DeviceDTO.class).getDevice());
            }
            cursor.close();
        } catch (Exception e) {
            LoggingThread.logError("Error getting Device by id from database: " + e.getMessage());
        }
        return Optional.empty();
    }

    public void saveNewDevice(@NotNull Device device) {
        try (MongoClient mongoClient = MongoClients.create(DATABASE_URL)) {
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
            MongoCollection<Document> collection = database.getCollection(DEVICE_COLLECTION);
            ObjectMapper mapper = new ObjectMapper();
            ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
            device.setId(ObjectId.get().toString());
            var locations = getLocations();
            if (!locations.contains(device.getLocation())) {
                saveNewLocation(device.getLocation());
            }
            DeviceDTO deviceDTO = DeviceDTO.builder()
                    .deviceType(device.getClass().getSimpleName())
                    .device(device)
                    .build();
            String json = writer.writeValueAsString(deviceDTO);
            collection.insertOne(Document.parse(json));
        } catch (Exception e) {
            LoggingThread.logError("Error saving new Device in the database: " + e.getMessage());
        }
    }

    public void updateDevice(@NotNull Device device) {
        try (MongoClient mongoClient = MongoClients.create(DATABASE_URL)) {
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
            MongoCollection<Document> collection = database.getCollection(DEVICE_COLLECTION);
            ObjectMapper mapper = new ObjectMapper();
            ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
            DeviceDTO deviceDTO = DeviceDTO.builder()
                    .deviceType(device.getClass().getSimpleName())
                    .device(device)
                    .build();
            var locations = getLocations();
            if (!locations.contains(device.getLocation())) {
                saveNewLocation(device.getLocation());
            }
            String json = writer.writeValueAsString(deviceDTO);
            Document query = new Document("device", new Document("id", device.getId()));
            collection.findOneAndReplace(query, Document.parse(json));
        } catch (Exception e) {
            LoggingThread.logError("Error saving new Device in the database: " + e.getMessage());
        }
    }

    public Set<Device> getAllDevices() {
        Set<Device> deviceSet = new HashSet<>();
        try (MongoClient mongoClient = MongoClients.create(DATABASE_URL)) {
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
            MongoCollection<Document> collection = database.getCollection(DEVICE_COLLECTION);
            MongoCursor<Document> cursor = collection.find().iterator();
            while (cursor.hasNext()) {
                String json = cursor.next().toJson();
                ObjectMapper mapper = new ObjectMapper();
                deviceSet.add(mapper.readValue(json, DeviceDTO.class).getDevice());
            }
            cursor.close();
        } catch (Exception e) {
            LoggingThread.logError("Error getting Device by id from database: " + e.getMessage());
        }
        return deviceSet;
    }

    public void saveNewLocation(String location) {
        try (MongoClient mongoClient = MongoClients.create(DATABASE_URL)) {
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
            MongoCollection<Document> collection = database.getCollection(LOCATION_COLLECTION);
            collection.insertOne(new Document("location", location));
        } catch (Exception e) {
            LoggingThread.logError("Error saving new Location in the database: " + e.getMessage());
        }
    }

    public void deleteLocation(String location) {
        try (MongoClient mongoClient = MongoClients.create(DATABASE_URL)) {
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
            MongoCollection<Document> collection = database.getCollection(LOCATION_COLLECTION);
            Document query = new Document("location", location);
            collection.deleteOne(query);
        } catch (Exception e) {
            LoggingThread.logError("Error deleting Location in the database: " + e.getMessage());
        }
    }

    public Set<String> getLocations() {
        Set<String> locations = new HashSet<>();
        try (MongoClient mongoClient = MongoClients.create(DATABASE_URL)) {
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
            MongoCollection<Document> collection = database.getCollection(LOCATION_COLLECTION);
            MongoCursor<Document> cursor = collection.find().iterator();
            while (cursor.hasNext()) {
                locations.add(cursor.next().getString("location"));
            }
            cursor.close();
        } catch (Exception e) {
            LoggingThread.logError("Error getting Locations from the database: " + e.getMessage());
        }
        return locations;
    }



}
