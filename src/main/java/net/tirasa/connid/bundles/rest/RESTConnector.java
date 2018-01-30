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

import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.HttpHeaders;
import net.tirasa.connid.commons.scripted.AbstractScriptedConnector;
import net.tirasa.connid.commons.scripted.Constants;
import org.apache.cxf.jaxrs.client.WebClient;
import org.identityconnectors.common.security.SecurityUtil;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.filter.FilterTranslator;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.ConnectorClass;

@ConnectorClass(configurationClass = RESTConfiguration.class, displayNameKey = "rest.connector.display")
public class RESTConnector extends AbstractScriptedConnector<RESTConfiguration> {

    private WebClient client;

    @Override
    public void init(final Configuration cfg) {
        this.config = (RESTConfiguration) cfg;

        this.client = WebClient.create(config.getBaseAddress(),
                null,
                config.getUsername(),
                config.getPassword() == null ? null : SecurityUtil.decrypt(config.getPassword()),
                null).
                accept(config.getAccept()).
                type(config.getContentType());
        if (config.getBearer() != null) {
            this.client.header(HttpHeaders.AUTHORIZATION, "Bearer " + config.getBearer());
        }

        super.init(cfg);
    }

    @Override
    protected Map<String, Object> buildArguments() {
        final Map<String, Object> arguments = new HashMap<String, Object>();
        arguments.put("configuration", config);
        arguments.put("client", client);
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

}
