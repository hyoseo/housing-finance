package me.hyoseo.housingfinance.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import me.hyoseo.housingfinance.error.CommonException;
import me.hyoseo.housingfinance.error.ErrorCode;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class CryptoService {

    @Value("${key.password-secret}")
    private String passwordSecretKey;

    @Value("${key.jwt-secret}")
    private String jwtSecretKey;

    public String encrypt(String key, String text) {
        try {
            key = passwordSecretKey + key;

            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secret_key);

            return Base64.encodeBase64String(sha256_HMAC.doFinal(text.getBytes()));
        } catch (Exception ex) {
            throw CommonException.create(ErrorCode.INTERNAL_SERVER_ERROR, ex);
        }
    }

    public String createToken(String userId) {
        return Jwts.builder()
                .setIssuer("hyoseo")
                .setHeaderParam("typ", "JWT")
                .claim("uid", userId)
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                .signWith(SignatureAlgorithm.HS256, jwtSecretKey.getBytes(StandardCharsets.UTF_8))
                .compact();
    }

    public String parse(String accessToken) {
        try {
            Claims claims = Jwts.parser().setSigningKey(jwtSecretKey.getBytes(StandardCharsets.UTF_8))
                    .parseClaimsJws(accessToken).getBody();

            return (String) claims.get("uid");
        } catch (Exception ex) {
            throw CommonException.create(ErrorCode.FORBIDDEN, ex);
        }
    }
}
