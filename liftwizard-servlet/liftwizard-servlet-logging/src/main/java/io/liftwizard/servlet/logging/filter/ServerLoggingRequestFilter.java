/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2016 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package io.liftwizard.servlet.logging.filter;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;

import javax.annotation.Nonnull;
import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;

import io.liftwizard.servlet.logging.feature.LoggingConfig;
import io.liftwizard.servlet.logging.mediatype.MediaTypeUtility;
import io.liftwizard.servlet.logging.typesafe.StructuredArguments;
import org.glassfish.jersey.message.MessageUtils;

@ConstrainedTo(RuntimeType.SERVER)
public final class ServerLoggingRequestFilter
        extends AbstractLoggingFilter
        implements ContainerRequestFilter
{
    private final BiConsumer<StructuredArguments, Optional<String>> logger;

    public ServerLoggingRequestFilter(
            @Nonnull LoggingConfig loggingConfig,
            @Nonnull BiConsumer<StructuredArguments, Optional<String>> logger)
    {
        super(loggingConfig);
        this.logger = Objects.requireNonNull(logger);
    }

    @Override
    public void filter(@Nonnull ContainerRequestContext requestContext)
            throws IOException
    {
        if (requestContext.getProperty("structuredArguments") != null)
        {
            throw new IllegalStateException();
        }
        StructuredArguments structuredArguments = this.initRequestStructuredArguments(
                requestContext,
                "Server has received a request");
        Optional<String> body = this.getBody(requestContext);
        this.logger.accept(structuredArguments, body);
    }

    private Optional<String> getBody(@Nonnull ContainerRequestContext requestContext)
            throws IOException
    {
        if (!this.loggingConfig.isLogRequestBodies()
                || !requestContext.hasEntity()
                || !MediaTypeUtility.isReadable(requestContext.getMediaType()))
        {
            return Optional.empty();
        }

        int maxEntitySize = this.loggingConfig.getMaxEntitySize();

        InputStream inputStream = this.getMarkSupportedInputStream(requestContext.getEntityStream());
        inputStream.mark(maxEntitySize + 1);
        byte[] entity = new byte[maxEntitySize + 1];

        int entitySize = 0;
        while (entitySize < entity.length)
        {
            int readBytes = inputStream.read(entity, entitySize, entity.length - entitySize);
            if (readBytes < 0)
            {
                break;
            }
            entitySize += readBytes;
        }

        String mainBody = new String(
                entity,
                0,
                Math.min(entitySize, maxEntitySize),
                MessageUtils.getCharset(requestContext.getMediaType()));

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(mainBody);
        if (entitySize > maxEntitySize)
        {
            stringBuilder.append("...more...");
        }
        stringBuilder.append('\n');
        String body = stringBuilder.toString();

        inputStream.reset();
        requestContext.setEntityStream(inputStream);

        return Optional.of(body);
    }

    @Nonnull
    private InputStream getMarkSupportedInputStream(@Nonnull InputStream stream)
    {
        return stream.markSupported() ? stream : new BufferedInputStream(stream);
    }
}
