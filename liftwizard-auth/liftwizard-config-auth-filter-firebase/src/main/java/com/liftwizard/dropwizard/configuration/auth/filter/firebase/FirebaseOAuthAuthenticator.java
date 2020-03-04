package com.liftwizard.dropwizard.configuration.auth.filter.firebase;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Optional;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.liftwizard.firebase.principal.FirebasePrincipal;
import io.dropwizard.auth.Authenticator;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class FirebaseOAuthAuthenticator implements Authenticator<String, FirebasePrincipal>
{
    private static final Logger LOGGER = LoggerFactory.getLogger(FirebaseOAuthAuthenticator.class);

    private static final String FIREBASE_UID  = "liftwizard.auth.firebase.uid";
    private static final String FIREBASE_NAME = "liftwizard.auth.firebase.name";

    private static final ImmutableList<String> MDC_KEYS = Lists.immutable.with(
            FIREBASE_UID,
            FIREBASE_NAME);

    private final FirebaseAuth firebaseAuth;

    public FirebaseOAuthAuthenticator(FirebaseAuth firebaseAuth)
    {
        this.firebaseAuth = firebaseAuth;
    }

    public static ImmutableList<String> getMDCKeys()
    {
        return MDC_KEYS;
    }

    @Override
    public Optional<FirebasePrincipal> authenticate(String credentials)
    {
        try
        {
            FirebaseToken       firebaseToken = this.firebaseAuth.verifyIdToken(credentials);
            Map<String, Object> claims        = firebaseToken.getClaims();

            Map<String, Object> firebase       = (Map<String, Object>) claims.get("firebase");
            String              signInProvider = (String) firebase.get("sign_in_provider");

            String  uid           = firebaseToken.getUid();
            String  name          = firebaseToken.getName();
            String  email         = firebaseToken.getEmail();
            boolean emailVerified = firebaseToken.isEmailVerified();
            String  issuer        = firebaseToken.getIssuer();
            String  picture       = firebaseToken.getPicture();

            MDC.put(FIREBASE_UID, uid);
            MDC.put(FIREBASE_NAME, name);

            FirebasePrincipal firebasePrincipal = new FirebasePrincipal(
                    uid,
                    name,
                    email,
                    emailVerified,
                    issuer,
                    picture,
                    signInProvider);

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
            LOGGER.warn("", e);
            return Optional.empty();
        }
    }
}
