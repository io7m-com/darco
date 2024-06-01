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

import freemarker.template.TemplateException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

/**
 * Functions to write data from templates.
 */

public final class DTemplater
{
  private DTemplater()
  {

  }

  /**
   * Generate files for the given template parameters.
   *
   * @param parameters      The parameters
   * @param outputDirectory The output directory
   *
   * @throws TemplateException On errors
   * @throws IOException       On errors
   */

  public static void writeSourceFiles(
    final DTemplateParameters parameters,
    final Path outputDirectory)
    throws TemplateException, IOException
  {
    Objects.requireNonNull(parameters, "parameters");
    Objects.requireNonNull(outputDirectory, "outputDirectory");

    final var templates =
      DTemplateService.create();

    write(
      parameters,
      templates.template("Configuration"),
      outputDirectory.resolve(parameters.configuration() + ".java")
    );
    write(
      parameters,
      templates.template("ConfigurationType"),
      outputDirectory.resolve(parameters.configurationType() + ".java")
    );
    write(
      parameters,
      templates.template("Connection"),
      outputDirectory.resolve(parameters.connection() + ".java")
    );
    write(
      parameters,
      templates.template("ConnectionType"),
      outputDirectory.resolve(parameters.connectionType() + ".java")
    );
    write(
      parameters,
      templates.template("Database"),
      outputDirectory.resolve(parameters.database() + ".java")
    );
    write(
      parameters,
      templates.template("DatabaseFactoryType"),
      outputDirectory.resolve(parameters.databaseFactoryType() + ".java")
    );
    write(
      parameters,
      templates.template("DatabaseType"),
      outputDirectory.resolve(parameters.databaseType() + ".java")
    );
    write(
      parameters,
      templates.template("Transaction"),
      outputDirectory.resolve(parameters.transaction() + ".java")
    );
    write(
      parameters,
      templates.template("TransactionType"),
      outputDirectory.resolve(parameters.transactionType() + ".java")
    );
    write(
      parameters,
      templates.template("QueryAbstract"),
      outputDirectory.resolve(parameters.queryAbstract() + ".java")
    );
    write(
      parameters,
      templates.template("QueryProvider"),
      outputDirectory.resolve(parameters.queryProvider() + ".java")
    );
    write(
      parameters,
      templates.template("QueryProviderType"),
      outputDirectory.resolve(parameters.queryProviderType() + ".java")
    );
  }

  private static void write(
    final DTemplateParameters parameters,
    final DTemplateType<DTemplateParameters> template,
    final Path file)
    throws TemplateException, IOException
  {
    try (var output =
           Files.newBufferedWriter(file, UTF_8, CREATE, TRUNCATE_EXISTING)) {
      template.process(parameters, output);
      output.flush();
    }
  }
}
