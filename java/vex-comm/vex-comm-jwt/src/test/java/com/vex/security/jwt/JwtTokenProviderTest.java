package com.vex.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtTokenProvider单元测试")
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private JwtTokenProvider jwtTokenProviderWithCustomConfig;

    private static final String SECRET_KEY = "testSecretKeyForUnitTesting123456789";
    private static final String ISSUER = "test-issuer";
    private static final long ACCESS_TOKEN_VALIDITY = 3600L;
    private static final long REFRESH_TOKEN_VALIDITY = 604800L;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        jwtTokenProviderWithCustomConfig = new JwtTokenProvider(SECRET_KEY, ACCESS_TOKEN_VALIDITY, REFRESH_TOKEN_VALIDITY, ISSUER);
    }

    @Test
    @DisplayName("测试默认构造函数")
    void testDefaultConstructor() {
        assertNotNull(jwtTokenProvider);
        assertEquals(3600, jwtTokenProvider.getAccessTokenValidity());
        assertEquals(604800, jwtTokenProvider.getRefreshTokenValidity());
        assertEquals("vex-owl", jwtTokenProvider.getIssuer());
    }

    @Test
    @DisplayName("测试自定义构造函数")
    void testCustomConstructor() {
        assertEquals(SECRET_KEY, jwtTokenProviderWithCustomConfig.getSecretKeyForTest());
        assertEquals(ACCESS_TOKEN_VALIDITY, jwtTokenProviderWithCustomConfig.getAccessTokenValidity());
        assertEquals(REFRESH_TOKEN_VALIDITY, jwtTokenProviderWithCustomConfig.getRefreshTokenValidity());
        assertEquals(ISSUER, jwtTokenProviderWithCustomConfig.getIssuer());
    }

    @Test
    @DisplayName("测试生成访问令牌")
    void testGenerateAccessToken() {
        String subjectId = "user123";
        Map<String, Object> claims = new HashMap<>();
        claims.put("name", "Test User");
        claims.put("role", "admin");

        String token = jwtTokenProviderWithCustomConfig.generateAccessToken(subjectId, claims);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3);
    }

    @Test
    @DisplayName("测试生成刷新令牌")
    void testGenerateRefreshToken() {
        String subjectId = "user123";

        String token = jwtTokenProviderWithCustomConfig.generateRefreshToken(subjectId);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3);
    }

    @Test
    @DisplayName("测试解析有效令牌")
    void testParseValidToken() {
        String subjectId = "user123";
        Map<String, Object> claims = new HashMap<>();
        claims.put("name", "Test User");

        String token = jwtTokenProviderWithCustomConfig.generateAccessToken(subjectId, claims);
        Claims parsedClaims = jwtTokenProviderWithCustomConfig.parseToken(token);

        assertNotNull(parsedClaims);
        assertEquals(subjectId, parsedClaims.getSubject());
        assertEquals("Test User", parsedClaims.get("name", String.class));
        assertEquals("access", parsedClaims.get("type", String.class));
        assertEquals(ISSUER, parsedClaims.getIssuer());
    }

    @Test
    @DisplayName("测试验证有效令牌")
    void testValidateValidToken() {
        String token = jwtTokenProviderWithCustomConfig.generateAccessToken("user123", null);

        assertTrue(jwtTokenProviderWithCustomConfig.validateToken(token));
    }

    @Test
    @DisplayName("测试验证无效令牌")
    void testValidateInvalidToken() {
        String invalidToken = "invalid.token.here";

        assertFalse(jwtTokenProviderWithCustomConfig.validateToken(invalidToken));
    }

    @Test
    @DisplayName("测试从令牌获取主题")
    void testGetSubjectFromToken() {
        String subjectId = "user456";
        String token = jwtTokenProviderWithCustomConfig.generateAccessToken(subjectId, null);

        String extractedSubject = jwtTokenProviderWithCustomConfig.getSubjectFromToken(token);

        assertEquals(subjectId, extractedSubject);
    }

    @Test
    @DisplayName("测试从令牌获取声明")
    void testGetClaimsFromToken() {
        String subjectId = "user789";
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", "test@example.com");
        claims.put("age", 25);

        String token = jwtTokenProviderWithCustomConfig.generateAccessToken(subjectId, claims);
        Map<String, Object> extractedClaims = jwtTokenProviderWithCustomConfig.getClaimsFromToken(token);

        assertNotNull(extractedClaims);
        assertEquals("test@example.com", extractedClaims.get("email"));
        assertEquals(25, ((Number) extractedClaims.get("age")).intValue());
    }

    @Test
    @DisplayName("测试令牌过期检测")
    void testIsTokenExpired() {
        String token = jwtTokenProviderWithCustomConfig.generateAccessToken("user123", null);

        assertFalse(jwtTokenProviderWithCustomConfig.isTokenExpired(token));
    }

    @Test
    @DisplayName("测试获取令牌类型")
    void testGetTokenType() {
        String accessToken = jwtTokenProviderWithCustomConfig.generateAccessToken("user123", null);
        String refreshToken = jwtTokenProviderWithCustomConfig.generateRefreshToken("user123");

        assertEquals("access", jwtTokenProviderWithCustomConfig.getTokenType(accessToken));
        assertEquals("refresh", jwtTokenProviderWithCustomConfig.getTokenType(refreshToken));
    }

    @Test
    @DisplayName("测试获取令牌过期时间")
    void testGetExpirationFromToken() {
        String token = jwtTokenProviderWithCustomConfig.generateAccessToken("user123", null);
        Date expiration = jwtTokenProviderWithCustomConfig.getExpirationFromToken(token);

        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    @DisplayName("测试生成自定义有效期令牌")
    void testGenerateTokenWithCustomValidity() {
        String subjectId = "user123";
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "custom");
        long customValidity = 7200L;

        String token = jwtTokenProviderWithCustomConfig.generateToken(subjectId, claims, customValidity, "custom");

        assertNotNull(token);
        Date expiration = jwtTokenProviderWithCustomConfig.getExpirationFromToken(token);
        long actualValidity = (expiration.getTime() - System.currentTimeMillis()) / 1000;
        assertTrue(Math.abs(actualValidity - customValidity) <= 5);
    }

    @Test
    @DisplayName("测试RSA令牌生成")
    void testGenerateRsaToken() {
        String subjectId = "user123";
        Map<String, Object> claims = new HashMap<>();
        claims.put("name", "RSA User");

        String rsaToken = jwtTokenProviderWithCustomConfig.generateRsaToken(subjectId, claims, 3600, "rsa_access");

        assertNotNull(rsaToken);
        assertTrue(jwtTokenProviderWithCustomConfig.validateRsaToken(rsaToken));
    }

    @Test
    @DisplayName("测试RSA令牌解析")
    void testParseRsaToken() {
        String subjectId = "rsa_user";
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", "rsa@test.com");

        String rsaToken = jwtTokenProviderWithCustomConfig.generateRsaToken(subjectId, claims, 3600, "rsa_access");
        Claims parsedClaims = jwtTokenProviderWithCustomConfig.parseRsaToken(rsaToken);

        assertNotNull(parsedClaims);
        assertEquals(subjectId, parsedClaims.getSubject());
        assertEquals("rsa@test.com", parsedClaims.get("email", String.class));
    }

    @Test
    @DisplayName("测试RSA公钥获取")
    void testGetPublicKeyString() {
        String publicKey = jwtTokenProviderWithCustomConfig.getPublicKeyString();

        assertNotNull(publicKey);
        assertFalse(publicKey.isEmpty());
    }

    @Test
    @DisplayName("测试RSA私钥获取")
    void testGetPrivateKeyString() {
        String privateKey = jwtTokenProviderWithCustomConfig.getPrivateKeyString();

        assertNotNull(privateKey);
        assertFalse(privateKey.isEmpty());
    }

    @Test
    @DisplayName("测试令牌ID生成")
    void testGetTokenId() {
        String token = jwtTokenProviderWithCustomConfig.generateAccessToken("user123", null);
        String tokenId = jwtTokenProviderWithCustomConfig.getTokenId(token);

        assertNotNull(tokenId);
        assertFalse(tokenId.isEmpty());
    }

    @Test
    @DisplayName("测试解析篡改令牌")
    void testParseTamperedToken() {
        String validToken = jwtTokenProviderWithCustomConfig.generateAccessToken("user123", null);
        String tamperedToken = validToken.substring(0, validToken.length() - 5) + "xxxxx";

        assertFalse(jwtTokenProviderWithCustomConfig.validateToken(tamperedToken));
    }

    @Test
    @DisplayName("测试使用不同密钥验证令牌失败")
    void testValidateTokenWithDifferentKey() {
        String token = jwtTokenProviderWithCustomConfig.generateAccessToken("user123", null);

        JwtTokenProvider differentKeyProvider = new JwtTokenProvider("differentSecretKey12345678901234", 3600, 604800, "test");

        assertFalse(differentKeyProvider.validateToken(token));
    }

    @Test
    @DisplayName("测试令牌唯一性")
    void testTokenUniqueness() {
        String token1 = jwtTokenProviderWithCustomConfig.generateAccessToken("user123", null);
        String token2 = jwtTokenProviderWithCustomConfig.generateAccessToken("user123", null);

        assertNotEquals(token1, token2);
    }

    @Test
    @DisplayName("测试短密钥自动填充")
    void testShortSecretKeyPadding() {
        JwtTokenProvider shortKeyProvider = new JwtTokenProvider("short", 3600, 604800, "test");

        String token = shortKeyProvider.generateAccessToken("user123", null);

        assertTrue(shortKeyProvider.validateToken(token));
    }

    @Test
    @DisplayName("测试获取所有声明")
    void testGetAllClaims() {
        String subjectId = "claims_test_user";
        Map<String, Object> claims = new HashMap<>();
        claims.put("customClaim1", "value1");
        claims.put("customClaim2", 100);

        String token = jwtTokenProviderWithCustomConfig.generateAccessToken(subjectId, claims);
        Map<String, Object> allClaims = jwtTokenProviderWithCustomConfig.getClaimsFromToken(token);

        assertNotNull(allClaims);
        assertTrue(allClaims.containsKey("type"));
        assertTrue(allClaims.containsKey("iss"));
        assertTrue(allClaims.containsKey("iat"));
        assertTrue(allClaims.containsKey("exp"));
        assertTrue(allClaims.containsKey("jti"));
        assertTrue(allClaims.containsKey("customClaim1"));
        assertTrue(allClaims.containsKey("customClaim2"));
    }
}
