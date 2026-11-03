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

package io.liftwizard.dropwizard.configuration.auth.filter.firebase;

import java.util.List;
import java.util.Optional;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import io.liftwizard.firebase.principal.FirebasePrincipal;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

class FirebaseOAuthAuthenticatorTest
{
	private static final String SECRET_TOKEN = "secret-bearer-token-value";

	@Test
	void invalidTokenIsNotLogged()
	{
		var logger = (Logger) LoggerFactory.getLogger(FirebaseOAuthAuthenticatorTest.class);
		var listAppender = new ListAppender<ILoggingEvent>();
		listAppender.setContext(logger.getLoggerContext());
		listAppender.start();
		logger.addAppender(listAppender);

		FirebaseOptions options = FirebaseOptions.builder()
			.setCredentials(GoogleCredentials.create(new AccessToken("fake-access-token", null)))
			.setProjectId("test-project")
			.build();
		FirebaseApp firebaseApp = FirebaseApp.initializeApp(options, "invalidTokenIsNotLogged");

		try
		{
			var authenticator = new FirebaseOAuthAuthenticator(FirebaseAuth.getInstance(firebaseApp));

			Optional<FirebasePrincipal> principal = authenticator.authenticate(SECRET_TOKEN);

			assertThat(principal).isEmpty();
			List<String> messages = listAppender.list
				.stream()
				.map((event) -> event.getLevel() + " " + event.getFormattedMessage())
				.toList();
			assertThat(messages).containsExactly(
				Level.WARN
					+ " Firebase ID token verification failed: Failed to parse Firebase ID token. Make sure you passed a string that represents a complete and valid JWT. See https://firebase.google.com/docs/auth/admin/verify-id-tokens for details on how to retrieve an ID token."
			);
		}
		finally
		{
			logger.detachAppender(listAppender);
			firebaseApp.delete();
		}
	}
}
