package com.example.projet_java.Service.Impl;

import com.example.projet_java.Model.User;
import com.example.projet_java.Service.JwtService;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.*;
import org.springframework.security.core.GrantedAuthority;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class JwtServiceImpl implements JwtService {
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 *60 *60 * 24))
                .signWith(getSiginKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public <T> T extractClaim(String token, Function<Claims, T> ClaimsResolvers) {
        final Claims claims = extractAllClaims(token);
        return ClaimsResolvers.apply(claims);
    }
public String extractUserName(String token) {
    return extractClaim(token, Claims::getSubject);
}
public  boolean isTokenValid(String token,UserDetails userDetails) {
    final String username = extractUserName(token);
    return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));

}

    public Object generateRefreshToken(Map<String,Object>extraClaims, User user) {
        return  Jwts.builder().setClaims(extraClaims).setSubject(user.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 604800000 ))
                .signWith(getSiginKey(), SignatureAlgorithm.HS256)
                .compact();
 }




    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }


    private Claims extractAllClaims(String token) {
    return  Jwts.parserBuilder().setSigningKey(getSiginKey()).build().parseClaimsJws(token).getBody();
    }

    private Key getSiginKey() {
        byte[]key = Decoders.BASE64.decode("1234567890123456789009876543210987654321234567890123456789012345678901234567890");
        return Keys.hmacShaKeyFor(key);
    }

}
