package com.tahs;

import com.hazelcast.core.HazelcastInstance;
import com.tahs.application.dto.SearchDto;
import com.tahs.application.usecase.QueryBooksUseCase;
import com.tahs.config.AppConfig;
import com.tahs.infrastructure.hazelcast.HazelcastClientFactory;
import com.tahs.infrastructure.persistence.HazelcastInvertedIndexRepository;
import com.tahs.infrastructure.persistence.HazelcastMetadataRepository;
import io.github.cdimascio.dotenv.Dotenv;
import io.javalin.Javalin;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        var dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();
        var config = checkEnvVars(dotenv);
        createApp(config).start(config.port());
    }

    private static Javalin createApp(AppConfig appConfig) {
        // Crear cliente Hazelcast usando la fábrica
        HazelcastInstance hazelcast = HazelcastClientFactory.create(
                appConfig.hazelcastHost(),
                appConfig.hazelcastPort()
        );

        // Crear repositorios usando Hazelcast
        var indexRepository = new HazelcastInvertedIndexRepository(hazelcast);
        var metadataRepository = new HazelcastMetadataRepository(hazelcast);
        var queryUseCase = new QueryBooksUseCase(indexRepository, metadataRepository);

        Javalin app = Javalin.create(config -> config.http.defaultContentType = "application/json");

        app.get("/search", ctx -> {
            try {
                Set<String> allowedParams = Set.of("q","author", "language", "year");
                Map<String, List<String>> filteredParams = ctx.queryParamMap().entrySet().stream()
                        .filter(e -> allowedParams.contains(e.getKey()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

                var term = filteredParams.get("q").stream().findFirst().orElse(null);
                if (term == null || term.trim().isEmpty()) {
                    ctx.status(400).json(new ErrorResponse("Query parameter 'q' is required"));
                    return;
                }
                SearchDto results = queryUseCase.execute(filteredParams);
                ctx.json(results);
            } catch (Exception e) {
                ctx.status(500).json(new ErrorResponse("Search error: " + e.getMessage()));
                e.printStackTrace();
            }
        });

        // Endpoint de health check
        app.get("/health", ctx -> {
            ctx.json(Map.of(
                    "status", "UP",
                    "hazelcast", Map.of(
                            "host", appConfig.hazelcastHost(),
                            "port", appConfig.hazelcastPort()
                    )
            ));
        });

        // Shutdown hook para cerrar Hazelcast
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down Hazelcast client...");
            hazelcast.shutdown();
        }));

        return app;
    }

    public static class ErrorResponse {
        public String error;

        public ErrorResponse(String error) {
            this.error = error;
        }
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
                                .orElse("9090")));

        return new AppConfig(hazelcastHost, hazelcastPort, port);
    }
}
