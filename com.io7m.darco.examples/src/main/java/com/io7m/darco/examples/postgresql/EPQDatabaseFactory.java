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

import com.io7m.darco.api.DDatabaseException;
import com.io7m.darco.postgres.DPQDatabaseFactory;
import com.io7m.jmulticlose.core.CloseableCollectionType;
import com.io7m.lanark.core.RDottedName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

/**
 * The main database factory.
 */

public final class EPQDatabaseFactory
  extends DPQDatabaseFactory<
    EPQDatabaseConfiguration,
    EPQDatabaseConnectionType,
    EPQDatabaseTransactionType,
    EPQDatabaseQueryProviderType,
    EPQDatabaseType>
  implements EPQDatabaseFactoryType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(EPQDatabaseFactory.class);

  /**
   * The main database factory.
   */

  public EPQDatabaseFactory()
  {

  }

  @Override
  protected RDottedName applicationId()
  {
    return new RDottedName("com.io7m.darco.examples.postgresql");
  }

  @Override
  protected Logger logger()
  {
    return LOG;
  }

  @Override
  protected EPQDatabaseType onCreateDatabase(
    final EPQDatabaseConfiguration configuration,
    final DataSource source,
    final List<EPQDatabaseQueryProviderType> queryProviders,
    final CloseableCollectionType<DDatabaseException> resources)
  {
    return new EPQDatabase(
      configuration,
      source,
      queryProviders,
      resources
    );
  }

  @Override
  protected InputStream onRequireDatabaseSchemaXML()
  {
    return EPQDatabaseFactory.class.getResourceAsStream(
      "/com/io7m/darco/examples/postgresql/database.xml"
    );
  }

  @Override
  protected void onEvent(
    final String message)
  {

  }

  @Override
  protected DataSource onTransformDataSourceForSetup(
    final DataSource dataSource)
  {
    return dataSource;
  }

  @Override
  protected DataSource onTransformDataSourceForUse(
    final DataSource dataSource)
  {
    return dataSource;
  }

  @Override
  protected void onPostUpgrade(
    final EPQDatabaseConfiguration configuration,
    final Connection connection)
  {

  }

  @Override
  protected List<EPQDatabaseQueryProviderType> onRequireDatabaseQueryProviders()
  {
    return ServiceLoader.load(EPQDatabaseQueryProviderType.class)
      .stream()
      .map(ServiceLoader.Provider::get)
      .collect(Collectors.toList());
  }
}
