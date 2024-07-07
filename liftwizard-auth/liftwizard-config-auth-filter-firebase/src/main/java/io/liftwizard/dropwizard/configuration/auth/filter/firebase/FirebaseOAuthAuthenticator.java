/*
 * Copyright 2020 Craig Motlin
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

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import io.dropwizard.auth.Authenticator;
import io.liftwizard.firebase.principal.FirebasePrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FirebaseOAuthAuthenticator
        implements Authenticator<String, FirebasePrincipal>
{
    private static final Logger LOGGER = LoggerFactory.getLogger(FirebaseOAuthAuthenticator.class);

    private final FirebaseAuth firebaseAuth;

    public FirebaseOAuthAuthenticator(FirebaseAuth firebaseAuth)
    {
        this.firebaseAuth = firebaseAuth;
    }

    @Override
    public Optional<FirebasePrincipal> authenticate(String credentials)
    {
        try
        {
            FirebaseToken     firebaseToken     = this.firebaseAuth.verifyIdToken(credentials);
            FirebasePrincipal firebasePrincipal = getFirebasePrincipal(firebaseToken);

            return Optional.of(firebasePrincipal);
        }
        catch (FirebaseAuthException e)
        {
            Throwable cause = e.getCause();
            if (cause instanceof UnknownHostException)
            {
                throw new RuntimeException(e);
            }
            if (cause instanceof SocketTimeoutException)
            {
                throw new RuntimeException(e);
            }
            LOGGER.warn(credentials, e.getMessage());
            return Optional.empty();
        }
    }

    @Nonnull
    private static FirebasePrincipal getFirebasePrincipal(@Nonnull FirebaseToken firebaseToken)
    {
        Map<String, Object> claims        = firebaseToken.getClaims();

        Map<String, Object> firebase       = (Map<String, Object>) claims.get("firebase");
        String              signInProvider = (String) firebase.get("sign_in_provider");

        String  uid           = firebaseToken.getUid();
        String  name          = firebaseToken.getName();
        String  email         = firebaseToken.getEmail();
        boolean emailVerified = firebaseToken.isEmailVerified();
        String  issuer        = firebaseToken.getIssuer();
        String  picture       = firebaseToken.getPicture();

        return new FirebasePrincipal(
                uid,
                name,
                email,
                emailVerified,
                issuer,
                picture,
                signInProvider);
    }
}
