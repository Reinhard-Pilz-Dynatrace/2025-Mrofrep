## Customizing Spans for better insights

### Introduction

While tracing provides us the most basic visibility into our transactions and allows to see the end-to-end picture, in most cases there will be a need to capture additional detail relevant to troubleshooting problems.

In this exercise you'll modify your previously created Spans to capture additional details.

### 📑 Key Concepts

<details>
  <summary><strong>Adding attributes</strong></summary>

  Spans allow us to store simple custom information relevant to our troubleshooting as Attributes. If you're familiar with Dynatrace Request Attributes, OpenTelemetry's Span attributes are very similar.

  To add an attribute we can use the `setAttribute` function of the Span which takes the name and value of the attribute:

```java
Span span = tracer.spanBuilder("<span-name>").startSpan();
try (Scope scope = span.makeCurrent()) {
    span.setAttribute("<attribute-name>", attributeValue);
    // business logic
} finally {
    span.end();
}
```
</details>

---

<details>
  <summary><strong>Recording events</strong></summary>

  While attributes are great for storing simple single values, events can carry more information and mark a specific meaningful point in time.

  Adding an event is done using `addEvent`, which takes the title of the event and optionally a set of attributes of the event. Here's an example:

```java
Span span = tracer.spanBuilder("<span-name>").startSpan();
try (Scope scope = span.makeCurrent()) {
    // business logic

    // add a simple event without attributes
    span.addEvent("<attribute-title>");
    // add an event with attributes
    span.addEvent("<attribute-title>", Attributes.builder().put("<attribute-name>", productName).build());

    // business logic
} finally {
    span.end();
}  
```
</details>

---

<details>
  <summary><strong>Recording failures</strong></summary>

  Very often our code will be designed to handle failures and either recover or take an alternative path. However, for monitoring purposes, we want to be aware of any internal graceful failures that don't necessarily stop a transaction.

  For this purpose, OpenTelemetry allows setting a status for a Span which can be `Unset`, `Ok`, or `Error`. Similar to recording events, failure details including stack traces can be recorded using the `recordException` method which takes an `Exception`/`Throwable` along with optional attributes.

  You can see this in action in `BackendServer.java` on lines `150-152`:

  ```java
try {
  ...
} catch (Exception e) {
    serverSpan.setAttribute(SemanticAttributes.HTTP_RESPONSE_STATUS_CODE, 500);
    serverSpan.recordException(e);
    serverSpan.setStatus(StatusCode.ERROR);
}
```
</details>

### 📌 Your Tasks

In <mark>BackendServer.java</mark> revisit the methods `deductFromLocation` and `checkStorageLocations`:
1. Add the attributes `product.name`, `location.name` and `quantity` to the Span you have introduced to `deductFromLocation` in the earlier exercise.
2. In case `checkStorageLocations` doesn't find a suitable location with enough products available, add an event named `nothing deducted` to the Span `check-storage-locations`. Optionally, also add an attribute `product.name` to the event.
3. In addition to that Event, let's also set the Span Status, signalling that this is some sort of error.  

> 💡 **Hint:** You can get the name of a StorageLocation by calling `location.getName()`

4. Restart the application to verify any changes:

<details>
  <summary>Show solution</summary>

```java
public static void checkStorageLocations(String productName, int quantity) {
    Span span = tracer.spanBuilder("check-storage-locations").setSpanKind(SpanKind.INTERNAL).startSpan();
    try (Scope scope = span.makeCurrent()) {
        boolean deducted = false;
        for (StorageLocation location : StorageLocation.getAll()) {
            if (location.available(productName, quantity)) {
                deductFromLocation(location, productName, quantity);
                deducted = true;
                break;
            }
        }
        if (!deducted) {
            span.addEvent("nothing deducted", io.opentelemetry.api.common.Attributes.builder().put("product.name", productName).build());
        }
    } finally {
        span.end();
    }
}

public static void deductFromLocation(StorageLocation location, String productName, int quantity) {
    Span span = tracer.spanBuilder("deduct").setSpanKind(SpanKind.INTERNAL).startSpan();
    try (Scope scope = span.makeCurrent()) {
        span.setAttribute("product.name", productName);
        span.setAttribute("location.name", location.getName());
        span.setAttribute("quantity", quantity);
        location.deduct(productName, quantity);
      } finally {
        span.end();
      }
    }
```

</details>

### ✅ Verify results

Open the `order-api` service in Dynatrace and open one of the `/place-order` traces from its Distributed traces. 

You should verify that your attributes have been registered.<br />
Any orders where no location to deduct the product(s) from could be found, should also have an Event attached and should be flagged as erroneous. You can view your event in the Events tab, and depending on the span you may also observe a failure recorded in the Errors tab.

> 📑 **Note:** Span and event attributes are not captured by Dynatrace by default. When you first see your attribute, click the `+` button to store it on future traces.
