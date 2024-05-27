/*
 * Copyright © 2024 Mark Raynsford <code@io7m.com> https://www.io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.io7m.darco.api;

import com.io7m.seltzer.api.SStructuredErrorType;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;

/**
 * The OpenTelemetry interface for databases.
 */

public interface DDatabaseTelemetryType
{
  /**
   * @return The main tracer
   */

  Tracer tracer();

  /**
   * @return The main meter
   */

  Meter meter();

  /**
   * @return The main logger
   */

  Logger logger();

  /**
   * Set the error kind for the current span.
   *
   * @param errorCode The error kind
   */

  static void setSpanErrorCode(
    final String errorCode)
  {
    final var span = Span.current();
    span.setAttribute("errorCode", errorCode);
    span.setStatus(StatusCode.ERROR);
  }

  /**
   * Set the error kind for the current span.
   *
   * @param e The error kind
   */

  static void recordSpanException(
    final Throwable e)
  {
    final var span = Span.current();
    if (e instanceof final SStructuredErrorType<?> ex) {
      setSpanErrorCode(ex.errorCode().toString());
    }
    span.recordException(e);
    span.setStatus(StatusCode.ERROR);
  }

  /**
   * @return {@code true} if this telemetry is in no-op mode
   */

  boolean isNoOp();
}
