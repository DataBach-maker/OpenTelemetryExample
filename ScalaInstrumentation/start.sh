#!/bin/bash

echo "Starting Simple Tracing Program with OpenTelemetry Java Agent..."
echo "Service Name: $OTEL_SERVICE_NAME"
echo "OTLP Endpoint: $OTEL_EXPORTER_OTLP_ENDPOINT"
echo "Traces Exporter: $OTEL_TRACES_EXPORTER"
echo "Metrics Exporter: $OTEL_METRICS_EXPORTER"
echo "Logs Exporter: $OTEL_LOGS_EXPORTER"
echo "Debug Mode: ULTRA VERBOSE"
echo "Exposed Port: 5000"
echo "=================================="

# Start the application with maximum OpenTelemetry debugging
exec sbt -J-javaagent:/opt/opentelemetry-javaagent.jar \
     -J-Dotel.service.name="$OTEL_SERVICE_NAME" \
     -J-Dotel.resource.attributes="$OTEL_RESOURCE_ATTRIBUTES" \
     -J-Dotel.exporter.otlp.endpoint="$OTEL_EXPORTER_OTLP_ENDPOINT" \
     -J-Dotel.traces.exporter="$OTEL_TRACES_EXPORTER" \
     -J-Dotel.metrics.exporter="$OTEL_METRICS_EXPORTER" \
     -J-Dotel.logs.exporter="$OTEL_LOGS_EXPORTER" \
     -J-Dotel.javaagent.debug=true \
     -J-Dotel.javaagent.logging=application \
     -J-Dotel.exporter.otlp.timeout=30000 \
     -J-Dotel.bsp.export.timeout=30000 \
     -J-Dio.opentelemetry.javaagent.slf4j.simpleLogger.defaultLogLevel=DEBUG \
     -J-Dio.opentelemetry.javaagent.slf4j.simpleLogger.log.io.opentelemetry=DEBUG \
     run