package com.showroom.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Issues and reads the login token. It carries user id, showroom id and the permission codes. */
@Service
public class JwtService {
    private final SecretKey key;
    private final Duration expiry;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiry-minutes}") long expiryMinutes) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiry = Duration.ofMinutes(expiryMinutes);
    }

    public String issue(Long userId, Long showroomId, String name, Collection<String> permissions) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("sid", showroomId)
                .claim("name", name)
                .claim("perms", List.copyOf(permissions))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expiry)))
                .signWith(key)
                .compact();
    }

    /** Throws a JwtException (or IllegalArgumentException) if the token is invalid or expired. */
    public AuthUser parse(String token) {
        Claims c = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        Set<String> perms = new HashSet<>();
        Object raw = c.get("perms");
        if (raw instanceof Collection<?> list) {
            list.forEach(p -> perms.add(String.valueOf(p)));
        }
        return new AuthUser(Long.valueOf(c.getSubject()), ((Number) c.get("sid")).longValue(),
                c.get("name", String.class), perms);
    }
}
