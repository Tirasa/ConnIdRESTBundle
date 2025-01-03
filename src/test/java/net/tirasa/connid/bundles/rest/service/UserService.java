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

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import java.util.List;

@Path("users")
public interface UserService {

    @GET
    List<User> list();

    @GET
    @Path("{key}")
    User read(@PathParam("key") String key);

    @POST
    void create(User user);

    @PUT
    @Path("{key}")
    void update(@PathParam("key") String key, User user);

    @DELETE
    @Path("{key}")
    void delete(@PathParam("key") String key);

    @POST
    @Path("authenticate")
    User authenticate(@QueryParam("username") String username, @QueryParam("password") String password);

    @POST
    @Path("clear")
    void clear();
}
