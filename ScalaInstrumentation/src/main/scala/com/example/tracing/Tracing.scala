package com.example.tracing

import io.opentelemetry.api.trace.{Span, SpanKind, Tracer, StatusCode}
import io.opentelemetry.context.{Context, Scope}
import scala.util.control.NonFatal

/**
 * Provides OpenTelemetry tracing capabilities with automatic span management.
 * 
 * This trait handles the creation and management of OpenTelemetry spans,
 * including error handling and context propagation. It's designed to work
 * with the application's lifecycle, automatically cleaning up resources.
 */
trait Tracing {
  
  /** The OpenTelemetry tracer instance */
  protected val tracer: Tracer
  
  /** The application's root span */
  @volatile private var rootSpan: Option[Span] = None
  @volatile private var scope: Option[Scope] = None
  
  /**
   * Starts a new root span for the application.
   * Should be called when the application starts.
   * 
   * @param spanName The name for the root span (default: "app")
   * @return The created span
   */
  protected def startApplicationSpan(spanName: String = "app"): Span = synchronized {
    if (rootSpan.isEmpty) {
      val span = tracer.spanBuilder(spanName)
        .setSpanKind(SpanKind.INTERNAL)
        .startSpan()
      
      // Make this span the current context
      val newScope = span.makeCurrent()
      
      rootSpan = Some(span)
      scope = Some(newScope)
      
      // Add shutdown hook to ensure span is properly closed in case of premature termination
      sys.addShutdownHook {
        endApplicationSpan()
      }
      
      span
    } else {
      throw new IllegalStateException("Application span already started")
    }
  }
  
  /**
   * Ends the application span.
   * Should be called when the application is shutting down.
   */
  protected def endApplicationSpan(): Unit = synchronized {
    try {
      // End the span if it exists
      rootSpan.foreach { span =>
        if (span.getSpanContext.isValid) {
          span.end()
        }
      }
      
      // Close the scope if it exists
      scope.foreach(_.close())
    } finally {
      rootSpan = None
      scope = None
    }
  }
  
  /**
   * Gets the current application span.
   * 
   * @return The current span if it exists
   */
  protected def currentSpan: Option[Span] = rootSpan
  
  /**
   * Executes a block of code within a child span.
   * 
   * @param name The name of the child span
   * @param kind The span kind (default: INTERNAL)
   * @param block The block of code to execute
   * @tparam T The return type of the block
   * @return The result of the block
   */
  protected def withSpan[T](name: String, kind: SpanKind = SpanKind.INTERNAL)(block: => T): T = {
    val span = rootSpan match {
      case Some(parent) =>
        tracer.spanBuilder(name)
          .setParent(Context.current().`with`(parent))
          .setSpanKind(kind)
          .startSpan()
      case None =>
        throw new IllegalStateException("Application span not started. Call startApplicationSpan() first.")
    }
    
    val scope = span.makeCurrent()
    
    try {
      val result = block
      span.setStatus(StatusCode.OK)
      result
    } catch {
      case NonFatal(e) =>
        span.recordException(e)
        span.setStatus(StatusCode.ERROR, e.getMessage)
        throw e
    } finally {
      scope.close()
      span.end()
    }
  }
  
  /**
   * Adds an event to the current span.
   * 
   * @param name The name of the event
   * @param attributes Optional attributes to add to the event
   */
  /**
   * Adds an attribute to the current span.
   * 
   * @param key The attribute key
   * @param value The attribute value (will be converted to string)
   */
  protected def addSpanAttribute(key: String, value: Any): Unit = {
    currentSpan.foreach(_.setAttribute(key, value.toString))
  }
  
  /**
   * Adds an event to the current span.
   * 
   * @param name The name of the event
   * @param attributes Optional attributes to add to the event
   */
  protected def addSpanEvent(name: String, attributes: (String, Any)*): Unit = {
    currentSpan.foreach { span =>
      val event = span.addEvent(name)
      attributes.foreach { case (key, value) =>
        event.setAttribute(key, value.toString)
      }
    }
  }
}

/**
 * Companion object providing utility methods for the Tracing trait.
 */
object Tracing {
  
  /**
   * Creates a new Tracing instance with the provided tracer.
   */
  def apply(t: Tracer): Tracing = new Tracing {
    override protected val tracer: Tracer = t
  }
}
