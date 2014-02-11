/**
 * Copyright (C) 2013 all@code-story.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */
package net.codestory.http.filters.basic;

import static net.codestory.http.constants.Headers.*;

import java.io.*;
import java.util.*;

import net.codestory.http.filters.*;
import net.codestory.http.internal.*;
import net.codestory.http.payload.*;

public class BasicAuthFilter implements Filter {
  private final String uriPrefix;
  private final String realm;
  private final Map<String, String> usersPerHash;

  public BasicAuthFilter(String uriPrefix, String realm, Map<String, String> users) {
    this.uriPrefix = uriPrefix;
    this.realm = realm;
    this.usersPerHash = new HashMap<>();

    users.entrySet().forEach(entry -> {
      String user = entry.getKey();
      String password = entry.getValue();
      String hash = Base64.getEncoder().encodeToString((user + ":" + password).getBytes());

      usersPerHash.put("Basic " + hash, user);
    });
  }

  @Override
  public Payload apply(String uri, Context context, PayloadSupplier nextFilter) throws IOException {
    if (!uri.startsWith(uriPrefix)) {
      return nextFilter.get(); // Ignore
    }

    String authorizationHeader = context.getHeader(AUTHORIZATION);
    if (authorizationHeader == null) {
      return Payload.unauthorized(realm);
    }

    String user = usersPerHash.get(authorizationHeader.trim());
    if (user == null) {
      return Payload.unauthorized(realm);
    }

    context.setCurrentUser(user);

    return nextFilter.get();
  }
}
