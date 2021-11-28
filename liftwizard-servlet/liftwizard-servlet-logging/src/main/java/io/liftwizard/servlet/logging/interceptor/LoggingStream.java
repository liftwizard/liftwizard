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

package io.liftwizard.servlet.logging.interceptor;

import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;

import javax.annotation.Nonnull;

import io.liftwizard.servlet.logging.typesafe.StructuredArguments;

/**
 * Helper class used to log an entity to the output stream up to the specified maximum number of bytes.
 */
public class LoggingStream
        extends FilterOutputStream
{
    private final int maxEntitySize;

    @Nonnull
    private final BiConsumer<StructuredArguments, Optional<String>> logger;

    @Nonnull
    private final StructuredArguments structuredArguments;

    private final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

    public LoggingStream(
            @Nonnull OutputStream inner,
            int maxEntitySize,
            @Nonnull BiConsumer<StructuredArguments, Optional<String>> logger,
            @Nonnull StructuredArguments structuredArguments)
    {
        super(inner);

        this.maxEntitySize       = maxEntitySize;
        this.logger              = Objects.requireNonNull(logger);
        this.structuredArguments = Objects.requireNonNull(structuredArguments);
    }

    public void complete(Charset charset)
    {
        // write entity to the builder
        byte[] entity = this.byteArrayOutputStream.toByteArray();

        if (entity.length == 0)
        {
            this.logger.accept(this.structuredArguments, Optional.empty());
            return;
        }

        String mainBody = new String(
                entity,
                0,
                Math.min(entity.length, this.maxEntitySize),
                charset);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(mainBody);
        if (entity.length > this.maxEntitySize)
        {
            stringBuilder.append("...more...");
        }
        stringBuilder.append('\n');
        Optional<String> body = Optional.of(stringBuilder.toString());
        this.logger.accept(this.structuredArguments, body);
    }

    @Override
    public void write(int i)
            throws IOException
    {
        if (this.byteArrayOutputStream.size() <= this.maxEntitySize)
        {
            this.byteArrayOutputStream.write(i);
        }
        this.out.write(i);
    }

    @Override
    public void write(byte[] ba, int off, int len)
            throws IOException
    {
        if ((off | len | ba.length - (len + off) | off + len) < 0)
        {
            throw new IndexOutOfBoundsException();
        }
        if (this.byteArrayOutputStream.size() + len <= this.maxEntitySize)
        {
            this.byteArrayOutputStream.write(ba, off, len);
        }
        this.out.write(ba, off, len);
    }
}
