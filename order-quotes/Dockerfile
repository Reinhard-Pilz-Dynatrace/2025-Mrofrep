ARG GITHUB_USER

FROM python:3.10-slim

WORKDIR /app

COPY order-quotes/requirements.txt .

RUN pip install --no-cache-dir -r requirements.txt
RUN opentelemetry-bootstrap -a install

COPY . /app

EXPOSE 8090

ARG GITHUB_USER
ARG DT_API_TOKEN
ARG DT_ENVIRONMENT_HOST

## Python will require gRPC
ENV OTEL_EXPORTER_OTLP_ENDPOINT=http://opentelemetry-collector:4317

## setup for traces
ENV OTEL_SERVICE_NAME=order-quotes-${GITHUB_USER}
ENV OTEL_TRACES_EXPORTER=otlp

## setup for metrics
ENV OTEL_EXPORTER_OTLP_METRICS_TEMPORALITY_PREFERENCE=DELTA
ENV OTEL_METRICS_EXPORTER=otlp

## setup for logs exporter and logging
ENV OTEL_LOGS_EXPORTER=otlp
ENV OTEL_PYTHON_LOGGING_AUTO_INSTRUMENTATION_ENABLED=true
#ENV OTEL_PYTHON_LOG_LEVEL=error
## More configuration examples can be found here: https://opentelemetry.io/docs/zero-code/python/configuration/#logging

## Start auto-instrumentation
ENTRYPOINT ["sh", "-c", "opentelemetry-instrument python order-quotes/main.py"]