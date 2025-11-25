package com.tahs;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.google.gson.Gson;
import com.hazelcast.core.HazelcastInstance;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.tahs.application.exceptions.BookNotFound;
import com.tahs.application.usecase.IndexService;
import com.tahs.config.AppConfig;
import com.tahs.infrastructure.hazelcast.HazelcastClientFactory;
import com.tahs.infrastructure.persistence.HazelcastInvertedIndexRepository;
import com.tahs.infrastructure.persistence.HazelcastMetadataRepository;
import com.tahs.infrastructure.persistence.MongoMetadataRepository;
import com.tahs.infrastructure.serialization.books.GutenbergHeaderSerializer;
import io.javalin.Javalin;
import io.github.cdimascio.dotenv.Dotenv;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) {
        var dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();
        var config = checkEnvVars(dotenv);

        var hazelcast = HazelcastClientFactory.create(
                config.hazelcastHost(),
                config.hazelcastPort()
        );

        // MongoDB solo para leer los libros originales (si lo necesitas)
        MongoClient mongoClient = null;
        if (config.dbUrl() != null) {
            mongoClient = MongoClients.create(config.dbUrl());
        }

        final MongoClient finalMongoClient = mongoClient;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            hazelcast.shutdown();
            if (finalMongoClient != null) {
                finalMongoClient.close();
            }
        }));

        createApp(config, hazelcast, mongoClient).start(config.port());
    }

    public static Javalin createApp(AppConfig config, HazelcastInstance hazelcast, MongoClient mongoClient) {
        Javalin app = Javalin.create(c -> c.http.defaultContentType = "application/json");

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Instant.class,
                        (JsonSerializer<Instant>) (src, typeOfSrc, context) ->
                                src == null ? null : new JsonPrimitive(src.toString()))
                .create();

        var indexRepository = new HazelcastInvertedIndexRepository(hazelcast);
        var metadataRepository = new HazelcastMetadataRepository(hazelcast);
        var gutenbergHeaderSerializer = new GutenbergHeaderSerializer();

        // Si necesitas leer de MongoDB los libros originales
        MongoMetadataRepository mongoMetadataRepo = null;
        if (mongoClient != null) {
            mongoMetadataRepo = new MongoMetadataRepository(
                    mongoClient,
                    config.databaseName(),
                    config.collectionMetadataName()
            );
        }

        var indexService = new IndexService(indexRepository, metadataRepository, gutenbergHeaderSerializer);

        app.get("/index/status", ctx -> {
            var stats = indexService.getStats();
            ctx.result(gson.toJson(Map.of(
                    "stats", stats,
                    "books_in_cache", metadataRepository.size()
            )));
        });

        app.post("/index/update/{book_id}", ctx -> {
            String bookId = ctx.pathParam("book_id");
            System.out.println("Indexing book " + bookId + "...");

            try {
                indexService.updateByBookId(bookId);
                ctx.status(200).result(gson.toJson(Map.of(
                        "book_id", bookId,
                        "index", "updated"
                )));
            } catch (BookNotFound e) {
                ctx.status(404).result(gson.toJson(Map.of(
                        "book_id", bookId,
                        "error", "Book not found",
                        "message", e.getMessage()
                )));
            } catch (Exception e) {
                ctx.status(500).result(gson.toJson(Map.of(
                        "error", "Error interno al actualizar el índice",
                        "details", e.getMessage()
                )));
            }
        });

        app.post("/index/rebuild", ctx -> {
            System.out.println("Rebuild Index ...");
            long start = System.currentTimeMillis();
            indexService.rebuildIndex();
            long finish = System.currentTimeMillis();
            long timeElapsed = finish - start;
            var allBooks = indexService.getAllBooks();
            ctx.result(gson.toJson(Map.of(
                    "books_processed", allBooks.size(),
                    "elapsed_time", TimeUnit.MILLISECONDS.toSeconds(timeElapsed) + "s"
            )));
        });

        app.get("/health", ctx -> {
            ctx.result(gson.toJson(Map.of(
                    "status", "ok",
                    "hazelcast", "connected"
            )));
        });

        return app;
    }

    private static AppConfig checkEnvVars(Dotenv dotenv) {
        String hazelcastHost = Optional.ofNullable(dotenv.get("HAZELCAST_HOST"))
                .orElse(Optional.ofNullable(System.getenv("HAZELCAST_HOST"))
                        .orElse("localhost"));

        int hazelcastPort = Integer.parseInt(
                Optional.ofNullable(dotenv.get("HAZELCAST_PORT"))
                        .orElse(Optional.ofNullable(System.getenv("HAZELCAST_PORT"))
                                .orElse("5701")));

        int port = Integer.parseInt(
                Optional.ofNullable(dotenv.get("PORT"))
                        .orElse(Optional.ofNullable(System.getenv("PORT"))
                                .orElse("8080")));

        return new AppConfig(hazelcastHost, hazelcastPort, port);
    }

    private static String getEnv(Dotenv dotenv, String key, String defaultValue) {
        return Optional.ofNullable(dotenv.get(key))
                .or(() -> Optional.ofNullable(System.getenv(key)))
                .orElse(defaultValue);
    }
}