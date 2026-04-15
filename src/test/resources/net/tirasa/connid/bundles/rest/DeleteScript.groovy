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

import jakarta.ws.rs.core.Response
import org.apache.cxf.jaxrs.client.WebClient

// Parameters:
// The connector sends the following:
// client : CXF WebClient
// action: a string describing the action ("DELETE" here)
// log: a handler to the Log facility
// objectClass: a String describing the Object class (__ACCOUNT__ / __GROUP__ / other)
// options: a handler to the OperationOptions Map
// uid: String for the unique id that specifies the object to delete
// accessToken: access token for connection instance
// extendedAttributes: a <String, Object> Map, containing custom attributes

log.info("Entering " + action + " Script")

WebClient webClient = client

assert uid != null

switch (objectClass) {
    case "__ACCOUNT__":

        webClient.path("/users/" + uid)
        log.info("DELETE PATH: {0}", webClient.getCurrentURI())
        webClient.header("X-Api-Token", accessToken)
        Response response = webClient.delete()
        log.info("DELETE RESPONSE: {0}", response)
        if (response.getStatus() != 204) {
            throw new RuntimeException("Could not delete user: " + uid)
        }

        break

    default:
        break
}
