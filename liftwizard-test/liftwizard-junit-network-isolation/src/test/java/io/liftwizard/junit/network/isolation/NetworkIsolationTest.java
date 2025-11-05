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

package io.liftwizard.junit.network.isolation;

import java.net.SocketException;

import io.liftwizard.junit.extension.log.marker.LogMarkerTestExtension;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(LogMarkerTestExtension.class)
class NetworkIsolationTest {

    @Test
    void shouldFailToConnectWhenNetworkIsDisabled() {
        assertThatThrownBy(() -> {
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpGet request = new HttpGet("https://www.google.com");
                httpClient.execute(request, response -> EntityUtils.toString(response.getEntity()));
            }
        })
            .isInstanceOf(SocketException.class)
            .hasMessageContaining("Can't connect to SOCKS proxy:Connection refused");
    }
}
