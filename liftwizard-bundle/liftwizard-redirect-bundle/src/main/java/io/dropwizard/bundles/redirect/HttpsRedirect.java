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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

public class HttpsRedirect
	implements Redirect
{
	private static final Pattern IPV4_LITERAL = Pattern.compile("\\d{1,3}(\\.\\d{1,3}){3}");

	private final boolean allowPrivateIps;

	public HttpsRedirect()
	{
		this(true);
	}

	public HttpsRedirect(boolean allowPrivateIps)
	{
		this.allowPrivateIps = allowPrivateIps;
	}

	@Override
	public String getRedirect(HttpServletRequest request)
	{
		if ("https".equalsIgnoreCase(request.getScheme()))
		{
			return null;
		}

		String forwardedProto = request.getHeader("X-Forwarded-Proto");
		if ("https".equalsIgnoreCase(forwardedProto))
		{
			return null;
		}

		if (this.allowPrivateIps && this.isPrivateIp(request.getRemoteAddr()))
		{
			return null;
		}

		return this.getRedirectUrl(request, "https");
	}

	/**
	 * Returns true for loopback addresses and the RFC 1918 ranges 10/8, 172.16/12 and 192.168/16.
	 * Jetty reports IPv6 remote addresses in brackets, like {@code [0:0:0:0:0:0:0:1]}.
	 */
	private boolean isPrivateIp(String remoteAddr)
	{
		String address =
			remoteAddr.startsWith("[") && remoteAddr.endsWith("]") ?
				remoteAddr.substring(1, remoteAddr.length() - 1) :
				remoteAddr;

		// Only parse IP literals, so InetAddress never falls back to a DNS lookup.
		if (!address.contains(":") && !IPV4_LITERAL.matcher(address).matches())
		{
			return false;
		}

		try
		{
			InetAddress inetAddress = InetAddress.getByName(address);
			return inetAddress.isLoopbackAddress() || inetAddress.isSiteLocalAddress();
		}
		catch (UnknownHostException e)
		{
			return false;
		}
	}

	private String getRedirectUrl(HttpServletRequest request, String scheme)
	{
		var url = new StringBuilder(scheme);
		url.append("://");
		// Drop the port: the request arrived on a plaintext port, so speaking TLS to it would fail.
		url.append(request.getServerName());
		url.append(request.getRequestURI());

		String queryString = request.getQueryString();
		if (queryString != null)
		{
			url.append('?').append(queryString);
		}

		return url.toString();
	}
}
