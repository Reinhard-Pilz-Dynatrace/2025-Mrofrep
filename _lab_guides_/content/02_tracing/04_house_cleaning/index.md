## House Cleaning

### Introduction

We've now learned how OpenTelemetry Signals are getting produced - either because the application already was pre-instrumented or when auto-instrumentation kicked in and added even more visibility.

But unless you're in charge of adding the code snippets that are talking to the OpenTelemetry SDK you don't seem to have control over how much of that data eventually ends up at your OpenTelemetry Backend.

.... unless your OpenTelemetry Backend is Dynatrace.

### 📌 Your Tasks

The developer who was in charge of adding OpenTelemetry Spans to the Frontend Server was a bit overzealous. If you take a detailed look at the `/place-order` traces, you'll notice that they contain spans named `persist-purchase-confirmation-#` - in an unnecessarily high amount.

In your Dynatrace Environment navigate to `Settings Classic > Server-side service monitoring`.
Find a way to exclude all the spans that start with the name `persist-purchase-confirmation-` from getting captured by Dynatrace.

> 💡 **Note:** This feature requires OneAgent Fullstack to be present. It doesn't apply to traces ingested via OTLP.

### ✅ Verify results

Open the `order-api` service in Dynatrace and open one of the `/place-order` traces from its Distributed traces. 

The spans that have been previously spammed your traces should now be gone

### 💡 Questions
* Why was it not required to restart your Demo Application in that case?
