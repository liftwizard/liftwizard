/*
 * Copyright 2021 Craig Motlin
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

package io.liftwizard.servlet.logging.mdc;

import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liftwizard.logging.slf4j.mdc.MultiMDCCloseable;
import io.liftwizard.servlet.logging.typesafe.StructuredArguments;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Stacks;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.stack.ImmutableStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StructuredArgumentsMDCLogger
        implements BiConsumer<StructuredArguments, Optional<String>>
{
    private static final Logger LOGGER = LoggerFactory.getLogger(StructuredArgumentsMDCLogger.class);

    @Nonnull
    private final ObjectMapper objectMapper;

    public StructuredArgumentsMDCLogger(@Nonnull ObjectMapper objectMapper)
    {
        this.objectMapper = Objects.requireNonNull(objectMapper);
    }

    @Override
    public void accept(@Nonnull StructuredArguments structuredArguments, @Nonnull Optional<String> maybeBody)
    {
        String message = maybeBody.orElseGet(structuredArguments::getEvent);
        // TODO 2021-12-07: Null out the event here

        ObjectNode objectNode = this.objectMapper.valueToTree(structuredArguments);
        try (MultiMDCCloseable ignored = this.structuredArgumentsToMDC(objectNode))
        {
            LOGGER.debug(message);
        }
    }

    private MultiMDCCloseable structuredArgumentsToMDC(@Nonnull ObjectNode objectNode)
    {
        MultiMDCCloseable result = new MultiMDCCloseable();
        this.structuredArgumentsToMDC(result, Stacks.immutable.empty(), objectNode);
        return result;
    }

    private void structuredArgumentsToMDC(
            @Nonnull MultiMDCCloseable mdc,
            @Nonnull ImmutableStack<String> stack,
            @Nonnull ObjectNode objectNode)
    {
        objectNode.fields().forEachRemaining(entry -> this.structuredArgumentToMDC(mdc, stack, entry));
    }

    private void structuredArgumentToMDC(
            @Nonnull MultiMDCCloseable mdc,
            @Nonnull ImmutableStack<String> stack,
            @Nonnull Entry<String, JsonNode> entry)
    {
        String   key   = entry.getKey();
        JsonNode value = entry.getValue();

        if (value.isObject())
        {
            ImmutableStack<String> nextStack      = stack.push(key);
            ObjectNode             nextObjectNode = (ObjectNode) value;
            this.structuredArgumentsToMDC(mdc, nextStack, nextObjectNode);
            return;
        }

        String keyString = stack.isEmpty()
                ? key
                : stack.toList().toReversed().makeString("", ".", "." + key);

        if (value.isArray())
        {
            MutableList<String> list = Lists.mutable.empty();
            value.iterator().forEachRemaining(each -> list.add(each.textValue()));
            mdc.put(keyString, list.makeString());
        }
        else
        {
            mdc.put(keyString, value.asText());
        }
    }
}
