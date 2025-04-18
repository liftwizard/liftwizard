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

package io.liftwizard.servlet.logging.typesafe;

import java.time.Duration;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StructuredArgumentsResponseHttp extends StructuredArgumentsHttp {

    private final StructuredArgumentsStatus status = new StructuredArgumentsStatus();

    private String entityType;
    private String contentType;
    private Long elapsedNanos;

    @JsonProperty
    public StructuredArgumentsStatus getStatus() {
        return this.status;
    }

    @JsonProperty
    public String getEntityType() {
        return this.entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = Objects.requireNonNull(entityType);
    }

    @JsonProperty
    public String getContentType() {
        return this.contentType;
    }

    public void setContentType(String contentType) {
        if (this.contentType != null) {
            throw new AssertionError(this.contentType);
        }
        this.contentType = Objects.requireNonNull(contentType);
    }

    @JsonProperty
    public Long getElapsedNanos() {
        return this.elapsedNanos;
    }

    public void setElapsed(Duration elapsed) {
        if (this.elapsedNanos != null) {
            throw new AssertionError(this.elapsedNanos);
        }
        this.elapsedNanos = elapsed.toNanos();
    }
}
