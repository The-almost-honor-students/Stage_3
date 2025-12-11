# Load Testing Search API with Locust

This directory contains a Locust script to load test the Search API.

## Prerequisites

- [Python 3.x](https://www.python.org/downloads/)
- [Locust](https://locust.io/) installed via pip:

```bash
pip install locust
```

## Running the Load Test

1. Navigate to this directory:
    ```bash
    cd load-testing
    ```

2. Run Locust to raise a WEB UI with dashboards and logs where we can see how it interacts:
    ```bash
    locust -f load-testing/locustfile.py
      ```
    Or specify the host directly if running from CLI without UI (headless):
    ```bash
    locust --headless --users 10 --spawn-rate 1 -H http://localhost:9090
    ```

3. Open your browser and go to `http://localhost:8089`.

4. Enter the number of users, spawn rate, and the target host (e.g., `http://localhost:9090` if the search service is running locally on port 9090).

5. Click **Start swarming**.
