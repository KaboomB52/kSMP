package net.minebo.smp.mongo;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static com.mongodb.client.model.Filters.eq;

public class MongoManager {

    private static MongoClient mongoClient;
    private static MongoDatabase database;

    public static MongoCollection<Document> profileCollection;
    public static MongoCollection<Document> teamCollection;
    public static MongoCollection<Document> shopCollection;

    private static final Logger logger = Logger.getLogger(MongoManager.class.getName());

    // Connect to MongoDB
    public static void init(String uri, String dbName) {
        try {
            CodecRegistry pojoCodecRegistry = CodecRegistries.fromProviders(
                    PojoCodecProvider.builder().automatic(true).build()
            );

            CodecRegistry codecRegistry = CodecRegistries.fromRegistries(
                    getDefaultCodecRegistry(),
                    pojoCodecRegistry
            );

            mongoClient = MongoClients.create(uri);
            database = mongoClient.getDatabase(dbName).withCodecRegistry(codecRegistry);

            profileCollection = database.getCollection("profiles");
            teamCollection = database.getCollection("teams");
            shopCollection = database.getCollection("shopItems");

            logger.info("Connected to MongoDB at " + uri);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to connect to MongoDB", e);
        }
    }

    // Close MongoDB connection
    public static void close() {
        if (mongoClient != null) {
            mongoClient.close();
            logger.info("MongoDB connection closed.");
        }
    }
}
