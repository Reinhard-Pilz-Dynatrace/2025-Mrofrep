## Creating Spans within the Order Backend

### Introduction

Head over to `order-backend/src/main/java/com/dtcookie/shop/backend/BackendServer.java` to begin.

Spans can be manually created to allow for custom/fine grain naming. This action is commonly known as ***instrumentation*** and the objective is to collect "telemetry data".

### 📑 Key Concepts

Expand as needed.

<details>
  <summary><strong>Create custom Spans</strong></summary>

  In Java, Spans can be created by first aquiring a `tracer`. Tracers offer the functionality to build and start Spans.

  **Example**

```java
Tracer tracer = GlobalOpenTelemetry.get().getTracer("my-tracer-name");
Span span = tracer.spanBuilder("my-span").setSpanKind(SpanKind.INTERNAL).startSpan();
try (Scope scope = span.makeCurrent()) {
  // ...
  // perform some business logic in here
  // ...
} finally {
  span.end();
}
```

  > 📝 **Note:** It is usually not necessary to create a Tracer every time you want to create a Span. In our demo app the BackendServer creates a reusable `tracer` right in the beginning - on line `41`.
</details>

<br/>

### 📌 Your Task

In `order-backend/src/main/java/com/dtcookie/shop/backend/BackendServer.java`:
1. Search for the method `checkStorageLocations`. In here the Storage Locations are getting checked whether the requested quantity of a specific product is available. It eventually calls the method `deductFromLocation` which can be found a little farther below. 
2. Create an additional Span whenever `deductFromLocation` is getting invoked. You can use `checkStorageLocations` in order to figure out what additional code is necessary.   
3. Restart the demo application to verify any changes:

<details>
  <summary>Show solution</summary>

```java
public static void deductFromLocation(StorageLocation location, String productName, int quantity) {
    Span span = tracer.spanBuilder("deduct").setSpanKind(SpanKind.INTERNAL).startSpan();
    try (Scope scope = span.makeCurrent()) {
      location.deduct(productName, quantity);
    } finally {
      span.end();
    }
}
```
</details>
<br/>

### ✅ Verify results

Open the `order-api` service in Dynatrace and open one of the `/place-order` traces from its Distributed traces. 

Except for a few outliers these Traces should now contain the new Span - signalling that a suitable storage location has been found from which to deduct the ordered product(s). We will deal with these outliers in our next excercise.
