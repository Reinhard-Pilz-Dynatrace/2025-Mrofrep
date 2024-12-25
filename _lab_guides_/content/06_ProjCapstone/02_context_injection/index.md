## Implement Trace Context Propagation

### Introduction

The "Processing Backend" is getting called via HTTP GET Request from your Backend Server - just not in a way that Auto Instrumentation is able to recognize that.

### 📌 Your Task: Make outgoing HTTP Requests visible

Open up `order-backend/src/main/java/com/dtcookie/shop/backend/BackendServer.java`.

Find a method named `notifyProcessingBackend`. The usage `GETRequest` clearly hints, that an outgoing HTTP request is happening here

* Enrich that method with a custom OpenTelemetry Span
* Make sure you're using the correct Span Kind
* Don't forget to add semantic attributes. `http.request.method` and `http.request.method` will be sufficient for Dynatrace.
* Handle exceptions properly in case the outgoing call fails
* Ensure that the Trace Context is getting propagated. You may want to take another look into the lab guide for `Span Creation` for an example.

<details>
<summary><strong>Expand for solution</strong></summary>

```java
public static void notifyProcessingBackend(Product product) throws Exception {
		
	GETRequest request = new GETRequest("http://order-quotes-" + System.getenv("GITHUB_USER") + ":" + "8090/quote");
	request.send();
}
```
</details>

### ✅ Verify results

* Restart your Demo Application
* Open up a recent `/place-order` trace
* Validate that the Backend Server indeed shows an outgoing HTTP call to the Python Service
