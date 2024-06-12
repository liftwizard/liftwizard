/*
 * Copyright 2024 Craig Motlin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.liftwizard.dropwizard.bundle.config.logging;

import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Optional;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.auto.service.AutoService;
import io.dropwizard.setup.Environment;
import io.liftwizard.dropwizard.bundle.prioritized.PrioritizedBundle;
import io.liftwizard.dropwizard.configuration.config.logging.ConfigLoggingFactoryProvider;
import io.liftwizard.dropwizard.configuration.enabled.EnabledFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The ConfigLoggingBundle logs the Dropwizard configuration to slf4j at INFO level, by serializing the in-memory configuration object to json.
 *
 * @see <a href="https://liftwizard.io/docs/configuration/ConfigLoggingBundle#configloggingbundle">https://liftwizard.io/docs/configuration/ConfigLoggingBundle#configloggingbundle</a>
 */
@AutoService(PrioritizedBundle.class)
public class ConfigLoggingBundle
        implements PrioritizedBundle
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigLoggingBundle.class);

    @Override
    public int getPriority()
    {
        return -9;
    }

    @Override
    public void runWithMdc(@Nonnull Object configuration, @Nonnull Environment environment)
            throws JsonProcessingException
    {
        ConfigLoggingFactoryProvider configLoggingFactoryProvider = this.safeCastConfiguration(
                ConfigLoggingFactoryProvider.class,
                configuration);

        EnabledFactory configLoggingFactory = configLoggingFactoryProvider.getConfigLoggingFactory();
        if (!configLoggingFactory.isEnabled())
        {
            LOGGER.info("{} disabled.", this.getClass().getSimpleName());
            return;
        }

        LOGGER.info("Running {}.", this.getClass().getSimpleName());

        ConfigLoggingBundle.logConfiguration(configuration, environment.getObjectMapper());

        LOGGER.info("Completing {}.", this.getClass().getSimpleName());
    }

    private static void logConfiguration(
            @Nonnull Object configuration,
            @Nonnull ObjectMapper objectMapper)
            throws JsonProcessingException
    {
        String fullConfigurationString = objectMapper.writeValueAsString(configuration);
        LOGGER.info("Dropwizard configuration (full):\n{}", fullConfigurationString);

        Optional<Object> maybeDefaultConfiguration = ConfigLoggingBundle
                .getConstructor(configuration)
                .flatMap(ConfigLoggingBundle::getDefaultConfiguration);
        if (maybeDefaultConfiguration.isEmpty())
        {
            return;
        }
        Object defaultConfiguration = maybeDefaultConfiguration.get();

        ObjectMapper nonDefaultObjectMapper = objectMapper.copy();
        nonDefaultObjectMapper.setMixInResolver(new JsonIncludeNonDefaultMixInResolver());

        ObjectNode configurationJsonNode = nonDefaultObjectMapper.valueToTree(configuration);
        ObjectNode defaultConfigurationJsonNode = nonDefaultObjectMapper.valueToTree(defaultConfiguration);

        subtractObjectNode(configurationJsonNode, defaultConfigurationJsonNode);
        removeEmptyNodes(configurationJsonNode);

        String configurationString = objectMapper.writeValueAsString(configurationJsonNode);
        LOGGER.info("Dropwizard configuration (minimized):\n{}", configurationString);
    }

    private static void removeEmptyNodes(@Nonnull ObjectNode node)
    {
        node.forEach(property ->
        {
            if (property.isObject())
            {
                removeEmptyNodes((ObjectNode) property);
            }
            else if (property.isArray())
            {
                property.elements().forEachRemaining(element ->
                {
                    if (element.isObject())
                    {
                        removeEmptyNodes((ObjectNode) element);
                    }
                });
            }
        });

        Iterator<Entry<String, JsonNode>> properties = node.fields();
        properties.forEachRemaining(
                property ->
                {
                    if (property.getValue().isArray())
                    {
                        Iterator<JsonNode> elements = property.getValue().elements();
                        removeEmptyJsonNodes(elements);
                    }
                });

        Iterator<Entry<String, JsonNode>> fieldIterator = node.fields();
        while (fieldIterator.hasNext())
        {
            Entry<String, JsonNode> property = fieldIterator.next();
            if ((property.getValue().isObject() || property.getValue().isArray()) && property.getValue().isEmpty())
            {
                fieldIterator.remove();
            }
        }
    }

    private static void removeEmptyJsonNodes(Iterator<JsonNode> elements)
    {
        while (elements.hasNext())
        {
            JsonNode element = elements.next();
            if (element.isObject() && element.isEmpty())
            {
                elements.remove();
            }
        }
    }

    private static void subtractObjectNode(ObjectNode mutableObjectNode, ObjectNode substractObjectNode)
    {
        substractObjectNode.fields().forEachRemaining(subtractProperty ->
        {
            String key = subtractProperty.getKey();
            JsonNode value = subtractProperty.getValue();
            if (!mutableObjectNode.has(key))
            {
                return;
            }

            subtractObjectNode(mutableObjectNode, key, value);
        });
    }

    private static void subtractObjectNode(ObjectNode mutableObjectNode, String key, JsonNode value)
    {
        JsonNode mutableValue = mutableObjectNode.get(key);
        if (mutableValue.isObject() && value.isObject())
        {
            subtractObjectNode((ObjectNode) mutableValue, (ObjectNode) value);
        }
        else if (mutableValue.isArray() && value.isArray())
        {
            Iterator<JsonNode> mutableElements = mutableValue.elements();
            Iterator<JsonNode> elements = value.elements();
            while (mutableElements.hasNext() && elements.hasNext())
            {
                JsonNode mutableElement = mutableElements.next();
                JsonNode element = elements.next();
                if (mutableElement.isObject() && element.isObject())
                {
                    subtractObjectNode((ObjectNode) mutableElement, (ObjectNode) element);
                }
            }
        }
        else if (mutableValue.equals(value))
        {
            mutableObjectNode.remove(key);
        }
    }

    @Nonnull
    private static Optional<Object> getDefaultConfiguration(@Nonnull Constructor<?> constructor)
    {
        try
        {
            return Optional.of(constructor.newInstance());
        }
        catch (ReflectiveOperationException e)
        {
            LOGGER.debug(
                    "Could not log Default Dropwizard configuration because {} is not instantiable through its no-arg constructor.",
                    constructor.getDeclaringClass().getCanonicalName());
            return Optional.empty();
        }
    }

    @Nonnull
    private static Optional<Constructor<?>> getConstructor(@Nonnull Object configuration)
    {
        try
        {
            return Optional.of(configuration.getClass().getConstructor());
        }
        catch (NoSuchMethodException e)
        {
            LOGGER.debug(
                    "Could not log Default Dropwizard configuration because {} does not implement a no-arg constructor.",
                    configuration.getClass().getCanonicalName());
            return Optional.empty();
        }
    }
}
