ARG BASE_IMAGE=ubuntu:24.04
FROM ${BASE_IMAGE}

WORKDIR /app
COPY build/libs/*.war app.war
CMD ["java", "-jar", "app.war"]