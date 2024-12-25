## Transform metrics

📝 **References**
- [Which distribution should I use?](https://docs.dynatrace.com/docs/shortlink/otel-collector#which-distribution-should-i-use)
- [Various use cases](https://docs.dynatrace.com/docs/ingest-from/opentelemetry/collector/use-cases) 
- [Adding a prefix to metrics](https://docs.dynatrace.com/docs/ingest-from/opentelemetry/getting-started/metrics/ingest/migration-guide-otlp-exporter#in-collector-additional-features)

### 📌 Task 1: Add a prefix to your metrics

Choose either:
- Add prefix to only `shop.*` metrics, or
- Add prefix to `all ingested` OpenTelemetry metrics

You can find <mark>**otel-collector-config.yaml** </mark> in the directory

```
collector/otel-collector-config.yaml
```

Scroll down to the section called `processors`. Make the necessary changes under `metricstransform`.

<details>
  <summary>Expand to see solution for <strong>shop</strong> metrics</summary>

  <h4> Add additional rules </h4>

  ```yaml
      - include: ^shop\.(.*)$$
        match_type: regexp
        action: update
        new_name: ${GITHUB_USER}.otel.shop.$${1}
  ```
  > **NOTE**: be careful of the intendation. Please follow the preceding definitions if unsure.
</details>

<br/>

<details>
  <summary>Expand to see solution for <strong>all</strong> metrics</summary>

  <h4> Modify existing rule </h4>

  ```yaml
      - include: ^(.*)$$
        match_type: regexp
        action: update
        new_name: ${GITHUB_USER}.otel.$${1}
  ```
  > **NOTE**: be careful of the intendation. Please follow the preceding definitions if unsure.
</details>

### 📌 Task 2 Manipulate filters

Choose either:
- Impose stricter filtering rules, or
- Remove filter to allow all metrics to be sent

<details>
  <summary>Expand to see solution for <strong>stricter</strong> filtering rules</summary>

  Modify `metric_names` to exclude all JVM metrics under `filter` 

  ```yaml
  filter:
    metrics:
      exclude:
        match_type: regexp
        metric_names:
          - .*\.jvm.*
  ```
  > **NOTE**: be careful of the intendation. Please follow the preceding definitions if unsure.
</details>

<br/>

<details>
  <summary>Expand to see solution for <strong>removing</strong> filter</summary>

  In the service pipelines, under processes, remove `filter`

  ```yaml
    metrics:
      receivers: [otlp]
      processors: [batch, metricstransform]
      exporters: [debug, otlphttp]
  ```

  > **NOTE**: be careful of the intendation. Please follow the preceding definitions if unsure.
</details>

#### Rebuild the containers

For the OpenTelemetry Collector, you will need to completely stop and remove all containers. To do so, run the command

```bash
docker compose down
```

Once all the containers are stopped, run the `compose up` command again.

```bash
docker compose up -d --build
```

### ✅ Verify results

Validate `metricstransform`

```bash
docker logs opentelemetry-collector 2>&1 | grep -i "${GITHUB_USER}.otel."
```
Expected output: list of metric names matching `${GITHUB_USER}.otel.*`

Validate `filter`

```bash
docker logs opentelemetry-collector 2>&1 | grep -i filter
```

Expected output if strict filter rules are in place. If you chose to remove the filters, the above command will not yield any output.

```bash
2024-12-20T12:55:18.711Z        info    filterprocessor@v0.116.0/metrics.go:99  Metric filter configured        {"kind": "processor", "name": "filter", "pipeline": "metrics", "include match_type": "", "include expressions": [], "include metric names": [], "include metrics with resource attributes": null, "exclude match_type": "regexp", "exclude expressions": [], "exclude metric names": ["(.*)\\.otel.jvm.*"], "exclude metrics with resource attributes": null}
```

- Launch a `Notebook` or `Dashboard` app, use the `Metric explorer` to search for the metrics

### 💡 Troubleshooting tips

Check log output of the OpenTelemetry collector container
```bash
docker logs opentelemetry-collector
```
