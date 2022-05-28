package com.armin.security;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;

import java.security.SecureRandom;
import java.text.ParseException;

/**
 * JwtJose
 *
 * @author zy
 * @version 2022/5/28
 */
public class JwtJose {
    public static void main(String[] args) throws JOSEException, ParseException {
        // Create an HMAC-protected JWS object with some payload
        JWSObject jwsObject =
                new JWSObject(new JWSHeader(JWSAlgorithm.HS256), new Payload("Hello, world!"));

        // We need a 256-bit key for HS256 which must be pre-shared
        byte[] sharedKey = new byte[32];
        new SecureRandom().nextBytes(sharedKey);

        // Apply the HMAC to the JWS object
        jwsObject.sign(new MACSigner(sharedKey));

        // Output in URL-safe format
        System.out.println(jwsObject.serialize());

        jwtWithRsa();
    }

    public static void jwtWithRsa() throws JOSEException, ParseException {
        // RSA signatures require a public and private RSA key pair,
        // the public key must be made known to the JWS recipient to
        // allow the signatures to be verified
        RSAKey rsaJWK = new RSAKeyGenerator(2048).keyID("123").generate();
        RSAKey rsaPublicJWK = rsaJWK.toPublicJWK();

        // Create RSA-signer with the private key
        JWSSigner signer = new RSASSASigner(rsaJWK);

        // Prepare JWS object with simple string as payload
        JWSObject jwsObject =
                new JWSObject(
                        new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaJWK.getKeyID()).build(),
                        new Payload("In RSA we trust!"));

        // Compute the RSA signature
        jwsObject.sign(signer);

        // To serialize to compact form, produces something like
        // eyJhbGciOiJSUzI1NiJ9.SW4gUlNBIHdlIHRydXN0IQ.IRMQENi4nJyp4er2L
        // mZq3ivwoAjqa1uUkSBKFIX7ATndFF5ivnt-m8uApHO4kfIFOrW7w2Ezmlg3Qd
        // maXlS9DhN0nUk_hGI3amEjkKd0BWYCB8vfUbUv0XGjQip78AI4z1PrFRNidm7
        // -jPDm5Iq0SZnjKjCNS5Q15fokXZc8u0A
        String s = jwsObject.serialize();

        // To parse the JWS and verify it, e.g. on client-side
        jwsObject = JWSObject.parse(s);

        JWSVerifier verifier = new RSASSAVerifier(rsaPublicJWK);

        System.err.println(jwsObject.verify(verifier));

        System.err.println("In RSA we trust!".equals(jwsObject.getPayload().toString()));
    }
}
