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
import com.fasterxml.jackson.databind.ObjectMapper
import net.tirasa.connid.bundles.rest.service.User
import org.apache.cxf.jaxrs.client.WebClient

// Parameters:
// The connector sends us the following:
// client : CXF WebClient
//
// action: String correponding to the action (UPDATE/ADD_ATTRIBUTE_VALUES/REMOVE_ATTRIBUTE_VALUES)
//   - UPDATE : For each input attribute, replace all of the current values of that attribute
//     in the target object with the values of that attribute.
//   - ADD_ATTRIBUTE_VALUES: For each attribute that the input set contains, add to the current values
//     of that attribute in the target object all of the values of that attribute in the input set.
//   - REMOVE_ATTRIBUTE_VALUES: For each attribute that the input set contains, remove from the current values
//     of that attribute in the target object any value that matches one of the values of the attribute from the input set.

// log: a handler to the Log facility
//
// objectClass: a String describing the Object class (__ACCOUNT__ / __GROUP__ / other)
//
// uid: a String representing the entry uid
//
// attributes: an Attribute Map, containg the <String> attribute name as a key
// and the <List> attribute value(s) as value.
//
// password: password string, clear text (only for UPDATE)
//
// options: a handler to the OperationOptions Map

log.info("Entering " + action + " Script");

WebClient webClient = client;
ObjectMapper mapper = new ObjectMapper();

assert uid != null

switch (action) {
case "UPDATE":
  switch (objectClass) {  
  case "__ACCOUNT__":
    User user = new User();
    user.setKey(new UUID(uid.getBytes()));
    if (attributes.containsKey("__NAME__")) {
      user.setUsername(attributes.get("__NAME__").get(0));
    }
    if (attributes.containsKey("username")) {
      user.setUsername(attributes.get("username").get(0));
    }
    if (password != null) {
      user.setPassword(password);
    }
    if (attributes.containsKey("firstName")) {
      user.setFirstName(attributes.get("firstName").get(0));
    }
    if (attributes.containsKey("surname")) {
      user.setSurname(attributes.get("surname").get(0));
    }
    if (attributes.containsKey("email")) {
      user.setEmail(attributes.get("email").get(0));
    }
    
    String payload = mapper.writeValueAsString(user);

    // this if update works with PUT
    webClient.path("users").path(uid);
    webClient.put(payload);
    
    // this instead if update works with PATCH
    //webClient.path("users").path(uid);
    //WebClient.getConfig(webClient).getRequestContext().put("use.async.http.conduit", true);
    //webClient.invoke("PATCH", payload);

  default:
    break
  }

  return uid;
  break

case "ADD_ATTRIBUTE_VALUES":
  break


default:
  break
}