# OpenTelemetry Scala Instrumentation

This project demonstrates how to integrate OpenTelemetry with Scala applications, specifically focusing on Apache Spark jobs.

## Features

- **Tracing**: Automatic and manual span creation for monitoring application flow
- **Spark Integration**: Custom Spark Listeners for tracing Spark job execution
- **Error Handling**: Proper error capture and status setting in traces
- **Context Propagation**: Maintains context across async operations

## Architecture Overview

### Spark Listeners and OpenTelemetry Integration

1. **Spark Listeners Architecture**:
   - Event-driven system for monitoring Spark applications
   - Extends SparkListener abstract class to override specific event methods
   - Registered with SparkContext.addSparkListener()
   - Runs asynchronously to avoid impacting Spark performance

2. **OpenTelemetry Concepts**:
   - **Span**: Represents a single operation in a trace (has start/end times)
   - **Trace**: Collection of spans representing a complete request flow
   - **Attributes**: Key-value metadata attached to spans for filtering/analysis
   - **Events**: Timestamped log entries within a span's lifecycle

3. **Tracing Strategy**:
   - Create span before any Spark operations begin
   - Register listener to capture automatic Spark events
   - Add manual events for business logic milestones
   - Include custom attributes for operational metrics
   - Ensure proper span lifecycle management (start -> end)

4. **Best Practices**:
   - Always end spans to prevent memory leaks
   - Use structured attributes instead of string concatenation in event names
   - Register listeners before triggering operations they should monitor
   - Include business-relevant metrics alongside technical metrics
   - Use consistent naming conventions for spans and attributes

5. **Production Considerations**:
   - Listener overhead: Keep processing lightweight to avoid performance impact
   - Sampling: Use trace sampling to control data volume in high-throughput systems
   - Error handling: Ensure listener failures don't crash Spark applications
   - Security: Avoid logging sensitive data in span attributes or events
