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

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Tracer;

import java.util.Objects;

/**
 * No-op database telemetry.
 */

public final class DDatabaseTelemetryNoOp
  implements DDatabaseTelemetryType
{
  private static final DDatabaseTelemetryType INSTANCE =
    new DDatabaseTelemetryNoOp(OpenTelemetry.noop());

  private final OpenTelemetry openTelemetry;
  private final Tracer tracer;
  private final Meter meter;
  private final Logger logger;

  private DDatabaseTelemetryNoOp(
    final OpenTelemetry inOpenTelemetry)
  {
    this.openTelemetry =
      Objects.requireNonNull(inOpenTelemetry, "openTelemetry");
    this.tracer =
      this.openTelemetry.getTracer("com.io7m.darco");
    this.meter =
      this.openTelemetry.getMeter("com.io7m.darco");
    this.logger =
      this.openTelemetry.getLogsBridge()
        .get("com.io7m.darco");
  }

  /**
   * @return No-op database telemetry.
   */

  public static DDatabaseTelemetryType get()
  {
    return INSTANCE;
  }

  @Override
  public Tracer tracer()
  {
    return this.tracer;
  }

  @Override
  public Meter meter()
  {
    return this.meter;
  }

  @Override
  public Logger logger()
  {
    return this.logger;
  }

  @Override
  public boolean isNoOp()
  {
    return true;
  }
}
