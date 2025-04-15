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


package com.io7m.darco.examples.postgresql;

import com.io7m.darco.api.DDatabaseCreate;
import com.io7m.darco.api.DDatabaseTelemetryType;
import com.io7m.darco.api.DDatabaseUpgrade;
import com.io7m.darco.api.DRoles;
import com.io7m.darco.api.DUsernamePassword;
import com.io7m.darco.postgres.DPQDatabaseConfigurationType;
import com.io7m.jxe.core.JXEHardenedSAXParsers;

import java.util.Objects;
import java.util.Optional;

/**
 * The configuration information for the example PostgreSQL database.
 *
 * @param saxParsers      The SAX parser factory
 * @param telemetry       The telemetry interface
 * @param create          The database creation option
 * @param upgrade         The database upgrade option
 * @param databaseAddress The database address
 * @param databaseName    The database name
 * @param databasePort    The database port
 * @param databaseUseTLS  Whether to use TLS to connect to the database
 * @param ownerRole       The database owner role
 * @param workerRole      The database worker role
 * @param roles           The roles
 */

public record EPQDatabaseConfiguration(
  Optional<JXEHardenedSAXParsers> saxParsers,
  DDatabaseTelemetryType telemetry,
  DDatabaseCreate create,
  DDatabaseUpgrade upgrade,
  String databaseAddress,
  int databasePort,
  String databaseName,
  boolean databaseUseTLS,
  DUsernamePassword ownerRole,
  DUsernamePassword workerRole,
  DRoles roles)
  implements DPQDatabaseConfigurationType
{
  /**
   * The configuration information for the example PostgreSQL database.
   *
   * @param saxParsers      The SAX parser factory
   * @param telemetry       The telemetry interface
   * @param create          The database creation option
   * @param upgrade         The database upgrade option
   * @param databaseAddress The database address
   * @param databaseName    The database name
   * @param databasePort    The database port
   * @param databaseUseTLS  Whether to use TLS to connect to the database
   * @param ownerRole       The database owner role
   * @param workerRole      The database worker role
   * @param roles           The roles
   */

  public EPQDatabaseConfiguration
  {
    Objects.requireNonNull(saxParsers, "saxParsers");
    Objects.requireNonNull(telemetry, "telemetry");
    Objects.requireNonNull(create, "create");
    Objects.requireNonNull(upgrade, "upgrade");
    Objects.requireNonNull(databaseAddress, "databaseAddress");
    Objects.requireNonNull(databaseName, "databaseName");
    Objects.requireNonNull(ownerRole, "ownerRole");
    Objects.requireNonNull(workerRole, "workerRole");
    Objects.requireNonNull(roles, "roles");

    roles.get(ownerRole.userName());
    roles.get(workerRole.userName());
  }
}
