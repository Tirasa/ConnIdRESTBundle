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
package net.tirasa.connid.bundles.rest;

import jakarta.ws.rs.core.MediaType;
import net.tirasa.connid.commons.scripted.AbstractScriptedConfiguration;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.spi.ConfigurationProperty;

public class RESTConfiguration extends AbstractScriptedConfiguration {

    private String baseAddress;

    @ConfigurationProperty(displayMessageKey = "baseAddress.display",
            helpMessageKey = "baseAddress.help", order = -3, required = true)
    public String getBaseAddress() {
        return baseAddress;
    }

    public void setBaseAddress(String baseAddress) {
        this.baseAddress = baseAddress;
    }

    private String accept = MediaType.APPLICATION_JSON;

    @ConfigurationProperty(displayMessageKey = "accept.display",
            helpMessageKey = "accept.help", order = -2, required = true)
    public String getAccept() {
        return accept;
    }

    public void setAccept(final String accept) {
        this.accept = accept;
    }

    private String contentType = MediaType.APPLICATION_JSON;

    @ConfigurationProperty(displayMessageKey = "contentType.display",
            helpMessageKey = "contentType.help", order = -1, required = true)
    public String getContentType() {
        return contentType;
    }

    public void setContentType(final String contentType) {
        this.contentType = contentType;
    }

    private String username;

    @ConfigurationProperty(displayMessageKey = "username.display",
            helpMessageKey = "username.help", order = 0)
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    private GuardedString password;

    @ConfigurationProperty(displayMessageKey = "password.display",
            helpMessageKey = "password.help", order = 1, confidential = true)
    public GuardedString getPassword() {
        return password;
    }

    public void setPassword(GuardedString password) {
        this.password = password;
    }

    private String clientId;

    @ConfigurationProperty(displayMessageKey = "clientId.display",
            helpMessageKey = "clientId.help", order = 2)
    public String getClientId() {
        return clientId;
    }

    public void setClientId(final String clientId) {
        this.clientId = clientId;
    }

    private String clientSecret;

    @ConfigurationProperty(displayMessageKey = "clientSecret.display",
            helpMessageKey = "clientSecret.help", order = 3)
    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(final String clientSecret) {
        this.clientSecret = clientSecret;
    }

    private String accessTokenNodeId;

    @ConfigurationProperty(displayMessageKey = "accessTokenNodeId.display",
            helpMessageKey = "accessTokenNodeId.help", order = 4)
    public String getAccessTokenNodeId() {
        return accessTokenNodeId;
    }

    public void setAccessTokenNodeId(final String accessTokenNodeId) {
        this.accessTokenNodeId = accessTokenNodeId;
    }

    private String accessTokenBaseAddress;

    @ConfigurationProperty(displayMessageKey = "accessTokenBaseAddress.display",
            helpMessageKey = "accessTokenBaseAddress.help", order = 5)
    public String getAccessTokenBaseAddress() {
        return accessTokenBaseAddress;
    }

    public void setAccessTokenBaseAddress(final String accessTokenBaseAddress) {
        this.accessTokenBaseAddress = accessTokenBaseAddress;
    }

    private String accessTokenContentType = MediaType.APPLICATION_FORM_URLENCODED;

    @ConfigurationProperty(displayMessageKey = "accessTokenContentType.display",
            helpMessageKey = "accessTokenContentType.help", order = 6)
    public String getAccessTokenContentType() {
        return accessTokenContentType;
    }

    public void setAccessTokenContentType(final String accessTokenContentType) {
        this.accessTokenContentType = accessTokenContentType;
    }

    @Override
    public void validate() {
        LOG.info("Validate " + getClass().getName());

        super.validate();

        LOG.ok("Configuration is valid");
    }
}
