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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import net.tirasa.connid.commons.scripted.AbstractScriptedConnector;
import net.tirasa.connid.commons.scripted.Constants;
import org.apache.cxf.jaxrs.client.WebClient;
import org.identityconnectors.common.StringUtil;
import org.identityconnectors.common.script.ScriptExecutor;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.common.security.SecurityUtil;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.filter.FilterTranslator;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.ConnectorClass;
import org.identityconnectors.framework.spi.PoolableConnector;

@ConnectorClass(configurationClass = RESTConfiguration.class, displayNameKey = "rest.connector.display")
public class RESTConnector extends AbstractScriptedConnector<RESTConfiguration> implements PoolableConnector {

    private ScriptExecutor connectionInitExecutor;

    private ScriptExecutor healthCheckExecutor;

    private ScriptExecutor extendedAttributesExecutor;

    protected WebClient client;

    protected String accessToken;

    @Override
    public void init(final Configuration cfg) {
        super.init(cfg);

        this.client = WebClient.create(config.getBaseAddress())
                .accept(config.getAccept())
                .type(config.getContentType());

        if (!hasConnectionInitScript()) {
            if (StringUtil.isNotBlank(config.getClientId())
                    && StringUtil.isNotBlank(config.getClientSecret())
                    && StringUtil.isNotBlank(config.getAccessTokenBaseAddress())
                    && StringUtil.isNotBlank(config.getAccessTokenNodeId())) {

                this.client = WebClient.create(config.getBaseAddress())
                        .accept(config.getAccept())
                        .type(config.getContentType());
                this.client.header(HttpHeaders.AUTHORIZATION, "Bearer " + generateToken());
            } else {
                this.client = WebClient.create(config.getBaseAddress(),
                                null,
                                config.getUsername(),
                                config.getPassword() == null ? null : SecurityUtil.decrypt(config.getPassword()),
                                null)
                        .accept(config.getAccept())
                        .type(config.getContentType());
            }
        } else {
            this.accessToken = connectionInit(config.getUsername(), config.getPassword());
        }
    }

    protected String generateToken() {
        WebClient webClient = WebClient
                .create(config.getAccessTokenBaseAddress())
                .type(config.getAccessTokenContentType())
                .accept(config.getAccept());

        String contentUri = new StringBuilder("&client_id=")
                .append(config.getClientId())
                .append("&client_secret=")
                .append(config.getClientSecret())
                .append("&username=")
                .append(config.getUsername())
                .append("&password=")
                .append(SecurityUtil.decrypt(config.getPassword()))
                .toString();
        String token = null;
        try {
            Response response = webClient.post(contentUri);
            String responseAsString = response.readEntity(String.class);
            JsonNode result = new ObjectMapper().readTree(responseAsString);
            if (result == null || !result.hasNonNull(config.getAccessTokenNodeId())) {
                throw new ConnectorException("No access token found - " + responseAsString);
            }
            token = result.get(config.getAccessTokenNodeId()).textValue();
        } catch (Exception ex) {
            throw new ConnectorException("While obtaining authentication token", ex);
        }

        return token;
    }

    @Override
    protected Map<String, Object> buildArguments() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("configuration", config);
        arguments.put("client", WebClient.fromClient(client, true));

        if (hasConnectionInitScript()) {
            arguments.put("accessToken", accessToken);
        } else {
            arguments.put("accessToken", StringUtil.EMPTY);
        }

        if (hasExtendedAttributesScript()) {
            arguments.put("extendedAttributes", extendedAttributes());
        } else {
            arguments.put("extendedAttributes", Map.of());
        }

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

    @Override
    public void dispose() {
        if (client == null) {
            LOG.ok("Dispose for poolable connection called but client is null, no operation executed");
        } else {
            client.close();
        }
    }

    @Override
    public void checkAlive() {
        try {
            if (client != null && hasHealthCheckScript()) {
                try (Response response = healthCheck()) {
                    if (response.getStatusInfo().getFamily() == Response.Status.Family.CLIENT_ERROR
                            || response.getStatusInfo().getFamily() == Response.Status.Family.SERVER_ERROR) {
                        throw new IllegalStateException(
                                "Health checking connection results into the invalid response " + response);
                    }
                    LOG.ok("Health checking elaboration: connection is valid");
                }
            }

            LOG.warn("Health check script not defined, health check skipped");
        } catch (Exception ex) {
            throw ConnectorException.wrap(ex);
        }
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
            throw new UnsupportedOperationException(config.getMessage(Constants.MSG_INVALID_SCRIPT));
        }
    }

    private Response healthCheck() {
        if (checkReloadScript(healthCheckExecutor,
                config.getHealthCheckScript(), config.getHealthCheckScriptFileName())) {
            healthCheckExecutor = getScriptExecutor(
                    config.getHealthCheckScript(), config.getHealthCheckScriptFileName());
            LOG.ok("Health Check script loaded");
        }
        if (healthCheckExecutor != null) {
            Map<String, Object> arguments = buildArguments();

            arguments.put("action", "HEALTH CHECK");
            arguments.put("log", LOG);

            try {
                Object response = healthCheckExecutor.execute(arguments);
                if (response instanceof Response) {
                    return (Response) response;
                }
            } catch (Exception e) {
                throw new ConnectorException("Health Check script error", e);
            }
            throw new ConnectorException("Health Check script didn't return with the Response value");
        } else {
            throw new UnsupportedOperationException(config.getMessage(Constants.MSG_INVALID_SCRIPT));
        }
    }

    private Map<String, Object> extendedAttributes() {
        if (checkReloadScript(extendedAttributesExecutor,
                config.getExtendedAttributesScript(), config.getExtendedAttributesScriptFileName())) {
            extendedAttributesExecutor = getScriptExecutor(
                    config.getExtendedAttributesScript(), config.getExtendedAttributesScriptFileName());
            LOG.ok("Extended Attributes script loaded");
        }
        if (extendedAttributesExecutor != null) {
            Map<String, Object> arguments = new HashMap<>();

            arguments.put("action", "EXTENDED ATTRIBUTES");
            arguments.put("log", LOG);

            try {
                Object response = extendedAttributesExecutor.execute(arguments);
                if (response instanceof Map<?,?>) {

                    LOG.ok("Extended Attributes configuration is valid");
                    return (Map<String, Object>) response;
                }
            } catch (Exception e) {
                throw new ConnectorException("Extended Attributes script error", e);
            }
            throw new ConnectorException("Extended Attributes script didn't return with the Map value");
        } else {
            return Map.of();
        }
    }

    private boolean hasConnectionInitScript() {
        return StringUtil.isNotBlank(config.getConnectionInitScript())
                || StringUtil.isNotBlank(config.getConnectionInitScriptFileName());
    }

    private boolean hasHealthCheckScript() {
        return StringUtil.isNotBlank(config.getHealthCheckScript())
                || StringUtil.isNotBlank(config.getHealthCheckScriptFileName());
    }

    private boolean hasExtendedAttributesScript() {
        return StringUtil.isNotBlank(config.getExtendedAttributesScript())
                || StringUtil.isNotBlank(config.getExtendedAttributesScriptFileName());
    }
}
