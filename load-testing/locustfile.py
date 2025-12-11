from locust import HttpUser, task, between
import random

class SearchUser(HttpUser):
    wait_time = between(1, 3)  # Simulate a user thinking for 1-3 seconds between searches

    search_terms = [
        "love", "home", "happiness", "health", "money", "engineering", "computer",
        "behavior", "meat", "waste", "network", "science", "language",
        "personality", "free", "abrupt", "distributed", "design", "artificial",
        "intelligence", "hall", "spilling", "narrative", "object", "example",
        "gold", "midway", "quiet",

        "family", "friendship", "life", "death", "dream", "hope", "fear", "time",
        "memory", "story", "world", "nature", "earth", "ocean", "forest",
        "mountain", "river", "city", "village", "street", "sky", "sun", "moon",
        "stars", "wind", "storm", "rain", "snow", "fire",

        "algorithm", "performance", "concurrency", "parallel", "database",
        "storage", "cluster", "cloud", "server", "client", "index", "inverted",
        "latency", "throughput", "cache", "replication", "consistency",
        "kubernetes", "docker", "terraform", "aws", "azure", "gcp",

        "geometry", "algebra", "calculus", "probability", "statistics",
        "analysis", "quantum", "gravity", "energy", "atom", "molecule",
        "evolution", "species", "biology", "genetics", "chemistry", "physics",

        "justice", "freedom", "power", "clarity", "chaos", "order", "silence",
        "noise", "truth", "illusion", "beauty", "pain", "suffering",
        "harmony", "balance", "strength", "fragility", "wisdom",

        "book", "chair", "table", "mirror", "window", "door", "bridge", "tower",
        "castle", "ship", "engine", "car", "machine", "tool", "device", "clock",
        "camera", "painting", "sculpture", "artifact", "structure",

        "anger", "joy", "sadness", "anxiety", "calm", "focus", "desire",
        "confidence", "curiosity", "boredom",

        "run", "walk", "fly", "jump", "think", "learn", "create", "build",
        "destroy", "imagine", "explore", "analyze", "search", "develop",

        "novel", "poetry", "drama", "fantasy", "mystery", "horror", "romance",
        "classic", "epic", "mythology", "folklore",

        "blockchain", "cryptocurrency", "metaverse", "virtual", "augmented",
        "robotics", "automation", "bigdata", "iot", "neural", "model",
        "embedding", "vector", "token", "dataset", "corpus",

        "shadow", "crystal", "ember", "flame", "whisper", "pattern", "signal",
        "origin", "legacy", "portal", "dimension", "framework", "module",
        "context", "matrix", "tensor", "gravitywell",

        "1990", "1995", "1999", "2001", "2005", "2008", "2010", "2012",
        "2015", "2018", "2020", "2022", "2023", "2024"
    ]

    @task
    def search_books(self):
        term = random.choice(self.search_terms)
        self.client.get(f"/search?q={term}", name="/search")
