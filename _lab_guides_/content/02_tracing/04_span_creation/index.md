## Creating Spans within the Order Backend

### Introduction

Now that we're familiar with the OpenTelemetry setup, it's time to start tracing a few missing transactions.

You may have noticed in the previous exercise, that the `/place-order` traces of service `order-api` are not only reaching out to the backend for credit card validation.<br />
Another backend call addresses `http://localhost:54041/check-inventory/....`. And after checking out the distributed traces of `order-backend` it becomes clear, that the order backend is **already** reporting the incoming web requests to Dynatrace.

One of the topics in this exercise we will be to make sure that Dynatrace is able to correlate both sides and show them within a single trace.

Head over to `order-backend/src/main/java/com/dtcookie/shop/backend/BackendServer.java` to begin.

### 📑 Key Concepts

Expand each section as needed.

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

<details>
  <summary><strong>Set Span Context</strong></summary>

  We can think of `Context` as the glue that holds all Spans together in the same Trace. When a transaction goes across different processes and resources, the default OpenTelemetry behavior is to assign a new `Context` to each Span, thus creating separate transactions. We can modify this behavior by propagating `Context` across traces.

  **For incoming traces...**

  The method `handleCreditcards` within our BackendServer is already taking care of `Context Propagation` properly.<br />
  Before it attempts to create a Span, it inspects the HTTP Request Headers - specifically it looks for the Headers `traceparent` and `traceid`. A `Context` based on these headers tells Dynatrace, which Client Side HTTP Request this Server Side matches up with.

```java
Context ctx = openTelemetry.getPropagators().getTextMapPropagator().extract(Context.current(), request, getter);
try (Scope ctScope = ctx.makeCurrent()) {
  // ...
}
```

  > 📝 **Note:** We've hidden a few details here to keep things simple. If you're interested in how a `Context Propagator` needs to get implemented, take a look at method `newRequestHeaderGetter()` in `Otel.java`.

  **For outgoing Traces...**

  On the client side things are working very similar. Instead of **extracting** the `Context` it needs to get **injected** into outgoing call. Like in this example, where the necessary headers are getting added to an outgoing HTTP Request.
  
```java
// Tell OpenTelemetry to inject the context in the HTTP headers
TextMapSetter<HttpURLConnection> setter =
  new TextMapSetter<HttpURLConnection>() {
    @Override
    public void set(HttpURLConnection carrier, String key, String value) {
        // Insert the context as Header
        carrier.setRequestProperty(key, value);
    }
};

URL url = new URL("...");
Span outGoing = tracer.spanBuilder("/GET " + url).setSpanKind(SpanKind.CLIENT).startSpan();
try (Scope scope = outGoing.makeCurrent()) {
  // Use the Semantic Conventions.
  // (Note that to set these, Span does not *need* to be the current instance in Context or Scope.)
  outGoing.setAttribute(SemanticAttributes.HTTP_METHOD, "GET");
  outGoing.setAttribute(SemanticAttributes.HTTP_URL, url.toString());
  HttpURLConnection transportLayer = (HttpURLConnection) url.openConnection();
  
  // Inject the request with the *current*  Context, which contains our current Span.
  openTelemetry.getPropagators().getTextMapPropagator().inject(Context.current(), transportLayer, setter);

  // Make outgoing call
} finally {
  outGoing.end();
}
  ```

  > 📝 **Note:** The `HttpURLConnection` above will now hold the trace context that is needed to stitch any downstream spans to this current one.

</details>

<br/>

<details>
  <summary><strong>Setting Span Kind</strong></summary>

  You may have noticed in the previous examples the term `Span Kind`. Not all Spans are getting treated equally. There are several possible kinds:

  * **SERVER** - span covers server-side handling of a synchronous request
  * **CLIENT** - span describes a request to some remote service
  * **PRODUCER** - span describes the inititor of an asynchronous request
  * **CONSUMER** - span describes a child of an asynchronous request
  * **INTERNAL** - (default value) representing an internal operation without remote calls

  In other words, setting the Span Kind like in this example
  ```java
  Span span = tracer.spanBuilder("my-span").setSpanKind(SpanKind.INTERNAL).startSpan();
  ```
  is optional. If you want to signal that a synchronous request has been received, the OpenTelemetry SDK expects you to signal that
  ```java
  Span span = tracer.spanBuilder("my-span").setSpanKind(SpanKind.SERVER).startSpan();
  ```
</details>

<br/>

### 📌 Your First Task

In `order-backend/src/main/java/com/dtcookie/shop/backend/BackendServer.java`:
1. Search for the method `handleCreditcards`. We have seen in our previous tasks, that Dynatrace receives correct signals at this point.
2. Search for the method `handleInventory`. The nature of this method is **very** similar to `handleCreditCards` - it receives a HTTP request.
    * Compare both methods regarding the OpenTelemetry SDK calls
    * Method `handleInventory` does not yet handle `Context Propagation` correctly
    * Use `handleCreditCards` as a template to fix `handleInventory`    
3. Restart the demo application to verify any changes:

<details>
  <summary>Show solution</summary>

  ```java
	public static String handleInventory(HttpServletRequest request) throws Exception {
		String url = request.getRequestURI();
		String productName = url.substring(url.lastIndexOf("/"));
		int quantity = 1;

		Context ctx = openTelemetry.getPropagators().getTextMapPropagator().extract(Context.current(), request, getter);

		try (Scope ignored = ctx.makeCurrent()) {
			Span serverSpan = tracer.spanBuilder(request.getRequestURI()).setSpanKind(SpanKind.SERVER).startSpan();
			try (Scope scope = serverSpan.makeCurrent()) {
				serverSpan.setAttribute(SemanticAttributes.HTTP_REQUEST_METHOD,	request.getMethod().toUpperCase());
				serverSpan.setAttribute(SemanticAttributes.URL_SCHEME, "http");
				serverSpan.setAttribute(SemanticAttributes.SERVER_ADDRESS, "order-backend-" + System.getenv("GITHUB_USER") + ":" + Ports.INVENTORY_LISTEN_PORT);
				serverSpan.setAttribute(SemanticAttributes.URL_PATH, request.getRequestURI());
				
				Database.execute("SELECT * FROM products WHERE name = '" + productName + "'");

				checkStorageLocations(productName, quantity);
		
				serverSpan.setAttribute(SemanticAttributes.HTTP_RESPONSE_STATUS_CODE, 200);
				return "done";
			} catch (Exception e) {
				serverSpan.setAttribute(SemanticAttributes.HTTP_RESPONSE_STATUS_CODE, 500);
				serverSpan.recordException(e);
				serverSpan.setStatus(StatusCode.ERROR);
				log.warn("checking inventory failed", e);
				throw e;
			} finally {
				serverSpan.end();
			}
		}
	}
  ```
</details>
<br/>

### ✅ Verify results

Open the `order-api` service in Dynatrace and open one of the `/place-order` traces from its Distributed traces. 

The HTTP GET Request `/check-inventory` should now extend into the Order Backend.

### 📌 Your Second Task

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
