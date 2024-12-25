## Basic plumbing for OpenTelemetry

### Introduction

Before we can begin creating Spans and providing better insights into our `Order Backend`, we should first get an overview and understanding of the OpenTelemetry setup (_the plumbing_) that is already in place for these exercises.

Reference Dynatrace documentation can be found at [this link](https://docs.dynatrace.com/docs/extend-dynatrace/opentelemetry/walkthroughs/java/java-manual). While the concepts are still the same we adapted the setup for ease of use during this training.

Let's find out what's involved.

Open up the file `common/src/main/java/com/dtcookie/util/Otel.java`.

### 📑 Key Concepts

Expand each section as needed.
<details>
  <summary><strong>Create an OpenTelemetry Resource</strong></summary>
  A resource is nothing more than the representation of an entity which produces telemetry data. In Dynatrace, Resources define the Services which tie together our observability signals (traces, metrics, logs). OpenTelemetry provides an implementation of `Resource` as part of its `SDK`, all we have to provide is attributes to describe this resouce.

  At the very minimum it pays off to define the attribute `service.name`. In our case we expect that value to be available as an Environment Variable. 
  ```java
Resource serviceName = Optional.ofNullable(System.getenv("OTEL_SERVICE_NAME"))
    .map(n -> Attributes.of(AttributeKey.stringKey("service.name"), n)).map(Resource::create)
    .orElseGet(Resource::empty);
  ```

  > 📝 **Note**: `ResourceAttributes` allows us to access "semantic attributes" - industry set standards for attribute names

  Further down in `common/src/main/java/com/dtcookie/util/Otel.java` we attempt to interrogate the OneAgent for some Dynatrace-specific attributes.

  ```java
for (String name : new String[] { "dt_metadata_e617c525669e072eebe3d0f08212e8f2.properties",
        "/var/lib/dynatrace/enrichment/dt_metadata.properties" }) {
    try {
        Properties props = new Properties();
        props.load(name.startsWith("/var") ? new FileInputStream(name) : new FileInputStream(Files.readAllLines(Paths.get(name)).get(0)));
        dtMetadata = dtMetadata.merge(Resource.create(props.entrySet().stream().collect(Attributes::builder,
                        (b, e) -> b.put(e.getKey().toString(), e.getValue().toString()),
                        (b1, b2) -> b1.putAll(b2.build()))
                .build()));
    } catch (IOException e) {
    }
}
```

  > 📝 **Note**: `dt_metadata_e617c525669e072eebe3d0f08212e8f2.json` is a file that contains a path under which OneAgent writes topology-specific dimensions related to the process reading it. This means we can provide Dynatrace with OneAgent supplied information about the Process that our Resource is running on. More on this in the [online documentation](https://www.dynatrace.com/support/help/shortlink/enrich-metrics#oneagent-file-open-interface).
</details>

<details>
<summary><strong>Set up Trace export</strong></summary>

With a resource in place that we can link our Traces to, all we need is a way to send our Traces to Dynatrace.

Before we can make use of the endpoint, we must define our "ingest pipeline". This consists of:
* **Trace Provider** - mandatory to start tracing and it associates traces with a Resource
* **Span Processor** - watches the lifecycle of spans and sends them to an exporter when appropriate; we are using a `Batch Span Processor` which batches spans together and compresses data before sending it
* **Span Exporter** - converts the spans to the required format for the backend platform; we are using the `OTLP Span Exporter` to send spans in OTLP format to the Dynatrace API

OpenTelemetry offers various ways to define that pipeline. For our workshop we have chosen to use Environment Variables - just to keep things simple.

Open up the file `order-backend/Dockerfile`. You can see this all in action with the following environment properties. 

```properties
ENV OTEL_JAVA_GLOBAL_AUTOCONFIGURE_ENABLED=true
ENV OTEL_SERVICE_NAME=order-backend-${GITHUB_USER}
ENV OTEL_EXPORTER_OTLP_PROTOCOL=http/protobuf
ENV OTEL_PROPAGATORS=tracecontext

ENV OTEL_TRACES_EXPORTER=otlp
ENV OTEL_EXPORTER_OTLP_TRACES_PROTOCOL=http/protobuf
ENV OTEL_EXPORTER_OTLP_TRACES_ENDPOINT=http://opentelemetry-collector:4318/v1/traces
```

That's it! With these settings in place you can access the Global OpenTelemetry instance from anywhere within your source code and create a Tracer.<br/>
Our order-backend does that on lines `40-41` of the source file `order-backend/src/main/com/dtcookie/shop/backend/BackendServer.java`. From this point on anywhere within this Java class a tracer will be available for use.

```java
private static OpenTelemetry openTelemetry = GlobalOpenTelemetry.get();
private static final Tracer tracer = openTelemetry.getTracer("manual-instrumentation");
```

</details>

### 📌 Your Task: Explore the distributed traces enriched by OpenTelemetry

1. Open up `order-backend/Dockerfile`
2. Remove the comment (`#`) from lines `27-35`. (***shortcut:*** CTRL+/ for windows, CMD+/ for mac)
3. Restart the Demo application (`docker compose up -d --build`)
4. Familiarize yourself with the code in the file `common/src/main/java/com/dtcookie/util/Otel.java`.
5. Explore the improvements regarding visibility within the collected traces for `/place-order`
    - Go to Services
    - Click on `order-api`
    - Inspect a more recent `/place-order` trace to become available within `Distributed traces`
    - Inspect the `Resource Attributes` of one of the additional spans (example `process` span)

#### Questions

<details><summary>💡 Where is the attribute <mark>dt.entity.host</mark> is coming from?</summary>"semantic attributes" defined in Otel.java (i.e. dt_metadata*)</details>

<details><summary>💡 Where is the attribute <mark>service.name</mark> coming from?</summary>The <mark>serviceName</mark> attributes defined in Otel.java</details>

### ✅ Verify Results

You have completed this exercise once you can visualize in Dynatrace that the credit card validation is getting picked up by the Order Backend.
