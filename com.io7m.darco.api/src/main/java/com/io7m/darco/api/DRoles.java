/*
 * Copyright © 2025 Mark Raynsford <code@io7m.com> https://www.io7m.com
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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * The set of roles known by a database configuration.
 *
 * @param roles The roles
 */

public record DRoles(
  Map<String, DUsernamePassword> roles)
{
  /**
   * @param input A list of roles
   *
   * @return A set of roles
   */

  public static DRoles of(
    final List<DUsernamePassword> input)
  {
    return new DRoles(
      input.stream()
        .map(e -> Map.entry(e.userName(), e))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
    );
  }

  /**
   * Get a role.
   *
   * @param name The role name
   *
   * @return The role credentials
   */

  public DUsernamePassword get(
    final String name)
  {
    Objects.requireNonNull(name, "name");

    final var userPass = this.roles.get(name);
    if (userPass == null) {
      throw new IllegalArgumentException(
        "Unknown role: %s".formatted(name)
      );
    }
    return userPass;
  }

  /**
   * The set of roles known by a database configuration.
   *
   * @param roles The roles
   */

  public DRoles
  {
    roles = Map.copyOf(roles);

    for (final var name : roles.keySet()) {
      final var gotName = roles.get(name).userName();
      if (!Objects.equals(name, gotName)) {
        throw new IllegalArgumentException(
          "Bad role map: Name '%s' mapped to role '%s'".formatted(name, gotName)
        );
      }
    }
  }
}
