package com.rulebased848.gamsibot.service;

import io.jsonwebtoken.Jwts;
import static io.jsonwebtoken.io.Decoders.BASE64;
import static io.jsonwebtoken.security.Keys.hmacShaKeyFor;
import java.security.Key;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtService {
    private static final long expirationTimeMillis = 86400000;

    private final Key key;

    @Autowired
    public JwtService(@Value("${jwt.secret}") final String secret) {
        key = hmacShaKeyFor(BASE64.decode(secret));
    }

    public String getToken(String username) {
        return Jwts.builder()
            .setSubject(username)
            .setExpiration(new Date(System.currentTimeMillis() + expirationTimeMillis))
            .signWith(key)
            .compact();
    }

    public String getAuthUser(String jws) {
        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(jws)
            .getBody()
            .getSubject();
    }
}