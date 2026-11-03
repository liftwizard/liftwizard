/*
 * Copyright 2026 Craig Motlin
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

package io.dropwizard.bundles.redirect;

import java.lang.reflect.Proxy;

import javax.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HttpsRedirectTest
{
	private final HttpsRedirect httpsRedirect = new HttpsRedirect();

	@Test
	void privateAndLoopbackAddressesAreNotRedirected()
	{
		assertThat(this.httpsRedirect.getRedirect(request("10.1.2.3", 8080, null))).isNull();
		assertThat(this.httpsRedirect.getRedirect(request("192.168.1.1", 8080, null))).isNull();
		assertThat(this.httpsRedirect.getRedirect(request("127.0.0.1", 8080, null))).isNull();
		assertThat(this.httpsRedirect.getRedirect(request("172.16.0.1", 8080, null))).isNull();
		assertThat(this.httpsRedirect.getRedirect(request("172.17.0.1", 8080, null))).isNull();
		assertThat(this.httpsRedirect.getRedirect(request("172.31.255.255", 8080, null))).isNull();
		assertThat(this.httpsRedirect.getRedirect(request("0:0:0:0:0:0:0:1", 8080, null))).isNull();
		assertThat(this.httpsRedirect.getRedirect(request("[0:0:0:0:0:0:0:1]", 8080, null))).isNull();
		assertThat(this.httpsRedirect.getRedirect(request("[::1]", 8080, null))).isNull();
	}

	@Test
	void publicAddressesNextToPrivateRangesAreRedirected()
	{
		assertThat(this.httpsRedirect.getRedirect(request("172.15.0.1", 80, null))).isEqualTo(
			"https://example.com/path?a=b"
		);
		assertThat(this.httpsRedirect.getRedirect(request("172.32.0.1", 80, null))).isEqualTo(
			"https://example.com/path?a=b"
		);
		assertThat(this.httpsRedirect.getRedirect(request("8.8.8.8", 80, null))).isEqualTo(
			"https://example.com/path?a=b"
		);
	}

	@Test
	void redirectDropsPlaintextPort()
	{
		assertThat(this.httpsRedirect.getRedirect(request("8.8.8.8", 8080, null))).isEqualTo(
			"https://example.com/path?a=b"
		);
	}

	@Test
	void httpsRequestsAreNotRedirected()
	{
		assertThat(this.httpsRedirect.getRedirect(request("8.8.8.8", 8080, "https"))).isNull();
	}

	@Test
	void privateAddressesAreRedirectedWhenNotAllowed()
	{
		var strictRedirect = new HttpsRedirect(false);
		assertThat(strictRedirect.getRedirect(request("172.17.0.1", 8080, null))).isEqualTo(
			"https://example.com/path?a=b"
		);
	}

	private static HttpServletRequest request(String remoteAddr, int serverPort, String forwardedProto)
	{
		return (HttpServletRequest) Proxy.newProxyInstance(
			HttpServletRequest.class.getClassLoader(),
			new Class<?>[]
			{
				HttpServletRequest.class,
			},
			(proxy, method, args) ->
				switch (method.getName())
				{
					case "getScheme" -> "http";
					case "getHeader" -> "X-Forwarded-Proto".equals(args[0]) ? forwardedProto : null;
					case "getRemoteAddr" -> remoteAddr;
					case "getServerName" -> "example.com";
					case "getServerPort" -> serverPort;
					case "getRequestURI" -> "/path";
					case "getQueryString" -> "a=b";
					default -> throw new UnsupportedOperationException(method.getName());
				}
		);
	}
}
