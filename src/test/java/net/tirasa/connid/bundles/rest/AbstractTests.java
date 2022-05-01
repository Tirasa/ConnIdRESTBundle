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

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import java.util.ArrayList;
import java.util.List;
import net.tirasa.connid.bundles.rest.service.InMemoryUserService;
import net.tirasa.connid.bundles.rest.service.UserService;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;

public class AbstractTests {

    public static final String BASE_ADDRESS = "local://service";

    private static Server SERVER;

    @BeforeAll
    public static void startServer() throws Exception {
        JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();
        sf.setResourceClasses(UserService.class);

        List<Object> providers = new ArrayList<>();
        providers.add(new JacksonJaxbJsonProvider());
        sf.setProviders(providers);

        sf.setResourceProvider(UserService.class, new SingletonResourceProvider(new InMemoryUserService(), true));
        sf.setAddress(BASE_ADDRESS);

        SERVER = sf.create();
    }

    @AfterAll
    public static void stopServer() throws Exception {
        SERVER.stop();
        SERVER.destroy();
    }

    @AfterEach
    public void clear() {
        UserService userService = JAXRSClientFactory.create(BASE_ADDRESS, UserService.class);
        userService.clear();
    }
}
