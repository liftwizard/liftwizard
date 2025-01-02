/*
 * Copyright 2025 Craig Motlin
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

package io.liftwizard.logging.exception.mapper;

import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.WriterInterceptor;

import io.dropwizard.jersey.errors.EarlyEofExceptionMapper;
import io.dropwizard.jersey.errors.EofExceptionWriterInterceptor;
import io.dropwizard.jersey.errors.IllegalStateExceptionMapper;
import io.dropwizard.jersey.jackson.JsonProcessingExceptionMapper;
import io.dropwizard.jersey.optional.EmptyOptionalExceptionMapper;
import io.dropwizard.jersey.validation.JerseyViolationExceptionMapper;
import org.glassfish.jersey.internal.inject.AbstractBinder;

/**
 * A binder that registers all the default exception mappers while allowing users to override
 * individual exception mappers without disabling all others.
 *
 * Forked from io.dropwizard.setup.ExceptionMapperBinder to register LiftwizardLoggingExceptionMapper instead of LoggingExceptionMapper.
 */
public class LiftwizardExceptionMapperBinder
        extends AbstractBinder {
    private final boolean showDetails;

    public LiftwizardExceptionMapperBinder(boolean showDetails) {
        this.showDetails = showDetails;
    }

    @Override
    protected void configure() {
        bind(new LiftwizardLoggingExceptionMapper<Throwable>() {
        }).to(ExceptionMapper.class);
        bind(JerseyViolationExceptionMapper.class).to(ExceptionMapper.class);
        bind(new JsonProcessingExceptionMapper(isShowDetails())).to(ExceptionMapper.class);
        bind(EarlyEofExceptionMapper.class).to(ExceptionMapper.class);
        bind(EofExceptionWriterInterceptor.class).to(WriterInterceptor.class);
        bind(EmptyOptionalExceptionMapper.class).to(ExceptionMapper.class);
        bind(IllegalStateExceptionMapper.class).to(ExceptionMapper.class);
    }

    public boolean isShowDetails() {
        return showDetails;
    }
}
