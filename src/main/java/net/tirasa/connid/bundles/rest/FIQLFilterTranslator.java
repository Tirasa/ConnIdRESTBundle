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

import java.util.Map;
import java.util.HashMap;
import org.apache.cxf.jaxrs.ext.search.ConditionType;
import org.identityconnectors.framework.common.objects.AttributeUtil;
import org.identityconnectors.common.StringUtil;
import org.identityconnectors.framework.common.objects.filter.AbstractFilterTranslator;
import org.identityconnectors.framework.common.objects.filter.AttributeFilter;
import org.identityconnectors.framework.common.objects.filter.ContainsFilter;
import org.identityconnectors.framework.common.objects.filter.EndsWithFilter;
import org.identityconnectors.framework.common.objects.filter.EqualsFilter;
import org.identityconnectors.framework.common.objects.filter.EqualsIgnoreCaseFilter;
import org.identityconnectors.framework.common.objects.filter.GreaterThanFilter;
import org.identityconnectors.framework.common.objects.filter.GreaterThanOrEqualFilter;
import org.identityconnectors.framework.common.objects.filter.LessThanFilter;
import org.identityconnectors.framework.common.objects.filter.LessThanOrEqualFilter;
import org.identityconnectors.framework.common.objects.filter.StartsWithFilter;

/**
 * This is an implementation of AbstractFilterTranslator that gives a concrete representation
 * of which filters can be applied at the connector level (via FIQL).
 * Note: The generic query type is most commonly a String, but does not have to be.
 */
public class FIQLFilterTranslator extends AbstractFilterTranslator<Map<String, Object>> {

    private Map<String, Object> createMap(ConditionType conditionType, AttributeFilter filter) {
        Map<String, Object> map = new HashMap<>();
        String name = filter.getAttribute().getName();
        String value = AttributeUtil.getAsStringValue(filter.getAttribute());
        if (StringUtil.isBlank(value)) {
            return null;
        } else {
            map.put("conditionType", conditionType);
            map.put("left", name);
            map.put("right", value);
            return map;
        }
    }

    @Override
    protected Map<String, Object> createContainsExpression(ContainsFilter filter, boolean not) {
        String value = AttributeUtil.getAsStringValue(filter.getAttribute());
        if (StringUtil.isBlank(value)) {
            return null;
        }
        filter.getAttribute().getValue().set(0, '*' + value + "*");
        return createMap(not ? ConditionType.NOT_EQUALS : ConditionType.EQUALS, filter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Map<String, Object> createEndsWithExpression(EndsWithFilter filter, boolean not) {
        String value = AttributeUtil.getAsStringValue(filter.getAttribute());
        if (StringUtil.isBlank(value)) {
            return null;
        }
        filter.getAttribute().getValue().set(0, "*" + value);
        return createMap(not ? ConditionType.NOT_EQUALS : ConditionType.EQUALS, filter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Map<String, Object> createStartsWithExpression(StartsWithFilter filter, boolean not) {
        String value = AttributeUtil.getAsStringValue(filter.getAttribute());
        if (StringUtil.isBlank(value)) {
            return null;
        }
        filter.getAttribute().getValue().set(0, value + "*");
        return createMap(not ? ConditionType.NOT_EQUALS : ConditionType.EQUALS, filter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Map<String, Object> createEqualsExpression(EqualsFilter filter, boolean not) {
        return createMap(not ? ConditionType.NOT_EQUALS : ConditionType.EQUALS, filter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Map<String, Object> createEqualsIgnoreCaseExpression(EqualsIgnoreCaseFilter filter, boolean not) {
        return createMap(not ? ConditionType.NOT_EQUALS : ConditionType.EQUALS, filter);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected Map<String, Object> createAndExpression(
            Map<String, Object> leftExpression, Map<String, Object> rightExpression) {

        Map<String, Object> map = new HashMap<>();
        map.put("conditionType", ConditionType.AND);
        map.put("left", leftExpression);
        map.put("right", rightExpression);
        return map;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Map<String, Object> createOrExpression(
            Map<String, Object> leftExpression, Map<String, Object> rightExpression) {

        Map<String, Object> map = new HashMap<>();
        map.put("conditionType", ConditionType.OR);
        map.put("left", leftExpression);
        map.put("right", rightExpression);
        return map;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Map<String, Object> createGreaterThanExpression(GreaterThanFilter filter, boolean not) {
        return createMap(not ? ConditionType.LESS_OR_EQUALS : ConditionType.GREATER_THAN, filter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Map<String, Object> createGreaterThanOrEqualExpression(GreaterThanOrEqualFilter filter, boolean not) {
        return createMap(not ? ConditionType.LESS_THAN : ConditionType.GREATER_OR_EQUALS, filter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Map<String, Object> createLessThanExpression(LessThanFilter filter, boolean not) {
        return createMap(not ? ConditionType.GREATER_OR_EQUALS : ConditionType.LESS_THAN, filter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Map<String, Object> createLessThanOrEqualExpression(LessThanOrEqualFilter filter, boolean not) {
        return createMap(not ? ConditionType.GREATER_THAN : ConditionType.LESS_OR_EQUALS, filter);
    }
}
