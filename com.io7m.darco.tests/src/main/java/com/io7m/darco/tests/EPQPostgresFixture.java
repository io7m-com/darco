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


package com.io7m.darco.tests;

import com.io7m.ervilla.api.EContainerFactoryType;
import com.io7m.ervilla.api.EContainerType;
import com.io7m.ervilla.api.EPortAddressType;
import com.io7m.ervilla.postgres.EPgSpecs;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * A PostgreSQL fixture.
 */

public final class EPQPostgresFixture
{
  private static final Logger LOG =
    LoggerFactory.getLogger(EPQPostgresFixture.class);

  private final EContainerType container;
  private final String databaseOwner;
  private final int port;

  private EPQPostgresFixture(
    final EContainerType inContainer,
    final String inDatabaseOwner,
    final int inPort)
  {
    this.container =
      Objects.requireNonNull(inContainer, "container");
    this.databaseOwner =
      Objects.requireNonNull(inDatabaseOwner, "databaseOwner");
    this.port =
      inPort;
  }

  public static EPQPostgresFixture create(
    final EContainerFactoryType supervisor,
    final int port)
    throws Exception
  {
    LOG.info(
      "Creating postgresql database on {}", Integer.valueOf(port));

    return new EPQPostgresFixture(
      supervisor.start(
        EPgSpecs.builderFromDockerIO(
          EPQTestProperties.POSTGRESQL_VERSION,
          new EPortAddressType.All(),
          port,
          "postgresql",
          "postgresql",
          "12345678"
        ).build()
      ),
      "postgresql",
      port
    );
  }

  public EContainerType container()
  {
    return this.container;
  }

  public String databaseOwner()
  {
    return this.databaseOwner;
  }

  public int port()
  {
    return this.port;
  }

  /**
   * Reset the container by dropping and recreating the database. This
   * is significantly faster than destroying and recreating the container.
   *
   * @throws IOException          On errors
   * @throws InterruptedException On interruption
   */

  public void reset()
    throws IOException, InterruptedException
  {
    {
      final var r =
        this.container.executeAndWait(
            List.of(
              "dropdb",
              "-f",
              "-w",
              "-U",
              this.databaseOwner(),
              "postgresql"
            ),
            10L,
            TimeUnit.SECONDS
          );
      Assertions.assertEquals(0, r);
    }

    {
      final var r =
        this.container.executeAndWait(
            List.of(
              "createdb",
              "-w",
              "-U",
              this.databaseOwner(),
              "postgresql"
            ),
            10L,
            TimeUnit.SECONDS
          );
      Assertions.assertEquals(0, r);
    }
  }
}
