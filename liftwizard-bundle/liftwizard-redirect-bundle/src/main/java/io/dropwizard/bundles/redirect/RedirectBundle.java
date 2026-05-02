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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.dropwizard.core.Configuration;
import io.dropwizard.core.ConfiguredBundle;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;

public class RedirectBundle implements ConfiguredBundle<Configuration> {

	private final List<Redirect> redirects;

	public RedirectBundle(Redirect... redirects) {
		this.redirects = new ArrayList<>();
		Collections.addAll(this.redirects, redirects);
	}

	@Override
	public void initialize(Bootstrap<?> bootstrap) {
	}

	@Override
	public void run(Configuration configuration, Environment environment) {
		environment
			.servlets()
			.addFilter("redirect", new RedirectFilter())
			.addMappingForUrlPatterns(null, false, "*");
	}

	private class RedirectFilter implements Filter {

		@Override
		public void init(FilterConfig filterConfig) {
		}

		@Override
		public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

			if (request instanceof HttpServletRequest) {
				HttpServletRequest httpRequest = (HttpServletRequest) request;
				for (Redirect redirect : RedirectBundle.this.redirects) {
					String redirectUrl = redirect.getRedirect(httpRequest);
					if (redirectUrl != null) {
						((HttpServletResponse) response).sendRedirect(redirectUrl);
						return;
					}
				}
			}

			chain.doFilter(request, response);
		}

		@Override
		public void destroy() {
		}
	}
}
