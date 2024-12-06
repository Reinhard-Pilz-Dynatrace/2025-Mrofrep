ARG GITHUB_USER

FROM python:3.10-slim

WORKDIR /app

COPY order-quotes/requirements.txt .

RUN pip install --no-cache-dir -r requirements.txt

COPY . /app

EXPOSE 8090

ARG GITHUB_USER
ARG DT_API_TOKEN
ARG DT_ENVIRONMENT_HOST
ENV OTEL_SERVICE_NAME=order-quotes-${GITHUB_USER}

ENTRYPOINT ["sh", "-c", "python order-quotes/main.py"]
# CMD ["python", "order-quotes/main.py"]