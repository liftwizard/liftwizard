/*
 * Copyright 2014 dropwizard-bundles contributors
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

import javax.servlet.http.HttpServletRequest;

public class HttpsRedirect implements Redirect {

	private final boolean allowPrivateIps;

	public HttpsRedirect() {
		this(true);
	}

	public HttpsRedirect(boolean allowPrivateIps) {
		this.allowPrivateIps = allowPrivateIps;
	}

	@Override
	public String getRedirect(HttpServletRequest request) {
		if ("https".equalsIgnoreCase(request.getScheme())) {
			return null;
		}

		String forwardedProto = request.getHeader("X-Forwarded-Proto");
		if ("https".equalsIgnoreCase(forwardedProto)) {
			return null;
		}

		if (this.allowPrivateIps && isPrivateIp(request.getRemoteAddr())) {
			return null;
		}

		return getRedirectUrl(request, "https");
	}

	private boolean isPrivateIp(String remoteAddr) {
		return remoteAddr.startsWith("10.")
			|| remoteAddr.startsWith("192.168.")
			|| remoteAddr.startsWith("127.")
			|| remoteAddr.equals("0:0:0:0:0:0:0:1");
	}

	private String getRedirectUrl(HttpServletRequest request, String scheme) {
		StringBuilder url = new StringBuilder(scheme);
		url.append("://");
		url.append(request.getServerName());

		int port = request.getServerPort();
		if (port != 80 && port != 443) {
			url.append(':').append(port);
		}

		url.append(request.getRequestURI());

		String queryString = request.getQueryString();
		if (queryString != null) {
			url.append('?').append(queryString);
		}

		return url.toString();
	}
}
