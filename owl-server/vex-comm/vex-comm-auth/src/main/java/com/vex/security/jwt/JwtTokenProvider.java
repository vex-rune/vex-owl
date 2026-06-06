package com.vex.security.jwt;

import com.vex.security.LoginUser;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    private final String secretKey;
    private final long accessTokenValidity;
    private final long refreshTokenValidity;
    private final String issuer;
    private KeyPair rsaKeyPair;

    private static final String CLAIM_TYPE = "type";
    private static final String CLAIM_CLIENT_ID = "client_id";
    private static final String CLAIM_SCOPES = "scope";
    private static final String TOKEN_TYPE_ACCESS = "access";
    private static final String TOKEN_TYPE_REFRESH = "refresh";

    public JwtTokenProvider() {
        this("defaultSecretKeyForDevEnvironmentOnly12345", 3600, 604800, "vex-owl");
    }

    public JwtTokenProvider(String secretKey, long accessTokenValidity, long refreshTokenValidity, String issuer) {
        this.secretKey = secretKey;
        this.accessTokenValidity = accessTokenValidity;
        this.refreshTokenValidity = refreshTokenValidity;
        this.issuer = issuer;
    }

    public VexToken generateByUser(LoginUser user) {
        return new VexToken(
                generateToken(user.getSubjectId(), user.toMap(), accessTokenValidity, TOKEN_TYPE_ACCESS),
                generateRefreshToken(user.getSubjectId()),
                accessTokenValidity,
                user.getEmail(),
                user.getNickName(),
                user.getRole()
        );
    }

    public String generateAccessToken(String subjectId, Map<String, Object> claims) {
        return generateToken(subjectId, claims, accessTokenValidity, TOKEN_TYPE_ACCESS);
    }

    public String generateRefreshToken(String subjectId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_TYPE, TOKEN_TYPE_REFRESH);
        return generateToken(subjectId, claims, refreshTokenValidity, TOKEN_TYPE_REFRESH);
    }

    public String generateToken(String subjectId, Map<String, Object> claims, long validitySeconds, String tokenType) {
        Instant now = Instant.now();
        Instant expiration = now.plus(validitySeconds, ChronoUnit.SECONDS);

        Map<String, Object> allClaims = new HashMap<>();
        if (claims != null) {
            allClaims.putAll(claims);
        }
        allClaims.put(CLAIM_TYPE, tokenType);
        allClaims.put("iss", issuer);
        allClaims.put("iat", now.getEpochSecond());
        allClaims.put("exp", expiration.getEpochSecond());
        allClaims.put("jti", UUID.randomUUID().toString());

        return Jwts.builder()
                .subject(subjectId)
                .claims(allClaims)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(getSigningKey())
                .compact();
    }

    public String generateRsaToken(String subjectId, Map<String, Object> claims, long validitySeconds, String tokenType) {
        if (rsaKeyPair == null) {
            generateRsaKeyPair();
        }

        Instant now = Instant.now();
        Instant expiration = now.plus(validitySeconds, ChronoUnit.SECONDS);

        Map<String, Object> allClaims = new HashMap<>();
        if (claims != null) {
            allClaims.putAll(claims);
        }
        allClaims.put(CLAIM_TYPE, tokenType);
        allClaims.put("iss", issuer);
        allClaims.put("iat", now.getEpochSecond());
        allClaims.put("exp", expiration.getEpochSecond());
        allClaims.put("jti", UUID.randomUUID().toString());

        return Jwts.builder()
                .subject(subjectId)
                .claims(allClaims)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(rsaKeyPair.getPrivate(), Jwts.SIG.RS256)
                .compact();
    }

    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.error("Token已过期: {}", e.getMessage());
            throw e;
        } catch (SignatureException e) {
            log.error("Token签名验证失败: {}", e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            log.error("Token格式错误: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Token解析失败: {}", e.getMessage());
            throw new JwtException("Token解析失败", e);
        }
    }

    public Claims parseRsaToken(String token) {
        if (rsaKeyPair == null) {
            generateRsaKeyPair();
        }

        try {
            return Jwts.parser()
                    .verifyWith(rsaKeyPair.getPublic())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.error("Token已过期: {}", e.getMessage());
            throw e;
        } catch (SignatureException e) {
            log.error("Token签名验证失败: {}", e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            log.error("Token格式错误: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Token解析失败: {}", e.getMessage());
            throw new JwtException("Token解析失败", e);
        }
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            log.error("Token验证失败: {}", e.getMessage());
            return false;
        }
    }

    public boolean validateRsaToken(String token) {
        try {
            parseRsaToken(token);
            return true;
        } catch (Exception e) {
            log.error("RSA Token验证失败: {}", e.getMessage());
            return false;
        }
    }

    public String getSubjectFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    public String getSubjectFromRsaToken(String token) {
        Claims claims = parseRsaToken(token);
        return claims.getSubject();
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getClaimsFromToken(String token) {
        Claims claims = parseToken(token);
        return new HashMap<>(claims);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getClaimsFromRsaToken(String token) {
        Claims claims = parseRsaToken(token);
        return new HashMap<>(claims);
    }

    public Date getExpirationFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getExpiration();
    }

    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationFromToken(token);
            return expiration != null && expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public String getTokenType(String token) {
        Claims claims = parseToken(token);
        return claims.get(CLAIM_TYPE, String.class);
    }

    public String getTokenId(String token) {
        Claims claims = parseToken(token);
        return claims.get("jti", String.class);
    }

    public String getClientId(String token) {
        Claims claims = parseToken(token);
        return claims.get(CLAIM_CLIENT_ID, String.class);
    }

    public Set<String> getScopes(String token) {
        Claims claims = parseToken(token);
        String scope = claims.get(CLAIM_SCOPES, String.class);
        if (scope != null && !scope.isEmpty()) {
            return new HashSet<>(Arrays.asList(scope.split("\\s+")));
        }
        return Collections.emptySet();
    }

    public void generateRsaKeyPair() {
        this.rsaKeyPair = Keys.keyPairFor(SignatureAlgorithm.RS256);
    }

    public void setRsaKeyPair(KeyPair keyPair) {
        this.rsaKeyPair = keyPair;
    }

    public KeyPair getRsaKeyPair() {
        if (rsaKeyPair == null) {
            generateRsaKeyPair();
        }
        return rsaKeyPair;
    }

    public String getPublicKeyString() {
        if (rsaKeyPair == null) {
            generateRsaKeyPair();
        }
        return Base64.getEncoder().encodeToString(rsaKeyPair.getPublic().getEncoded());
    }

    public String getPrivateKeyString() {
        if (rsaKeyPair == null) {
            generateRsaKeyPair();
        }
        return Base64.getEncoder().encodeToString(rsaKeyPair.getPrivate().getEncoded());
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            byte[] paddedKey = new byte[32];
            System.arraycopy(keyBytes, 0, paddedKey, 0, keyBytes.length);
            keyBytes = paddedKey;
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public long getAccessTokenValidity() {
        return accessTokenValidity;
    }

    public long getRefreshTokenValidity() {
        return refreshTokenValidity;
    }

    public String getIssuer() {
        return issuer;
    }

    String getSecretKeyForTest() {
        return secretKey;
    }
}
