#!/bin/bash

# mvn clean package

ENVIRONMENT_ID=$(echo "$DT_ENVIRONMENT_URL" | sed -E 's#^https?://([a-zA-Z0-9\-]+)\..*\..*#\1#;s#^([a-zA-Z0-9\-]+)\..*\..*#\1#;s#^([a-zA-Z0-9\-]+)#\1#')
if echo "$DT_ENVIRONMENT_URL" | grep -q ".dynatracelabs.com"; then
    DT_DOMAIN="dynatracelabs.com"
else
    DT_DOMAIN="dynatrace.com"
fi

# Set DT_STAGE based on whether the URL contains ".sprint."
if echo "$DT_ENVIRONMENT_URL" | grep -q ".sprint."; then
    DT_STAGE="sprint"
elif echo "$DT_ENVIRONMENT_URL" | grep -q ".dev."; then
    DT_STAGE="dev"
else
    DT_STAGE="live"
fi

DT_ENVIRONMENT_HOST="$ENVIRONMENT_ID.$DT_STAGE.$DT_DOMAIN"


sudo sh -c "echo \"ENVIRONMENT_ID=$ENVIRONMENT_ID\" >> /etc/environment"
echo "export ENVIRONMENT_ID=$ENVIRONMENT_ID" >> ~/.bashrc
export ENVIRONMENT_ID=$ENVIRONMENT_ID

sudo sh -c "echo \"DT_ENVIRONMENT_HOST=$DT_ENVIRONMENT_HOST\" >> /etc/environment"
echo "export DT_ENVIRONMENT_HOST=$DT_ENVIRONMENT_HOST" >> ~/.bashrc
export DT_ENVIRONMENT_HOST

# sed -i "s/DYNATRACE_DOCKER_REGISTRY/$DT_ENVIRONMENT_HOST/g" ./frontend/Dockerfile

sed -i "s/#alias ll='ls -l'/alias ll='ls -al'/g" ~/.bashrc

# # Download OpenTelemetry Java Auto Instrumentation Agent
# wget https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar

sudo nohup dockerd > /dev/null 2>&1 &
# Log into the docker registry hosted by the environment
docker login https://$DT_ENVIRONMENT_HOST --username $ENVIRONMENT_ID --password $DT_API_TOKEN
# docker-compose up -d --build
