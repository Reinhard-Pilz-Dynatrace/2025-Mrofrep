## Java Auto Instrumentation

### Introduction

The Order Backend is now able to report traces into Dynatrace. But the only additional visibility we **are** getting is, that the an inbound call to validate credit cards is getting successfully picked up. We could have figured that out by just looking at the outbound HTTP call on the Frontend - and Dynatrace was capturing that already out of the box.

The developer of the Order Backend simply didn't spend a lot of thoughts on observability - and the same thing can be said about the libraries the backend depends on.

Luckily this is not the end of the road. For well known frameworks and libraries the OpenTelemetry community has already come up with a solution, that's based on auto-instrumentation.

In this exercise you'll add OpenTelemetry's auto-instrumentation for Java in order to get even more insight into what's happening within the Order Backend.

### 📑 Key Concepts

<details>
  <summary><strong>What is auto-instrumentation</strong></summary>

  Auto-instrumentation is a way of adding core observability signals to a previously unmonitored system. Using common knowledge about the techonology, many frameworks come with packages/modules which are ready to automate the core interactions within.

  While saving a lot of effort, auto-instrumentation won't know about the deeper innerworkings of the system. Chances are, you'll still have to customize or create additional spans to fully observe your system.

</details>

<details>
  <summary><strong>Auto-instrumentation for Java</strong></summary>

  Because the Order Backend is based on Java, the obvious solution for auto-instrumentation is to attach a Java Agent to the JVM.<br />
  Normally OneAgent would be able to perform exactly that job, but in this special case it's not able to inject the Agent Module for Deep Monitoring. 

  The Dockerfile of the Order backend (`order-backend/Dockerfile`) already downloads the OpenTelemetry Java Agent. Attaching that Agent to the Backend JVM can get achieved via JVM Argument `-javaagent:opentelemetry-javaagent.jar`.

  The effect will be that a long list of [supported libraries](https://github.com/open-telemetry/opentelemetry-java-instrumentation/blob/main/docs/supported-libraries.md#libraries--frameworks) will automatically get enriched with the capability to report traces and spans via OpenTelemetry.  
</details>

### 📌 Your Tasks

In `order-backend/Dockerfile` navigate to line `63`. It tells Docker which process should keep the container alive.

`ENTRYPOINT ["java", "-jar", "order-backend.jar"]`

All we need to do here is to add a JVM argument.

`ENTRYPOINT ["java", "-javaagent:opentelemetry-javaagent.jar", "-jar", "order-backend.jar"]`

The next time you are launching the demo application it will ensure that the OpenTelemetry Agent is getting attached to the backend.

Restart the Demo Application (`docker compose up -d --build`)

### ✅ Verify results

Open the `order-api` service in Dynatrace and open one of the `/place-order` traces from its Distributed traces. 

Verify new visibility within the traces:
* `process` is reaching out to the Database
* Within `process` additional `post-process` spans are visible
* `post-process` is also reaching out to the Database
* The backend responds back via asynchronous `GET` request to the frontend `order-api`
* Inspect the span metadata of these additional spans, specifically the `Instrumentation scope` and the `thread.name`

### 💡 Questions
* Which of these spans got introduced by auto-instrumentation?
* Take a detailed look at method `process` at line `70` in `order-backend/src/main/java/com/dtcookie/shop/backend/BackendServer`. On line `76` it is scheduling the invocation of `postprocess` to be executed by a Thread Pool. What could have been the reason for `postprocess` no reporting any spans until we've enabled Auto Instrumentation?
