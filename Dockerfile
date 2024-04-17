FROM --platform=linux/amd64 adoptopenjdk/openjdk11:alpine AS builder
WORKDIR /app
COPY build/libs/*.war ./
RUN mkdir /data && mkdir /data/alerts && mkdir /data/alerts/config
COPY alerts-config.properties /data/alerts/config/alerts-config.properties
# get latest war
RUN latest_war=$(ls -t *.war | head -1) && mv "$latest_war" app.war
FROM --platform=linux/amd64 adoptopenjdk/openjdk11:alpine
WORKDIR /app
COPY --from=builder /app/app.war .
CMD ["java", "-jar", "app.war"]