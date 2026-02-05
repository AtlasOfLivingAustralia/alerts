# Use Amazon Corretto 17 as the base image
FROM amazoncorretto:17

# Set environment variables for non-interactive installations
ENV DEBIAN_FRONTEND=noninteractive

# Optional: update package list (if you need other packages)
RUN yum -y update && yum clean all

# Set working directory
WORKDIR /app

# Copy the WAR file
COPY build/libs/*.war app.war

# Set the entrypoint to run the WAR
CMD ["java", "-jar", "app.war"]