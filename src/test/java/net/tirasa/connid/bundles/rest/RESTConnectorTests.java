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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.common.IOUtil;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.api.APIConfiguration;
import org.identityconnectors.framework.api.ConnectorFacade;
import org.identityconnectors.framework.api.ConnectorFacadeFactory;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.AttributeUtil;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.ObjectClassInfo;
import org.identityconnectors.framework.common.objects.OperationOptionsBuilder;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.common.objects.SyncDelta;
import org.identityconnectors.framework.common.objects.SyncResultsHandler;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.test.common.TestHelpers;
import org.junit.Test;

public class RESTConnectorTests extends AbstractTests {

    private RESTConfiguration newConfiguration() {
        RESTConfiguration conf = new RESTConfiguration();
        conf.setBaseAddress(BASE_ADDRESS);
        conf.setTestScript(IOUtil.getResourceAsString(getClass(), "/TestScript.groovy"));
        conf.setSchemaScript(IOUtil.getResourceAsString(getClass(), "/SchemaScript.groovy"));
        conf.setSearchScript(IOUtil.getResourceAsString(getClass(), "/SearchScript.groovy"));
        conf.setSyncScript(IOUtil.getResourceAsString(getClass(), "/SyncScript.groovy"));
        conf.setAuthenticateScript(IOUtil.getResourceAsString(getClass(), "/AuthenticateScript.groovy"));
        conf.setCreateScript(IOUtil.getResourceAsString(getClass(), "/CreateScript.groovy"));
        conf.setUpdateScript(IOUtil.getResourceAsString(getClass(), "/UpdateScript.groovy"));
        conf.setDeleteScript(IOUtil.getResourceAsString(getClass(), "/DeleteScript.groovy"));
        return conf;
    }

    private ConnectorFacade newFacade() {
        ConnectorFacadeFactory factory = ConnectorFacadeFactory.getInstance();
        APIConfiguration impl = TestHelpers.createTestConfiguration(RESTConnector.class, newConfiguration());
        impl.getResultsHandlerConfiguration().setFilteredResultsHandlerInValidationMode(true);
        return factory.newInstance(impl);
    }

    @Test
    public void test() {
        newFacade().test();
    }

    @Test
    public void schema() {
        Schema schema = newFacade().schema();
        assertNotNull(schema);
        assertEquals(1, schema.getObjectClassInfo().size());

        ObjectClassInfo oci = schema.getObjectClassInfo().iterator().next();
        assertNotNull(oci);
        assertEquals(ObjectClass.ACCOUNT_NAME, oci.getType());

        assertFalse(oci.getAttributeInfo().isEmpty());
    }

    private Set<Attribute> getUniqueSample() {
        String uuid = UUID.randomUUID().toString();

        Set<Attribute> attrs = new HashSet<Attribute>();
        attrs.add(new Name("username" + uuid));
        attrs.add(AttributeBuilder.buildPassword(uuid.toCharArray()));
        attrs.add(AttributeBuilder.build("firstName", CollectionUtil.newSet("firstName")));
        attrs.add(AttributeBuilder.build("surname", CollectionUtil.newSet("surname")));
        attrs.add(AttributeBuilder.build("email", CollectionUtil.newSet("email")));

        return attrs;
    }

    private Uid doCreate() {
        return newFacade().create(
                ObjectClass.ACCOUNT,
                getUniqueSample(),
                new OperationOptionsBuilder().build());
    }

    @Test
    public void create() {
        Uid created = doCreate();
        assertNotNull(created);
    }

    @Test
    public void update() {
        Uid created = doCreate();

        Set<Attribute> replaceAttributes = new HashSet<Attribute>();
        replaceAttributes.add(AttributeBuilder.build("email", CollectionUtil.newSet("updatedEmail")));

        Uid updated = newFacade().update(
                ObjectClass.ACCOUNT,
                created,
                replaceAttributes,
                new OperationOptionsBuilder().build());
        assertEquals(created, updated);

        ConnectorObject read = newFacade().getObject(
                ObjectClass.ACCOUNT, updated, new OperationOptionsBuilder().build());
        Attribute email = AttributeUtil.find("email", read.getAttributes());
        assertNotNull(email);
        assertEquals("updatedEmail", AttributeUtil.getStringValue(email));
    }

    @Test
    public void delete() {
        Uid created = doCreate();

        newFacade().delete(ObjectClass.ACCOUNT, created, new OperationOptionsBuilder().build());

        ConnectorObject read = newFacade().getObject(
                ObjectClass.ACCOUNT, created, new OperationOptionsBuilder().build());
        assertNull(read);
    }

    @Test
    public void getObject() {
        Uid created = doCreate();

        ConnectorObject read = newFacade().getObject(
                ObjectClass.ACCOUNT, created, new OperationOptionsBuilder().build());
        assertNotNull(read);
        assertEquals(created, read.getUid());
        assertFalse(read.getAttributes().isEmpty());
    }

    @Test
    public void search() {
        doCreate();
        doCreate();
        doCreate();

        final List<ConnectorObject> result = new ArrayList<ConnectorObject>();
        newFacade().search(ObjectClass.ACCOUNT,
                null,
                new ResultsHandler() {

            @Override
            public boolean handle(final ConnectorObject connectorObject) {
                result.add(connectorObject);
                return true;
            }
        }, new OperationOptionsBuilder().build());

        assertEquals(3, result.size());
    }

    @Test
    public void sync() {
        assertNotNull(newFacade().getLatestSyncToken(ObjectClass.ACCOUNT));

        doCreate();
        doCreate();

        final List<SyncDelta> result = new ArrayList<SyncDelta>();
        newFacade().sync(ObjectClass.ACCOUNT,
                null,
                new SyncResultsHandler() {

            @Override
            public boolean handle(final SyncDelta delta) {
                result.add(delta);
                return true;
            }

        }, new OperationOptionsBuilder().build());

        assertEquals(2, result.size());
    }

    @Test
    public void authenticate() {
        Uid created = doCreate();

        // first update to set test credentials
        Set<Attribute> replaceAttributes = new HashSet<Attribute>();
        replaceAttributes.add(new Name("username"));
        replaceAttributes.add(AttributeBuilder.buildPassword("password".toCharArray()));

        Uid updated = newFacade().update(
                ObjectClass.ACCOUNT,
                created,
                replaceAttributes,
                new OperationOptionsBuilder().build());
        assertEquals(created, updated);

        // then check for successful authentication
        Uid authenticated = newFacade().authenticate(
                ObjectClass.ACCOUNT,
                "username",
                new GuardedString("password".toCharArray()),
                new OperationOptionsBuilder().build());
        assertEquals(created, authenticated);

        // finally check for unsuccessful authentication
        try {
            newFacade().authenticate(
                    ObjectClass.ACCOUNT,
                    "username",
                    new GuardedString("password2".toCharArray()),
                    new OperationOptionsBuilder().build());
            fail();
        } catch (ConnectorException e) {
            assertTrue(e.getCause().getMessage().startsWith("Could not authenticate"));
        }
    }
}
