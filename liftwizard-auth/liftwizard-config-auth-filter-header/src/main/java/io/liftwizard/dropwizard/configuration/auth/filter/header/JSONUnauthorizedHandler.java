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

package io.liftwizard.dropwizard.configuration.auth.filter.header;

import io.dropwizard.auth.UnauthorizedHandler;
import io.dropwizard.jersey.errors.ErrorMessage;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class JSONUnauthorizedHandler implements UnauthorizedHandler {

    @Override
    public Response buildResponse(String headerName, String prefixName) {
        String message = "Single value header '%s' with prefix '%s' is required.".formatted(headerName, prefixName);
        ErrorMessage errorMessage = new ErrorMessage(Status.UNAUTHORIZED.getStatusCode(), message);
        return Response.status(errorMessage.getCode())
            .header(HttpHeaders.WWW_AUTHENTICATE, headerName)
            .type(MediaType.APPLICATION_JSON_TYPE)
            .entity(errorMessage)
            .build();
    }
}
