# Use official OpenJDK as base image
FROM openjdk:11-jdk-slim
LABEL authors="databach"

# Install curl and other dependencies
RUN apt-get update && \
    apt-get install -y curl bash wget && \
    rm -rf /var/lib/apt/lists/*

# Install sbt
RUN curl -L "https://github.com/sbt/sbt/releases/download/v1.9.6/sbt-1.9.6.tgz" | \
    tar -xz -C /opt/ && \
    ln -s /opt/sbt /usr/local/sbt

# Add sbt to PATH
ENV PATH="/usr/local/sbt/bin:${PATH}"

# Install OpenTelemetry Java Agent
ENV OTEL_VERSION=2.19.0
RUN wget -O /opt/opentelemetry-javaagent.jar \
    "https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v${OTEL_VERSION}/opentelemetry-javaagent.jar"


# Set OpenTelemetry environment variables
ENV OTEL_SERVICE_NAME=main
ENV OTEL_RESOURCE_ATTRIBUTES=service.name=main,service.version=1.0.0
ENV OTEL_EXPORTER_OTLP_ENDPOINT=http://otel-collector:4317
ENV OTEL_EXPORTER_OTLP_PROTOCOL=http/protobuf
ENV OTEL_TRACES_EXPORTER=otlp
ENV OTEL_METRICS_EXPORTER=otlp
ENV OTEL_LOGS_EXPORTER=otlp

# Enable Java agent debug logging
ENV OTEL_JAVAAGENT_DEBUG=true
ENV OTEL_JAVAAGENT_LOGGING=application

# Expose port 5000
EXPOSE 5000

# Set working directory
WORKDIR /app

# Copy build files first (for better caching)
COPY build.sbt .
COPY project/ ./project/

# Download dependencies (this layer will be cached)
RUN sbt update

# Copy source code
COPY src/ ./src/

# Compile the project
RUN sbt compile

# Command to run the Scala program using sbt
CMD ["sbt", "run"]

# Copy the startup script
COPY start.sh .

# Make the startup script executable
RUN chmod +x start.sh

# Set the entrypoint to the startup script
ENTRYPOINT ["./start.sh"]