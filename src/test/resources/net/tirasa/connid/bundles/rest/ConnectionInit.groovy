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


import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import jakarta.ws.rs.core.Form
import jakarta.ws.rs.core.Response
import org.apache.cxf.jaxrs.client.WebClient

// Parameters:
// The connector sends us the following:
// client : CXF WebClient
// action: String corresponding to the action ("CONNECTION INIT" here)
// log: a handler to the Log facility
// username: username
// password: password string, clear text or GuardedString depending on configuration

log.info("Entering " + action + " Script")

WebClient webClient = client
ObjectMapper mapper = new ObjectMapper()

if(configuration.getAccessTokenBaseAddress() != null && configuration.getAccessTokenNodeId() != null) {
    Form form = new Form()
    form.param("grant_type", "client_credentials")
    form.param("client_id", username)
    form.param("client_secret", password)
    webClient.path(configuration.getAccessTokenBaseAddress())
    Response response = webClient.form(form)
    if (response.getStatus() == 200) {
        String responseAsString = response.readEntity(String.class)
        JsonNode result = new ObjectMapper().readTree(responseAsString)
        if (result == null || !result.hasNonNull(configuration.getAccessTokenNodeId())) {
            throw new RuntimeException("No access token found - " + responseAsString)
        }

        return result.get(configuration.getAccessTokenNodeId()).textValue()
    } else {
        throw new RuntimeException("Could not authenticate " + username)
    }
} else {
    webClient.path("/users/authenticate").query("username", username).query("password", password)
    Response response = webClient.post(null)
    if (response.getStatus() == 200) {
        ObjectNode node = mapper.readTree(response.getEntity())
        return node.get("key").textValue()
    } else {
        throw new RuntimeException("Could not authenticate " + username)
    }
}

