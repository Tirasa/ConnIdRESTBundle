/**
 * Copyright (C) 2016 ConnId (connid-dev@googlegroups.com)
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
 * limitations under the License.
 */
package net.tirasa.connid.bundles.rest.service;

import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.tirasa.connid.bundles.rest.api.UserService;
import net.tirasa.connid.bundles.rest.model.User;
import net.tirasa.connid.bundles.rest.security.Secured;

public class InMemoryUserService implements UserService {

    private static final Map<String, User> USERS = new HashMap<>();

    @Override
    public List<User> list() {
        return new ArrayList<>(USERS.values());
    }

    @Override
    public User read(String key) {
        User user = USERS.get(key);
        if (user == null) {
            throw new NotFoundException(key);
        }
        return user;
    }

    @Override
    public void create(User user) {
        if (user.getKey() == null) {
            user.setKey(UUID.randomUUID().toString());
        }
        if (USERS.containsKey(user.getKey())) {
            throw new IllegalArgumentException("User already exists: " + user.getKey());
        }
        USERS.put(user.getKey(), user);
    }

    @Override
    public void update(String key, User updatedUser) {
        if (!USERS.containsKey(key)) {
            throw new NotFoundException(updatedUser.getKey());
        }
        User user = USERS.get(key);
        if (updatedUser.getUsername() != null) {
            user.setUsername(updatedUser.getUsername());
        }
        if (updatedUser.getPassword() != null) {
            user.setPassword(updatedUser.getPassword());
        }
        if (updatedUser.getFirstName() != null) {
            user.setFirstName(updatedUser.getFirstName());
        }
        if (updatedUser.getSurname() != null) {
            user.setSurname(updatedUser.getSurname());
        }
        if (updatedUser.getEmail() != null) {
            user.setEmail(updatedUser.getEmail());
        }
    }

    @Override
    public void delete(String key) {
        if (!USERS.containsKey(key)) {
            throw new NotFoundException(key.toString());
        }
        USERS.remove(key);
    }

    @Override
    public User authenticate(String username, String password) {
        User user = null;
        if ("admin".equals(username)) {
            User adminUser = new User();
            adminUser.setKey("admin");
            adminUser.setUsername("admin");
            adminUser.setPassword("password");
            adminUser.setFirstName("admin");
            adminUser.setSurname("admin");
            return adminUser;
        }

        for (User entry : USERS.values()) {
            if (username.equals(entry.getUsername())) {
                user = entry;
            }
        }
        if (user == null) {
            throw new NotFoundException(username);
        }
        if (!password.equals(user.getPassword())) {
            throw new ForbiddenException();
        }

        return user;
    }

    @Override
    public Response oAuth(String grantType, String clientId, String clientSecret) {
        if ("client_credentials".equals(grantType)
                && "admin".equals(clientId)
                && "password".equals(clientSecret)) {

            String token = "admin";

            return Response.ok(Map.of(
                    "access_token", token,
                    "token_type", "Bearer",
                    "expires_in", 3600
            )).build();
        }

        return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    @Override
    public void clear() {
        USERS.clear();
    }
}
