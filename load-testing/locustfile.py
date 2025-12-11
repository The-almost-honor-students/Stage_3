from locust import HttpUser, task, between
import random

class SearchUser(HttpUser):
    wait_time = between(1, 3)  # Simulate a user thinking for 1-3 seconds between searches

    search_terms = [
        "love", "home", "happiness", "health", "money",
        "engineering", "computer", "behavior", "meat", "waste",
        "network", "science", "language", "personality", "free",
        "abrupt", "distributed", "design", "artificial", "intelligence",
        "hall","spilling","narrative", "object", "example", "gold", "midway",
        "quiet"
    ]

    @task
    def search_books(self):
        term = random.choice(self.search_terms)
        self.client.get(f"/search?q={term}", name="/search")
