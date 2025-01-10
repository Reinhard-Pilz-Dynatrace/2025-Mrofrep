## Enhance visibility

### Additional details in logs
- `utils.py` is programed to throw an exception whenever the number n >= 20 (look at line `13` and `14`).
- An exception handler has been programed and we have logged a simple string message on line `29`.
- If you noticed, the log is at ***"WARN"*** level.

#### 📌 Your Task
- Enhance the logs in `utils.py` so that is at ***exception*** level rather than warn, reference: [How to log a Python exception?](https://www.geeksforgeeks.org/how-to-log-a-python-exception/)
- Ensure that the offending ***value of n*** that causes the exception is logged as well.
- You only need to modify  `utils.py`.
- Remember to rebuild the application after any modifications `docker compose up -d --build`.

<details>
<summary><strong>Expand for solution</strong></summary>

```diff
def process(n: int) -> int:
    with tracer.start_as_current_span("process") as span:
        try:
            f = fibonacci(n)
            return f
        except Exception as e:
+           logging.getLogger().exception(n)
```

> **NOTE**: Do not copy the + character

Or more elegantly,
```python
logging.getLogger().exception("SeedNumber=%s",n)
```

</details>

<br/>

#### ✅ Verify Results
1. Open `Logs` app
1. Filter the log entries for service.name order-quotes-(replace with your github name)

<details>
<summary>💡 What do you notice of the difference in log levels?</summary>
Logs with WARN status are very basic and outputs whatever text that was programmed there, whereas logs with ERROR status contain more details.
</details>
</br>

<details>
<summary>💡 What additional details are included?</summary>
The exception details are automatically appended to the logs.
</details>

<br/>

### Exceptions in spans
- Besides logging, we can also enhance spans to capture the exceptions.
- Clearly, an exception is thrown and we want the spans to also reflect those exceptions and mark that specific span as "failed".

#### 📌 Your Task
- Modify `utils.py` to record exceptions in spans
- ***hint***: [OpenTelemetry python documentation](https://opentelemetry.io/docs/languages/python/instrumentation/#record-exceptions-in-spans)
- Remember to rebuild the application after any modifications `docker compose up -d --build`.

<details>
<summary><strong>Expand for solution</strong></summary>

```diff
def process(n: int) -> int:
    with tracer.start_as_current_span("process") as span:
        try:
            f = fibonacci(n)
            return f
        except Exception as e:
            logging.getLogger().exception("SeedNumber=%s",n)
+           span.record_exception(e)
+           span.set_status(Status(StatusCode.ERROR))
```

> **NOTE**: Do not copy the + character
</details>

<br>

#### ✅ Verify Results
1. Open `Distributed trace` app
1. Switch to `span` view and search for `process` spans
1. You will notice that the span is now ***marked*** as failure and the corresponding metadata of the exceptions are also recorded for spans which have exceptions.

<details>
<summary>💡 What are the considerations of logging the exception compared to inserting the exception message in the span itself?</summary>
<p>- Having the exception messages recorded in spans provides a quick way to diagnose and troubleshoot an application.
<p>- However, logging provides an easy way to also include other details/data that might not be suitable to input in the span events.
<p>- OpenTelemetry provides the facilities to allow for either implementation and in fact, you can also implement both as well. However, there is always an element of cost and maintainability.
<p>- Thus it is prudent to use the right method for the right objectives/purpose.
</details>

<br/>

### Logging business data
- When called, the python microserivce responses with a JSON payload, for example
```bash
curl http://localhost:8090/quote
{amount: 14, currency: "USD"}
```
- The payload is often used for business analytics or to understand the business impact of a failed service.

#### 📌 Your Task
- Think of a way to capture this information in Dynatrace.
- ***hint***: The python microservices is called using a Java **http** from `BackendServer.java`
- As with every **http** calls, there is always a response header, response code and response body.
- Remember to rebuild the application after any modifications `docker compose up -d --build`.

<details>
<summary><strong>Expand for solution</strong></summary>
<p> You can choose to log the response in Java code, or log the output of the result in the Python code.
<p> Here, we are showing an example of using Java to capture the information in Dynatrace.

```diff
	public static void notifyProcessingBackend(Product product) throws Exception {
		GETRequest request = new GETRequest("http://order-quotes-" + System.getenv("GITHUB_USER") + ":" + "8090/quote");
+		log.info(request.send());
	}
```

> **NOTE**: Do not copy the + character

> If you choose to log the data in the Python code, you will need to write more python code to configure the logging package for "info" logs. The default level is `WARNING`, which means that only events of this severity and higher will be tracked, unless the logging package is configured to do otherwise, reference: [https://docs.python.org/3/howto/logging.html](https://docs.python.org/3/howto/logging.html).
</details>

<br/>

#### ✅ Verify Results
1. Open `Logs` app
1. Log entries should appear under the `service.name` of `order-backend-*`

<details>
<summary>💡 What benefits/disadvantages of logging on the Java side and Python side?</summary>
<p> Java side: Only the caller would be interested in the responses, thus it is purdent to log at the caller side.
<p> Python side: The Python service might not be only called by the Java application. If the python application developers need the context to diagnose, it is wise to log at the python side. 
</details>
