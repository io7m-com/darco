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

import com.io7m.darco.api.DDatabaseCreate;
import com.io7m.darco.api.DDatabaseException;
import com.io7m.darco.api.DDatabaseTelemetryNoOp;
import com.io7m.darco.api.DDatabaseUpgrade;
import com.io7m.darco.api.DRoles;
import com.io7m.darco.api.DUsernamePassword;
import com.io7m.darco.examples.postgresql.EPQDatabaseConfiguration;
import com.io7m.darco.examples.postgresql.EPQDatabaseFactory;
import com.io7m.darco.examples.postgresql.EPQDatabaseType;
import com.io7m.darco.examples.sqlite.ESWordGetType;
import com.io7m.darco.examples.sqlite.ESWordPutType;
import com.io7m.ervilla.api.EContainerSupervisorType;
import com.io7m.ervilla.test_extension.ErvillaCloseAfterSuite;
import com.io7m.ervilla.test_extension.ErvillaConfiguration;
import com.io7m.ervilla.test_extension.ErvillaExtension;
import com.io7m.zelador.test_extension.ZeladorExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.io7m.darco.api.DDatabaseUnit.UNIT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith({ErvillaExtension.class, ZeladorExtension.class})
@ErvillaConfiguration(projectName = "com.io7m.darco", disabledIfUnsupported = true)
public final class EPQDatabaseTest
{
  private static EPQPostgresFixture POSTGRES_FIXTURE;
  private EPQDatabaseFactory databases;
  private EPQDatabaseType database;

  @BeforeAll
  public static void setupOnce(
    final @ErvillaCloseAfterSuite EContainerSupervisorType containers)
    throws Exception
  {
    POSTGRES_FIXTURE =
      EPQFixtures.postgres(EPQFixtures.pod(containers));
  }

  @BeforeEach
  public void setup()
    throws Exception
  {
    POSTGRES_FIXTURE.reset();

    this.databases =
      new EPQDatabaseFactory();

    final var owner =
      new DUsernamePassword("postgresql", "12345678");
    final var worker =
      new DUsernamePassword("postgresql", "12345678");

    this.database =
      this.databases.open(
        new EPQDatabaseConfiguration(
          Optional.empty(),
          DDatabaseTelemetryNoOp.get(),
          DDatabaseCreate.CREATE_DATABASE,
          DDatabaseUpgrade.UPGRADE_DATABASE,
          "localhost",
          POSTGRES_FIXTURE.port(),
          "postgresql",
          false,
          owner,
          worker,
          new DRoles(
            Map.ofEntries(
              Map.entry(owner.userName(), owner)
            )
          )
        ),
        event -> {

        }
      );
  }

  @Test
  public void testUsage()
    throws DDatabaseException
  {
    try (var c = this.database.openConnection()) {
      try (var t = c.openTransaction()) {
        final var qp = t.query(ESWordPutType.class);
        qp.execute("Word0");
        t.commit();
      }

      try (var t = c.openTransaction()) {
        final var qg = t.query(ESWordGetType.class);
        assertEquals("Word0", qg.execute(UNIT).orElseThrow());
      }
    }
  }

  @Test
  public void testCloseConnection()
    throws DDatabaseException
  {
    final var closed = new AtomicBoolean(false);
    try (var c = this.database.openConnection()) {
      c.registerResource(() -> {
        closed.set(true);
      });
    }
    assertTrue(closed.get());
  }

  @Test
  public void testCloseTransaction()
    throws DDatabaseException
  {
    final var closed = new AtomicBoolean(false);
    try (var c = this.database.openConnection()) {
      try (var t = c.openTransaction()) {
        t.registerResource(() -> {
          closed.set(true);
        });
      }
    }
    assertTrue(closed.get());
  }

  @Test
  public void testCloseTransactionImplicit()
    throws DDatabaseException
  {
    final var closed = new AtomicBoolean(false);
    try (var c = this.database.openConnection()) {
      final var t = c.openTransaction();
      t.registerResource(() -> {
        closed.set(true);
      });
    }
    assertTrue(closed.get());
  }
}
