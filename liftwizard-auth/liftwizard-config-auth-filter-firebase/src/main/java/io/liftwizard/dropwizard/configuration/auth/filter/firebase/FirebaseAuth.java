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

package io.liftwizard.dropwizard.configuration.auth.filter.firebase;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseAuth {

    private final String databaseUrl;
    private final String firebaseConfig;

    private FirebaseApp firebaseApp;
    private GoogleCredentials credentials;

    public FirebaseAuth(String databaseUrl, String firebaseConfig) {
        this.databaseUrl = Objects.requireNonNull(databaseUrl);
        this.firebaseConfig = Objects.requireNonNull(firebaseConfig);
    }

    public GoogleCredentials getCredentials() {
        return this.credentials;
    }

    public FirebaseApp getFirebaseApp() {
        return this.firebaseApp;
    }

    public com.google.firebase.auth.FirebaseAuth getFirebaseAuth() {
        this.initFirebaseApp();
        return com.google.firebase.auth.FirebaseAuth.getInstance(this.firebaseApp);
    }

    public FirebaseDatabase getFirebaseDatabase() {
        this.initFirebaseApp();
        return FirebaseDatabase.getInstance(this.firebaseApp);
    }

    private void initFirebaseApp() {
        if (this.firebaseApp != null) {
            return;
        }

        this.initCredentials();
        FirebaseOptions options = FirebaseOptions.builder()
            .setCredentials(this.credentials)
            .setDatabaseUrl(this.databaseUrl)
            .build();

        this.firebaseApp = FirebaseApp.initializeApp(options);
    }

    private void initCredentials() {
        if (this.credentials != null) {
            return;
        }

        byte[] bytes = this.firebaseConfig.getBytes(StandardCharsets.UTF_8);
        InputStream firebaseCredentials = new ByteArrayInputStream(bytes);
        try {
            this.credentials = GoogleCredentials.fromStream(firebaseCredentials);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
