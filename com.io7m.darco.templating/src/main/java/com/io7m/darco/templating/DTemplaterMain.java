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

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * The main template program.
 */

public final class DTemplaterMain
{
  private DTemplaterMain()
  {

  }

  /**
   * The main template program.
   *
   * @param args The command-line arguments
   *
   * @throws Exception On errors
   */

  public static void main(
    final String[] args)
    throws Exception
  {
    if (args.length != 2) {
      System.err.println("Usage: template-properties.conf output-directory");
      System.exit(1);
    }

    final var file =
      Paths.get(args[0]);
    final var outputDirectory =
      Paths.get(args[1]);
    final var properties =
      new Properties();

    try (var input = Files.newInputStream(file)) {
      properties.loadFromXML(input);
    }

    final var parameters =
      DTemplateParameters.ofProperties(properties);

    Files.createDirectories(outputDirectory);
    DTemplater.writeSourceFiles(parameters, outputDirectory);
  }
}

