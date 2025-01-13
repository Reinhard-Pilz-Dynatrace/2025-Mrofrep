## Receiving basic observability signals

### 📑 References
- [Zero-code instrumentation](https://opentelemetry.io/docs/concepts/instrumentation/zero-code)
- [Python Zero-code reference](https://opentelemetry.io/docs/zero-code/python/)

### 📑 File structure
- the directory `order-quotes` contain all the python codes
- `main.py` is the main python progam
- `utils.py` is the a sub python program that is called by main
- `Dockerfile` is the file to dockerize the running of the python application. It contains the environment variables, installing the required python packages, start sequence etc.
- `requirements.txt` is the required python packages

### 📌 Your Task: Deploy the OpenTelemetry zero code insturmentation

Some ***hints***
- You will only need to modify `Dockerfile`.
- A few key considerations:
     - Installing the auto instrumentation agent package (this has already been done for you).
     - Setting up the OTLP exporters for the various signals (traces, metrics, logs).
     - Configuring additional environment variables.
     - Starting the auto-insturmentation with the python program.
- Once you are done modifying the files, rebuild by running `docker compose up -d --build`

<details>
<summary><strong>Expand for solution</strong></summary>

The Dockerfile should contain these environment variables in order to receive traces, metrics and logs from the Python service.

```properties
## Python will require gRPC
ENV OTEL_EXPORTER_OTLP_ENDPOINT=http://opentelemetry-collector:4317

## setup for traces
ENV OTEL_SERVICE_NAME=order-quotes-${GITHUB_USER}
ENV OTEL_TRACES_EXPORTER=otlp

## setup for metrics
ENV OTEL_EXPORTER_OTLP_METRICS_TEMPORALITY_PREFERENCE=DELTA
ENV OTEL_METRICS_EXPORTER=otlp

## setup for logs exporter and logging
ENV OTEL_LOGS_EXPORTER=otlp
ENV OTEL_PYTHON_LOGGING_AUTO_INSTRUMENTATION_ENABLED=true

## Start auto-instrumentation
ENTRYPOINT ["sh", "-c", "opentelemetry-instrument python order-quotes/main.py"]
```
</details>

</br>

💡 Questions
<details><summary> 💡 What happens when you don't set <mark>OTEL_EXPORTER_OTLP_METRICS_TEMPORALITY_PREFERENCE=DELTA`</mark>? </summary> Dynatrace will not ingest the metrics and the Otel collector will register that the metrics has been dropped.</details>
<details><summary> 💡 Where can you see that the Otel collector is dropping the metrics? </summary>

Collector logs. You can get the logs by running this command
     
```sh
docker logs opentelemetry-collector 2>&1 | grep -i dropped
```

If you had enabled it eariler, disable it, do a docker compose down to shutdown everything and start the apps up again to understand the impact.

</details>

<br>

### ✅ Verify Results

1. Validate that you are receiving the new Python `traces` in Dynatrace
    - Open `Distributed trace` app
    - Filter and search for the Python service `order-quotes-(your github username)`
1. Validate that you are receiving `logs` from the new Python service
    - Open `Logs` app
    - Use the `service.name` and search for `order-quotes-(your github username)`
1. Validate that you are receiving `metrics` from the Python auto instrumentation
    - Open `Notebooks` app
    - Explore `metrics` and search for python, you should see metric names that contain python
    - If you had implemented the renaming of all metrics in the Collector, these metrics will now also contain your GitHub user name.otel 
