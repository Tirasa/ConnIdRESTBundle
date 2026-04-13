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

import static net.tirasa.connid.commons.scripted.Constants.MSG_INVALID_SCRIPT;

import java.util.HashMap;
import java.util.Map;
import net.tirasa.connid.commons.scripted.AbstractScriptedConnector;
import net.tirasa.connid.commons.scripted.Constants;
import org.apache.cxf.jaxrs.client.WebClient;
import org.identityconnectors.common.script.ScriptExecutor;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.common.security.SecurityUtil;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.filter.FilterTranslator;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.ConnectorClass;

@ConnectorClass(configurationClass = RESTConfiguration.class, displayNameKey = "rest.connector.display")
public class RESTConnector extends AbstractScriptedConnector<RESTConfiguration> {

    private ScriptExecutor connectionInitExecutor;

    protected WebClient client;

    @Override
    public void init(final Configuration cfg) {
        super.init(cfg);

        this.client = WebClient.create(config.getBaseAddress())
                .accept(config.getAccept())
                .type(config.getContentType());
    }

    @Override
    protected Map<String, Object> buildArguments() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("configuration", config);
        arguments.put("client", WebClient.fromClient(client, true));
        arguments.put("accessToken", connectionInit(config.getUsername(), config.getPassword()));
        return arguments;
    }

    @Override
    public FilterTranslator<Map<String, Object>> createFilterTranslator(
            final ObjectClass objectClass, final OperationOptions options) {

        if (objectClass == null) {
            throw new IllegalArgumentException(config.getMessage(Constants.MSG_OBJECT_CLASS_REQUIRED));
        }
        LOG.ok("ObjectClass: {0}", objectClass.getObjectClassValue());

        return new FIQLFilterTranslator();
    }

    private String connectionInit(
            final String username,
            final GuardedString password) {
        if (checkReloadScript(connectionInitExecutor,
                config.getConnectionInitScript(), config.getConnectionInitScriptFileName())) {
            connectionInitExecutor = getScriptExecutor(
                    config.getConnectionInitScript(), config.getConnectionInitScriptFileName());
            LOG.ok("Connection Init script loaded");
        }
        if (connectionInitExecutor != null) {
            Map<String, Object> arguments = new HashMap<>();

            arguments.put("action", "CONNECTION INIT");
            arguments.put("log", LOG);
            arguments.put("configuration", config);
            arguments.put("client", WebClient.fromClient(client, true));
            arguments.put("username", username);
            arguments.put("password",
                    config.getClearTextPasswordToScript() ? SecurityUtil.decrypt(password) : password);

            try {
                Object token = connectionInitExecutor.execute(arguments);
                if (token instanceof String) {
                    LOG.ok("{0} authenticated", token);
                    return token.toString();
                }
            } catch (Exception e) {
                throw new ConnectorException("Connection Init script error", e);
            }
            throw new ConnectorException("Connection Init script didn't return with the token value");
        } else {
            throw new UnsupportedOperationException(config.getMessage(MSG_INVALID_SCRIPT));
        }
    }
}
