package yurykorzun.art.universe.common.auth.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import yurykorzun.art.universe.common.auth.config.AuthProperties;

import java.util.Collections;

@Service
@Slf4j
public class GoogleTokenVerifier {

    private final GoogleIdTokenVerifier verifier;

    public GoogleTokenVerifier(AuthProperties authProperties) {
        this.verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(), GsonFactory.getDefaultInstance())
            .setAudience(Collections.singletonList(authProperties.getGoogle().getClientId()))
            .build();
    }

    public GoogleIdToken.Payload verify(String idTokenString) {
        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null) {
                return idToken.getPayload();
            }
            log.warn("Invalid Google ID token");
            return null;
        } catch (Exception e) {
            log.error("Failed to verify Google ID token", e);
            return null;
        }
    }
}
