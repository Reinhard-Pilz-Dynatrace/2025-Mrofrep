## Implement Trace Context Propagation

### Introduction

You do not need to complete this section before attempting the next section as these 2 are mutually exclusive. If you are stuck here, you can attempt the next section and revisit this one anytime.

The "Processing Backend" is getting called via HTTP GET Request from your Backend Server - just not in a way that Auto Instrumentation is able to recognize that.

### 📌 Your Task: Make outgoing HTTP Requests visible

Open up `order-backend/src/main/java/com/dtcookie/shop/backend/BackendServer.java`.

Find a method named `notifyProcessingBackend`. The usage `GETRequest` clearly hints, that an outgoing HTTP request is happening here

* Ensure that the Trace Context is getting propagated.
   - Libraries that represent an exit point from an application, in our case, HTTP clients, should ***inject*** context into outgoing messages
   - You may want to take another look into the lab guide for `Span Creation` for an example, as well as using [OpenTelemetry documentation context propagators](https://opentelemetry.io/docs/languages/java/api/#contextpropagators) to understand the concepts with sample code.
* As `GETRequest` has been designed by the Java coder and not a known Java class/framework, you might need to understand how that class is built before you can figure out how to introduce the necessary code.
* Some hints to help you out...
   - `GETRequest`already has the TextMapSetter defined, and it is on line `38` of <mark>common/src/main/java/com/dtcookie/util/GETRequest.java</mark>
   - You will only need to call the ContextPropagators instance, and inject the current context into the GETRequest carrier, together with the TextMapSetter.

Some things to consider...
<details>
	<summary> 💡 Would the Java auto instrumentation create additional spans for you in order-backend to indicate that notifyProcessingBackend is making a call to an external service? If no, why?</summary>
The Java auto instrumentation will not create additional spans as notifyProcessingBackend method/function call is not from any of the known auto instrumentation frameworks. 
</details>
<details>
	<summary> 💡 Following the above consideration, would there be a need to create a custom OpenTelemetry Span? Why?</summary>
Technicall, if you are just concern in connecting the services together, there is no need to create a custom span. However, in terms of having clear indication of where one service starts and where the other begins, without a custom span, it is difficult to understand which part of your application makes that call. Even more so when the call fails, there will not be any indication on which method has failed. Thus making diagnostics difficult.
</details>

<br/>

We highly recommend that you enrich the method with a custom OpenTelemetry Span, and if you are attempting it, ensure that
* You are using the correct Span Kind
* You add semantic attributes. HTTP_METHOD and HTTP_URL will be sufficient for Dynatrace.
* You handle exceptions properly in case the outgoing call fails

<details>
<summary><strong>Expand for solution (without custom span creation)</strong></summary>

```java
public static void notifyProcessingBackend(Product product) throws Exception {
	GETRequest request = new GETRequest("http://order-quotes-" + System.getenv("GITHUB_USER") + ":" + "8090/quote");
	openTelemetry.getPropagators().getTextMapPropagator().inject(Context.current(), request, GETRequest.OTEL_SETTER);
	request.send();
}
```
</details>

<br/>

<details>
<summary><strong>Expand for solution (with custom span creation)</strong></summary>

```java
public static void notifyProcessingBackend(Product product) throws Exception {
	String call = "http://order-quotes-" + System.getenv("GITHUB_USER") + ":" + "8090/quote";
	Span outGoing = tracer.spanBuilder("/GET order-quotes python service").setSpanKind(SpanKind.CLIENT).startSpan();
	GETRequest request = new GETRequest("http://order-quotes-" + System.getenv("GITHUB_USER") + ":" + "8090/quote");
	try (Scope scope = outGoing.makeCurrent()) {
	  outGoing.setAttribute(SemanticAttributes.HTTP_METHOD, "GET");
	  outGoing.setAttribute(SemanticAttributes.HTTP_URL, call);
	  openTelemetry.getPropagators().getTextMapPropagator().inject(Context.current(), request, GETRequest.OTEL_SETTER);
	
	  // Make outgoing call
	  request.send();
	} catch (Exception e) {
	  outGoing.setAttribute(SemanticAttributes.HTTP_RESPONSE_STATUS_CODE, 500);
	  outGoing.recordException(e);
	  outGoing.setStatus(StatusCode.ERROR);
	  throw e;
	} finally {
	  outGoing.end();
	}
}
```
</details>

<br/>

### ✅ Verify results

* Restart your Demo Application
* Open up a recent `/place-order` trace
* Validate that the Backend Server indeed shows an outgoing HTTP call to the Python Service
