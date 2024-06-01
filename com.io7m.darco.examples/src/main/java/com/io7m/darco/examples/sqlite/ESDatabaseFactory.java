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


package com.io7m.darco.examples.sqlite;

import com.io7m.darco.api.DDatabaseException;
import com.io7m.darco.sqlite.DSDatabaseFactory;
import com.io7m.jmulticlose.core.CloseableCollectionType;
import com.io7m.lanark.core.RDottedName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;

import java.io.InputStream;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

/**
 * The main database factory.
 */

public final class ESDatabaseFactory
  extends DSDatabaseFactory<
  ESDatabaseConfiguration,
  ESDatabaseConnectionType,
  ESDatabaseTransactionType,
  ESDatabaseQueryProviderType<?, ?, ?>,
  ESDatabaseType>
  implements ESDatabaseFactoryType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(ESDatabaseFactory.class);

  /**
   * The main database factory.
   */

  public ESDatabaseFactory()
  {

  }

  @Override
  protected RDottedName applicationId()
  {
    return new RDottedName("com.io7m.darco.examples.sqlite");
  }

  @Override
  protected Logger logger()
  {
    return LOG;
  }

  @Override
  protected ESDatabaseType onCreateDatabase(
    final ESDatabaseConfiguration configuration,
    final SQLiteDataSource source,
    final List<ESDatabaseQueryProviderType<?, ?, ?>> queryProviders,
    final CloseableCollectionType<DDatabaseException> resources)
  {
    return new ESDatabase(
      configuration,
      source,
      queryProviders,
      resources
    );
  }

  @Override
  protected InputStream onRequireDatabaseSchemaXML()
  {
    return ESDatabaseFactory.class.getResourceAsStream(
      "/com/io7m/darco/examples/sqlite/database.xml"
    );
  }

  @Override
  protected void onEvent(
    final String message)
  {

  }

  @Override
  protected void onAdjustSQLiteConfig(
    final SQLiteConfig config)
  {

  }

  @Override
  protected List<ESDatabaseQueryProviderType<?, ?, ?>> onRequireDatabaseQueryProviders()
  {
    return ServiceLoader.load(ESDatabaseQueryProviderType.class)
      .stream()
      .map(ServiceLoader.Provider::get)
      .map(x -> (ESDatabaseQueryProviderType<?, ?, ?>) x)
      .collect(Collectors.toList());
  }
}
