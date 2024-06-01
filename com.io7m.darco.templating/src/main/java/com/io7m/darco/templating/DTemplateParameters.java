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


package com.io7m.darco.templating;

import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * Template parameters.
 *
 * @param configuration       The configuration class
 * @param configurationType   The configuration interface
 * @param copyrightText       The copyright header
 * @param packageName         The package containing generated classes
 * @param connection          The concrete connection class
 * @param connectionType      The connection interface type
 * @param transaction         The transaction class
 * @param transactionType     The transaction interface type
 * @param queryAbstract       The abstract query base
 * @param queryProvider       The query provider class
 * @param queryProviderType   The query provider interface type
 * @param database            The database class
 * @param databaseFactoryType The database factory interface type
 * @param databaseType        The database type
 */

public record DTemplateParameters(
  String configuration,
  String configurationType,
  String copyrightText,
  String packageName,
  String connection,
  String connectionType,
  String transaction,
  String transactionType,
  String queryAbstract,
  String queryProvider,
  String queryProviderType,
  String database,
  String databaseFactoryType,
  String databaseType)
  implements DTemplateDataModelType
{
  /**
   * Template parameters.
   *
   * @param configuration       The configuration class
   * @param configurationType   The configuration interface
   * @param copyrightText       The copyright header
   * @param packageName         The package containing generated classes
   * @param connection          The concrete connection class
   * @param connectionType      The connection interface type
   * @param transaction         The transaction class
   * @param transactionType     The transaction interface type
   * @param queryAbstract       The abstract query base
   * @param queryProvider       The query provider class
   * @param queryProviderType   The query provider interface type
   * @param database            The database class
   * @param databaseFactoryType The database factory interface type
   * @param databaseType        The database type
   */

  public DTemplateParameters
  {
    Objects.requireNonNull(configuration, "configuration");
    Objects.requireNonNull(configurationType, "configurationType");
    Objects.requireNonNull(copyrightText, "copyrightText");
    Objects.requireNonNull(packageName, "packageName");
    Objects.requireNonNull(connection, "connection");
    Objects.requireNonNull(connectionType, "connectionType");
    Objects.requireNonNull(transaction, "transaction");
    Objects.requireNonNull(transactionType, "transactionType");
    Objects.requireNonNull(queryAbstract, "queryAbstract");
    Objects.requireNonNull(queryProvider, "queryProvider");
    Objects.requireNonNull(queryProviderType, "queryProviderType");
    Objects.requireNonNull(database, "database");
    Objects.requireNonNull(databaseFactoryType, "databaseFactoryType");
    Objects.requireNonNull(databaseType, "databaseType");
  }

  @Override
  public Map<String, Object> toTemplateHash()
  {
    return Map.ofEntries(
      Map.entry("configuration", this.configuration),
      Map.entry("configurationType", this.configurationType),
      Map.entry("connection", this.connection),
      Map.entry("connectionType", this.connectionType),
      Map.entry("copyrightText", this.copyrightText),
      Map.entry("database", this.database),
      Map.entry("databaseFactoryType", this.databaseFactoryType),
      Map.entry("databaseType", this.databaseType),
      Map.entry("packageName", this.packageName),
      Map.entry("queryAbstract", this.queryAbstract),
      Map.entry("queryProvider", this.queryProvider),
      Map.entry("queryProviderType", this.queryProviderType),
      Map.entry("transaction", this.transaction),
      Map.entry("transactionType", this.transactionType)
    );
  }

  /**
   * Parse template parameters from properties.
   *
   * @param properties The properties
   *
   * @return The template parameters
   */

  public static DTemplateParameters ofProperties(
    final Properties properties)
  {
    Objects.requireNonNull(properties, "properties");

    return new DTemplateParameters(
      properties.getProperty("configuration"),
      properties.getProperty("configurationType"),
      properties.getProperty("copyrightText"),
      properties.getProperty("packageName"),
      properties.getProperty("connection"),
      properties.getProperty("connectionType"),
      properties.getProperty("transaction"),
      properties.getProperty("transactionType"),
      properties.getProperty("queryAbstract"),
      properties.getProperty("queryProvider"),
      properties.getProperty("queryProviderType"),
      properties.getProperty("database"),
      properties.getProperty("databaseFactoryType"),
      properties.getProperty("databaseType")
    );
  }
}
