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
package net.tirasa.connid.bundles.rest.api;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import net.tirasa.connid.bundles.rest.model.User;
import net.tirasa.connid.bundles.rest.security.Secured;

@Path("users")
@Consumes("application/json")
@Produces("application/json")
public interface UserService {

    @GET
    @Secured
    List<User> list();

    @GET
    @Path("{key}")
    @Secured
    User read(@PathParam("key") String key);

    @POST
    @Secured
    void create(User user);

    @PUT
    @Path("{key}")
    @Secured
    void update(@PathParam("key") String key, User user);

    @DELETE
    @Path("{key}")
    @Secured
    void delete(@PathParam("key") String key);

    @POST
    @Path("authenticate")
    User authenticate(@QueryParam("username") String username, @QueryParam("password") String password);

    @POST
    @Path("oauth/token")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON) Response oAuth(
            @FormParam("grant_type") String grantType,
            @FormParam("client_id") String clientId,
            @FormParam("client_secret") String clientSecret);

    @POST
    @Path("clear")
    void clear();
}
