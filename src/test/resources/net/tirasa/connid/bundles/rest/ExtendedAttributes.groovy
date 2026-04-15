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
import org.identityconnectors.common.security.GuardedString
import org.identityconnectors.framework.common.objects.AttributeInfo
import org.identityconnectors.framework.common.objects.AttributeInfo.Flags
import org.identityconnectors.framework.common.objects.AttributeInfoBuilder
import org.identityconnectors.framework.common.objects.ObjectClassInfo
import org.identityconnectors.framework.common.objects.ObjectClassInfoBuilder

// Parameters:
// The connector sends the following:
// action: a string describing the action ("EXTENDED ATTRIBUTES" here)
// log: a handler to the Log facility
//
//
// This script must return a Map<String, Object> containing custom attributes
// The returned map will be used as an extended context for subsequent connector operations
//
// Each entry in the map represents a custom attribute where:
// - key   -> attribute name (String)
// - value -> attribute value (any Object type supported by the connector)
//
// Example:
// return [
//     customAttr1: "value1",
//     customAttr2: 123,
//     flags: [
//         enabled: true
//     ]
// ]
//
// Important:
// - The script MUST return a Map<String, Object>.
// - This map will be injected into the runtime context


log.info("Entering " + action + " Script");

Map<String, Object> result = new HashMap<>()
result.put("test", "test2")

return result