package com.tahs;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.tahs.application.exceptions.BookNotFound;
import com.tahs.application.usecase.IndexService;
import com.tahs.config.AppConfig;
import com.tahs.infrastructure.cache.hazelcast.CachedInvertedIndexRepository;
import com.tahs.infrastructure.cache.hazelcast.CachedMetadataRepository;
import com.tahs.infrastructure.persistence.MongoInvertedIndexRepository;
import com.tahs.infrastructure.persistence.MongoMetadataRepository;
import com.tahs.infrastructure.serialization.books.GutenbergHeaderSerializer;
import io.javalin.Javalin;
import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;
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
                var config = CheckEnvVars(dotenv);
                createApp(config, dotenv).start(config.port());

        }

        public static Javalin createApp(AppConfig appConfig, Dotenv dotenv) {
                Javalin app = Javalin.create(config -> config.http.defaultContentType = "application/json");
                Gson gson = new GsonBuilder()
                                .registerTypeAdapter(Instant.class,
                                                (JsonSerializer<Instant>) (src, typeOfSrc, context) -> src == null
                                                                ? null
                                                                : new JsonPrimitive(src.toString()))
                                .create();

                var mongoClient = MongoClients.create(appConfig.dbUrl());
                var indexService = getIndexService(mongoClient, appConfig, dotenv);

                app.get("/index/status", ctx -> {
                        var stats = indexService.getStats();
                        ctx.result(gson.toJson(stats));
                });

                app.post("/index/update/{book_id}", ctx -> {
                        String bookId = ctx.pathParam("book_id");
                        System.out.println("Indexing book " + bookId + "...");

                        try {
                                indexService.updateByBookId(bookId);
                                Map<String, Object> response = Map.of(
                                                "book_id", bookId,
                                                "index", "updated");
                                ctx.status(200).result(gson.toJson(response));
                        } catch (BookNotFound e) {
                                ctx.status(404);
                                Map<String, Object> error = Map.of(
                                                "book_id", bookId,
                                                "error", "Book not found",
                                                "message", e.getMessage());
                                ctx.result(gson.toJson(error));
                        } catch (Exception e) {
                                ctx.status(500);
                                Map<String, Object> errorResponse = Map.of(
                                                "error", "Error interno al actualizar el índice",
                                                "details", e.getMessage());
                                ctx.result(gson.toJson(errorResponse));
                        }
                });

                app.post("/index/rebuild", ctx -> {
                        System.out.println("Rebuild Index ...");
                        long start = System.currentTimeMillis();
                        indexService.rebuildIndex();
                        long finish = System.currentTimeMillis();
                        long timeElapsed = finish - start;
                        var allBooks = indexService.getAllBooks();
                        Map<String, Object> response = Map.of(
                                        "books_processed", allBooks.size(),
                                        "elapsed_time", TimeUnit.MILLISECONDS.toSeconds(timeElapsed) + "s");
                        ctx.result(gson.toJson(response));
                });

                try {
                        var consumer = new com.tahs.infrastructure.messaging.ActiveMQIngestionEventConsumer(
                                        appConfig.activeMqUrl(),
                                        "book.events",
                                        appConfig.activeMqUsername(),
                                        appConfig.activeMqPassword(),
                                        indexService);
                        consumer.startListening();
                } catch (Exception e) {
                        System.err.println("Failed to start ActiveMQ consumer: " + e.getMessage());
                }

                return app;
        }

        private static AppConfig CheckEnvVars(Dotenv dotenv) {
                String dbUrl = Optional.ofNullable(dotenv.get("MONGO_URL"))
                                .orElse(System.getenv("MONGO_URL"));

                String databaseName = Optional.ofNullable(dotenv.get("DATABASE_NAME"))
                                .orElse(System.getenv("DATABASE_NAME"));
                String collectionMetaData = Optional.ofNullable(dotenv.get("COLLECTION_METADATA"))
                                .orElse(System.getenv("COLLECTION_METADATA"));
                String collectionIndex = Optional.ofNullable(dotenv.get("COLLECTION_INDEX"))
                                .orElse(System.getenv("COLLECTION_INDEX"));
                String portStr = Optional.ofNullable(dotenv.get("PORT"))
                                .orElse(System.getenv("PORT"));
                int port = portStr != null ? Integer.parseInt(portStr) : 8080;
                String activeMqUrl = Optional.ofNullable(dotenv.get("ACTIVEMQ_URL"))
                                .orElse(System.getenv("ACTIVEMQ_URL"));
                if (activeMqUrl == null)
                        activeMqUrl = "tcp://localhost:61616";
                String activeMqUsername = Optional.ofNullable(dotenv.get("ACTIVEMQ_USERNAME"))
                                .orElse(System.getenv("ACTIVEMQ_USERNAME"));
                String activeMqPassword = Optional.ofNullable(dotenv.get("ACTIVEMQ_PASSWORD"))
                                .orElse(System.getenv("ACTIVEMQ_PASSWORD"));
                return new AppConfig(
                                dbUrl,
                                collectionMetaData,
                                collectionIndex,
                                databaseName,
                                port,
                                activeMqUrl,
                                activeMqUsername,
                                activeMqPassword);
        }

        @NotNull
        private static IndexService getIndexService(MongoClient mongoClient, AppConfig appConfig, Dotenv dotenv) {
                String nodeIp = Optional.ofNullable(dotenv.get("NODE_IP"))
                                .orElse(System.getenv("NODE_IP"));

                var hazelcastConfig = nodeIp != null
                                ? com.tahs.config.HazelcastServerConfig.createConfigForNode(nodeIp)
                                : com.tahs.config.HazelcastServerConfig.createConfig();

                var hazelcastInstance = com.hazelcast.core.Hazelcast.newHazelcastInstance(hazelcastConfig);

                var indexRepository = new MongoInvertedIndexRepository(mongoClient, appConfig.databaseName(),
                                appConfig.collectionIndexName());
                var cachedIndexRepository = new CachedInvertedIndexRepository(
                                indexRepository, hazelcastInstance);

                var metadataRepository = new MongoMetadataRepository(mongoClient, appConfig.databaseName(),
                                appConfig.collectionMetadataName());
                var cachedMetadataRepository = new CachedMetadataRepository(
                                metadataRepository, hazelcastInstance);

                var gutenbergHeaderSerializer = new GutenbergHeaderSerializer();
                return new IndexService(cachedIndexRepository, cachedMetadataRepository, gutenbergHeaderSerializer);
        }
}
