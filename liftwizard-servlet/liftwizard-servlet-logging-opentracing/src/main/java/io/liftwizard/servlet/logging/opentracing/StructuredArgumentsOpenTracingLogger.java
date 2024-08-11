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

package io.liftwizard.servlet.logging.opentracing;

import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liftwizard.servlet.logging.typesafe.StructuredArguments;
import io.opentracing.Span;
import io.opentracing.util.GlobalTracer;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Stacks;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.stack.ImmutableStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StructuredArgumentsOpenTracingLogger
        implements Consumer<StructuredArguments>
{
    private static final Logger LOGGER = LoggerFactory.getLogger(StructuredArgumentsOpenTracingLogger.class);

    @Nonnull
    private final ObjectMapper objectMapper;

    public StructuredArgumentsOpenTracingLogger(@Nonnull ObjectMapper objectMapper)
    {
        this.objectMapper = Objects.requireNonNull(objectMapper);
    }

    @Override
    public void accept(StructuredArguments structuredArguments)
    {
        ObjectNode objectNode = this.objectMapper.valueToTree(structuredArguments);
        this.structuredArgumentsToSpans(objectNode);
        LOGGER.info("Response sent");
    }

    private void structuredArgumentsToSpans(@Nonnull ObjectNode objectNode)
    {
        Span span = GlobalTracer.get().activeSpan();
        if (span == null)
        {
            return;
        }

        this.structuredArgumentsToSpans(span, Stacks.immutable.empty(), objectNode);
    }

    private void structuredArgumentsToSpans(
            @Nonnull Span span,
            @Nonnull ImmutableStack<String> stack,
            @Nonnull ObjectNode objectNode)
    {
        objectNode.fields().forEachRemaining(entry -> this.structuredArgumentToSpan(span, stack, entry));
    }

    private void structuredArgumentToSpan(
            @Nonnull Span span,
            @Nonnull ImmutableStack<String> stack,
            @Nonnull Entry<String, JsonNode> entry)
    {
        String key = entry.getKey();
        JsonNode value = entry.getValue();

        if (value.isObject())
        {
            ImmutableStack<String> nextStack = stack.push(key);
            ObjectNode nextObjectNode = (ObjectNode) value;
            this.structuredArgumentsToSpans(span, nextStack, nextObjectNode);
            return;
        }

        String keyString = stack.isEmpty()
                ? key
                : stack.toList().toReversed().makeString("", ".", "." + key);

        if (value.isArray())
        {
            MutableList<String> list = Lists.mutable.empty();
            value.iterator().forEachRemaining(each -> list.add(each.textValue()));
            span.setTag(keyString, list.makeString());
        }
        else if (value.isNumber())
        {
            span.setTag(keyString, value.numberValue());
        }
        else if (value.isBoolean())
        {
            span.setTag(keyString, value.booleanValue());
        }
        else if (value.isTextual())
        {
            span.setTag(keyString, value.textValue());
        }
        else
        {
            throw new AssertionError(value);
        }
    }
}
