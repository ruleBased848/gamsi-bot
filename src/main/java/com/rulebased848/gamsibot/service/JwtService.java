package com.rulebased848.gamsibot.service;

import io.jsonwebtoken.Jwts;
import static io.jsonwebtoken.io.Decoders.BASE64;
import static io.jsonwebtoken.security.Keys.hmacShaKeyFor;
import java.security.Key;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JwtService {
    private final long expirationTimeMillis;

    private final Key key;

    @Autowired
    public JwtService(JwtProps jwtProps) {
        expirationTimeMillis = jwtProps.getExpirationTimeMillis();
        key = hmacShaKeyFor(BASE64.decode(jwtProps.getSecret()));
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