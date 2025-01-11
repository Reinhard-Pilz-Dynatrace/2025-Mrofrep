## Transform metrics

📝 **References**
- [Which distribution should I use?](https://docs.dynatrace.com/docs/shortlink/otel-collector#which-distribution-should-i-use)
- [Various use cases](https://docs.dynatrace.com/docs/ingest-from/opentelemetry/collector/use-cases) 
- [Adding a prefix to metrics](https://docs.dynatrace.com/docs/ingest-from/opentelemetry/getting-started/metrics/ingest/migration-guide-otlp-exporter#in-collector-additional-features)

Open the file <mark>**otel-collector-config.yaml** </mark> in the directory 

```
collector/otel-collector-config.yaml
```

### Add a prefix to your metrics

Scroll down to the section called `processors`, under `metricstransform`. You will see the definition

```yaml
- include: ^jvm\.(.*)$$
  match_type: regexp
  action: update
  new_name: ${GITHUB_USER}.otel.jvm.$${1}
```

This section tells the collector to use `regex` as a matcher and updates all metric names that starts with `jvm`, with a prefix of `GITHUB_USER.otel`. You can manipulate the regex to match different criteria, for example

```yaml
- include: ^shop\.(.*)$$
  match_type: regexp
  action: update
  new_name: ${GITHUB_USER}.otel.shop.$${1}
```

would allow you to modify only metric names that starts with `shop`, and update it with a prefix of `GITHUB_USER.otel`.

### Manipulate filters

Scroll down to the section called `processors`, under `filters`. You will see the definition

```yaml
  filter:
    metrics:
      exclude:
        match_type: regexp
        metric_names:
          - .*\.jvm.class*
```

This section tells the collector to use `regex` as a matcher and <mark>excludes</mark> all metrics containing the words `.jvm.class`. You can manipulate the regex to match different criteria.

### 📌 Task 1
<details>
  <summary> 📌 Add prefix to <mark>all ingested</mark> OpenTelemetry metrics. Expand to see solution.</summary>

  ```yaml
      - include: ^(.*)$$
        match_type: regexp
        action: update
        new_name: ${GITHUB_USER}.otel.$${1}
  ```
  > **NOTE**: If you are copying the text above, please be careful of the intendation. Please follow the preceding definitions if unsure.
</details>

<br/>

### 📌 Task 2
<details>
  <summary> 📌 Filter out all <mark>jvm metrics</mark>. Expand to see solution.</summary>

  ```yaml
  filter:
    metrics:
      exclude:
        match_type: regexp
        metric_names:
          - .*\.jvm.*
  ```
  > **NOTE**: If you are copying the text above, please be careful of the intendation. Please follow the preceding definitions if unsure.
</details>

<br/>

### Rebuild the containers

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
