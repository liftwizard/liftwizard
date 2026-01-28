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

	/**
	 * Verifies that network isolation blocks external connections.
	 *
	 * <p>The test uses an IP address (8.8.8.8, Google's public DNS) instead of a hostname
	 * to ensure consistent behavior across platforms. Using a hostname like "www.google.com"
	 * causes platform-specific behavior:
	 * <ul>
	 *   <li>Linux: SOCKS proxy intercepts before DNS, throwing SocketException</li>
	 *   <li>macOS: DNS resolution fails first, throwing UnknownHostException</li>
	 * </ul>
	 *
	 * <p>By using an IP address, we skip DNS resolution entirely and the SOCKS proxy
	 * consistently blocks the connection with a SocketException on all platforms.
	 */
	@Test
	void shouldFailToConnectWhenNetworkIsDisabled() {
		assertThatThrownBy(() -> {
			try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
				// Use IP address to skip DNS resolution and ensure consistent cross-platform behavior
				var request = new HttpGet("http://8.8.8.8");
				httpClient.execute(request, (response) -> EntityUtils.toString(response.getEntity()));
			}
		})
			.isInstanceOf(SocketException.class)
			.hasMessageContaining("SOCKS");
	}
}
