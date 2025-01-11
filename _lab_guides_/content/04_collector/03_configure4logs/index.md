## Prepare for logs

### 📌 Task Configure the service pipelines

Apply what you have learnt and configure a new service pipeline for logs.

You can find <mark>**otel-collector-config.yaml** </mark> in the directory

```
collector/otel-collector-config.yaml
```

Scroll down to the section called `service` and make the necessary modifications. Be careful of the spaces as yaml files are sensitive to the spaces.

<details>
  <summary>Expand to see solution</summary>

  ```yaml
    logs:
      receivers: [otlp]
      processors: [batch]
      exporters: [debug, otlphttp]
  ```

</details>

<br/>

### ✅ Verify results

```bash
docker logs opentelemetry-collector 2>&1 | grep -i logs
```

Expected output
```bash
2024-12-20T12:55:18.701Z        info    builders/builders.go:26 Development component. May change in the future.        {"kind": "exporter", "data_type": "logs", "name": "debug"}
2024-12-20T12:55:24.534Z        info    Logs    {"kind": "exporter", "data_type": "logs", "name": "debug", "resource logs": 1, "log records": 2}
        {"kind": "exporter", "data_type": "logs", "name": "debug"}
```
