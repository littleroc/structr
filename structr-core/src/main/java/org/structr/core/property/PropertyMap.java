/**
 * Copyright (C) 2010-2018 Structr GmbH
 *
 * This file is part of Structr <http://structr.org>.
 *
 * Structr is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Structr is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Structr.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.structr.core.property;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.structr.common.SecurityContext;
import org.structr.common.error.FrameworkException;
import org.structr.core.GraphObject;
import org.structr.core.app.StructrApp;
import org.structr.core.converter.PropertyConverter;
import org.structr.core.entity.AbstractNode;
import org.structr.core.graph.CreationContainer;
import org.structr.core.graph.Tx;
import org.structr.schema.SchemaHelper;

/**
 * A container for properties and their values that is used for input/output and database
 * conversion.
 *
 *
 */
public class PropertyMap {

	private static final Logger logger      = LoggerFactory.getLogger(PropertyMap.class.getName());
	private static final Map<String, String> CMIS_PROPERTY_MAPPING = new LinkedHashMap<>();

	static {

		CMIS_PROPERTY_MAPPING.put(PropertyIds.OBJECT_ID,      "id");
		CMIS_PROPERTY_MAPPING.put(PropertyIds.NAME,           "name");
		CMIS_PROPERTY_MAPPING.put(PropertyIds.OBJECT_TYPE_ID, "type");
	}

	protected Map<PropertyKey, Object> properties = new LinkedHashMap<>();

	public PropertyMap() {
	}

	public PropertyMap(final PropertyMap source) {

		putAll(source);

	}

	public <T> PropertyMap(final PropertyKey<T> key, final T value) {

		properties.put(key, value);

	}

	@Override
	public String toString() {
		return properties.toString();
	}

	public int size() {
		return properties.size();
	}

	public boolean isEmpty() {
		return properties.isEmpty();
	}

	public <T> boolean containsKey(PropertyKey<T> key) {
		return properties.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return properties.containsValue(value);
	}

	public <T> T get(PropertyKey<T> key) {
		return (T)properties.get(key);
	}

	public <T> T put(PropertyKey<T> key, T value) {
		return (T)properties.put(key, value);
	}

	public final void putAll(PropertyMap source) {

		if (source != null) {

			for (Entry<PropertyKey, Object> entry : source.entrySet()) {
				properties.put(entry.getKey(), entry.getValue());
			}

		}

	}

	public <T> T remove(PropertyKey<T> key) {
		return (T)properties.remove(key);
	}

	public void clear() {
		properties.clear();
	}

	public Set<PropertyKey> keySet() {
		return properties.keySet();
	}

	public Collection<Object> values() {
		return properties.values();
	}

	public Set<Entry<PropertyKey, Object>> entrySet() {
		return properties.entrySet();
	}

	public Map<PropertyKey, Object> getRawMap() {
		return properties;
	}

	/**
	 * Calculates a hash code for the contents of this PropertyMap.
	 *
	 * @param comparableKeys the set of property keys to use for hash code calculation, or null to use the whole keySet
	 * @param includeSystemProperties whether to include system properties in the calculation
	 * @return hash code
	 */
	public int contentHashCode(Set<PropertyKey> comparableKeys, boolean includeSystemProperties) {

		Map<PropertyKey, Object> sortedMap = new TreeMap<>(new PropertyKeyComparator());
		int hashCode                       = 42;

		sortedMap.putAll(properties);

		if (comparableKeys == null) {

			// calculate hash code for all properties in this map
			for (Entry<PropertyKey, Object> entry : sortedMap.entrySet()) {

				if (includeSystemProperties || !entry.getKey().isUnvalidated()) {

					hashCode ^= entry.hashCode();
				}
			}

		} else {

			for (Entry<PropertyKey, Object> entry : sortedMap.entrySet()) {

				PropertyKey key = entry.getKey();

				if (comparableKeys.contains(key)) {

					if (includeSystemProperties || !key.isUnvalidated()) {

						hashCode ^= entry.hashCode();
					}
				}
			}
		}


		return hashCode;
	}

	// ----- static methods -----
	public static PropertyMap javaTypeToDatabaseType(SecurityContext securityContext, GraphObject wrapped, Map<String, Object> source) throws FrameworkException {

		final PropertyMap resultMap = new PropertyMap();
		final GraphObject entity    = unwrap(wrapped);

		if (source != null) {

			for (Entry<String, Object> entry : source.entrySet()) {

				String key   = entry.getKey();
				Object value = entry.getValue();

				if (key != null) {

					final PropertyKey propertyKey     = StructrApp.getConfiguration().getPropertyKeyForDatabaseName(entity.getClass(), key);
					final PropertyConverter converter = propertyKey.databaseConverter(securityContext, entity);

					if (converter != null) {

						try {
							Object propertyValue = converter.convert(value);
							resultMap.put(propertyKey, propertyValue);

						} catch(ClassCastException cce) {

							throw new FrameworkException(422, "Invalid JSON input for key " + propertyKey.jsonName() + ", expected a JSON " + propertyKey.typeName() + ".");
						}

					} else {

						resultMap.put(propertyKey, value);
					}
				}
			}
		}

		return resultMap;
	}

	public static PropertyMap databaseTypeToJavaType(final SecurityContext securityContext, final GraphObject wrapped, final Map<String, Object> source) throws FrameworkException {

		final PropertyMap resultMap = new PropertyMap();
		final GraphObject entity    = unwrap(wrapped);
		final Class entityType      = entity.getClass();

		if (source != null) {

			for (Entry<String, Object> entry : source.entrySet()) {

				String key   = entry.getKey();
				Object value = entry.getValue();

				if (key != null) {

					final PropertyKey propertyKey     = StructrApp.getConfiguration().getPropertyKeyForDatabaseName(entityType, key);
					final PropertyConverter converter = propertyKey.databaseConverter(securityContext, entity);

					if (converter != null) {

						try {
							Object propertyValue = converter.revert(value);
							resultMap.put(propertyKey, propertyValue);

						} catch(ClassCastException cce) {

							throw new FrameworkException(422, "Invalid JSON input for key " + propertyKey.jsonName() + ", expected a JSON " + propertyKey.typeName() + ".");
						}

					} else {

						resultMap.put(propertyKey, value);
					}
				}
			}
		}

		return resultMap;
	}

	public static PropertyMap databaseTypeToJavaType(SecurityContext securityContext, Class<? extends GraphObject> entityType, Map<String, Object> source) throws FrameworkException {

		PropertyMap resultMap = new PropertyMap();

		if (source != null) {

			for (Entry<String, Object> entry : source.entrySet()) {

				String key   = entry.getKey();
				Object value = entry.getValue();

				if (key != null) {

					PropertyKey propertyKey     = StructrApp.getConfiguration().getPropertyKeyForDatabaseName(entityType, key);
					PropertyConverter converter = propertyKey.databaseConverter(securityContext);

					if (converter != null) {

						try {
							Object propertyValue = converter.revert(value);
							resultMap.put(propertyKey, propertyValue);

						} catch(ClassCastException cce) {

							throw new FrameworkException(422, "Invalid JSON input for key " + propertyKey.jsonName() + ", expected a JSON " + propertyKey.typeName() + ".");
						}

					} else {

						resultMap.put(propertyKey, value);
					}
				}
			}
		}

		return resultMap;
	}

	public static PropertyMap inputTypeToJavaType(SecurityContext securityContext, Map<String, Object> source) throws FrameworkException {

		if (source != null) {

			Object typeName = source.get(AbstractNode.type.jsonName());
			if (typeName != null) {

				Class<? extends GraphObject> type = SchemaHelper.getEntityClassForRawType(typeName.toString());
				if (type != null) {

					return inputTypeToJavaType(securityContext, type, source);

				} else {

					logger.warn("No entity type found for raw type {}", typeName);
				}

			} else {

				logger.warn("No entity type found in source map: {}", source);
			}
		}

		return fallbackPropertyMap(source);
	}

	public static PropertyMap inputTypeToJavaType(final SecurityContext securityContext, Class<? extends GraphObject> entity, final Map<String, Object> source) throws FrameworkException {

		final String batchType = securityContext.getAttribute("batchType", "__");
		if (batchType.equals(source.get("type"))) {

			// only to batching if a type is set for which batch is enable
			final Integer count   = securityContext.getAttribute("objectCount", 0);
			final Integer overall = securityContext.getAttribute("overallCount", 0);

			securityContext.setAttribute("objectCount",  count   + 1);
			securityContext.setAttribute("overallCount", overall + 1);

			if (count == 100) {

				final Tx tx = (Tx)securityContext.getAttribute("currentTransaction");
				if (tx != null) {

					logger.info("Committing batch transaction after {} objects of type {}.", overall, batchType);

					// try to commit this batch
					tx.success();
					tx.close();

					// open new transaction and store it in context
					securityContext.setAttribute("currentTransaction", StructrApp.getInstance(securityContext).tx());
					securityContext.setAttribute("objectCount",        0);
				}
			}
		}

		PropertyMap resultMap = new PropertyMap();
		if (source != null) {

			// caution, source can be null when an empty nested property group is encountered!
			for (final Entry<String, Object> entry : source.entrySet()) {

				String key   = entry.getKey();
				Object value = entry.getValue();

				if (key != null) {

					final PropertyKey propertyKey = StructrApp.getConfiguration().getPropertyKeyForJSONName(entity, key);
					if (propertyKey != null) {

						final PropertyConverter converter = propertyKey.inputConverter(securityContext);

						if (converter != null && value != null && !propertyKey.valueType().isAssignableFrom(value.getClass())) {

							try {

								// test
								converter.setContext(source);

								Object propertyValue = converter.convert(value);
								resultMap.put(propertyKey, propertyValue);

							} catch (ClassCastException cce) {

								logger.warn("", cce);

								throw new FrameworkException(422, "Invalid JSON input for key " + propertyKey.jsonName() + ", expected a JSON " + propertyKey.typeName() + ".");
							}

						} else {

							resultMap.put(propertyKey, value);
						}
					}
				}
			}
		}

		return resultMap;
	}

	public static Map<String, Object> javaTypeToDatabaseType(SecurityContext securityContext, Class<? extends GraphObject> entity, PropertyMap properties) throws FrameworkException {

		Map<String, Object> databaseTypedProperties = new LinkedHashMap<>();

		for(Entry<PropertyKey, Object> entry : properties.entrySet()) {

			PropertyKey propertyKey     = entry.getKey();
			PropertyConverter converter = propertyKey.databaseConverter(securityContext);

			if (converter != null) {

				try {
					Object propertyValue = converter.convert(entry.getValue());
					databaseTypedProperties.put(propertyKey.jsonName(), propertyValue);

				} catch(ClassCastException cce) {

					throw new FrameworkException(422, "Invalid JSON input for key " + propertyKey.jsonName() + ", expected a JSON " + propertyKey.typeName() + ".");
				}

			} else {

				databaseTypedProperties.put(propertyKey.jsonName(), entry.getValue());
			}
		}

		return databaseTypedProperties;
	}

	public static Map<String, Object> javaTypeToInputType(SecurityContext securityContext, Class<? extends GraphObject> entity, PropertyMap properties) throws FrameworkException {

		Map<String, Object> inputTypedProperties = new LinkedHashMap<>();

		for(Entry<PropertyKey, Object> entry : properties.entrySet()) {

			PropertyKey propertyKey     = entry.getKey();
			PropertyConverter converter = propertyKey.inputConverter(securityContext);

			if (converter != null) {

				try {
					Object propertyValue = converter.revert(entry.getValue());
					inputTypedProperties.put(propertyKey.jsonName(), propertyValue);

				} catch(ClassCastException cce) {

					throw new FrameworkException(422, "Invalid JSON input for key " + propertyKey.jsonName() + ", expected a JSON " + propertyKey.typeName() + ".");
				}

			} else {

				inputTypedProperties.put(propertyKey.jsonName(), entry.getValue());
			}
		}

		return inputTypedProperties;
	}

	public static PropertyMap cmisTypeToJavaType(final SecurityContext securityContext, final Class type, final Properties properties) throws FrameworkException {

		final Map<String, PropertyData<?>> map = properties.getProperties();
		final PropertyMap propertyMap          = new PropertyMap();

		for (final Entry<String, PropertyData<?>> entry : map.entrySet()) {

			final PropertyData<?> propertyValue = entry.getValue();
			Object value                        = propertyValue.getFirstValue();
			String key                          = entry.getKey();

			// convert CMIS properties to Structr properties
			if (CMIS_PROPERTY_MAPPING.containsKey(key)) {
				key = CMIS_PROPERTY_MAPPING.get(key);
			}

			final PropertyKey propertyKey = StructrApp.getConfiguration().getPropertyKeyForJSONName(type, key);
			if (propertyKey != null) {

				final PropertyConverter converter = propertyKey.inputConverter(securityContext);
				if (converter != null) {

					value = converter.convert(value);
				}

				propertyMap.put(propertyKey, value);

			} else {

				throw new FrameworkException(500, "Invalid property key " + key + " for type " + type.getSimpleName() + " provided.");
			}
		}

		return propertyMap;
	}

	private static PropertyMap fallbackPropertyMap(Map<String, Object> source) {

		PropertyMap map = new PropertyMap();

		logger.error("Using GenericProperty for input {}", source);
		//Thread.dumpStack();

		if (source != null) {


			for (Entry<String, Object> entry : source.entrySet()) {

				String key   = entry.getKey();
				Object value = entry.getValue();

				if (key != null && value != null) {

					map.put(new GenericProperty(key), value);
				}
			}
		}

		return map;
	}

	private static class PropertyKeyComparator implements Comparator<PropertyKey> {

		@Override
		public int compare(PropertyKey o1, PropertyKey o2) {
			return o1.jsonName().compareTo(o2.jsonName());
		}
	}

	public static GraphObject unwrap(final GraphObject source) {

		if (source instanceof CreationContainer) {

			final CreationContainer container = (CreationContainer)source;

			return container.getWrappedObject();
		}

		return source;
	}
}
