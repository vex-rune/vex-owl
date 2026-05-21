package com.vex.security.jwt;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtTokenProvider Spring Security集成测试")
class JwtTokenProviderSpringTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(
                "testSecretKeyForUnitTesting123456789",
                3600,
                604800,
                "test-issuer"
        );
    }

    @Test
    @DisplayName("测试生成访问令牌")
    void testGenerateAccessToken() {
        String subjectId = "user123";
        Map<String, Object> claims = Map.of("name", "Test User", "role", "admin");

        String token = jwtTokenProvider.generateAccessToken(subjectId, claims);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3);
    }

    @Test
    @DisplayName("测试生成刷新令牌")
    void testGenerateRefreshToken() {
        String subjectId = "user123";

        String token = jwtTokenProvider.generateRefreshToken(subjectId);

        assertNotNull(token);
        assertEquals("refresh", jwtTokenProvider.getTokenType(token));
    }

    @Test
    @DisplayName("测试解析有效令牌")
    void testParseValidToken() {
        String subjectId = "user456";
        Map<String, Object> claims = Map.of("name", "解析测试");

        String token = jwtTokenProvider.generateAccessToken(subjectId, claims);
        Claims parsedClaims = jwtTokenProvider.parseToken(token);

        assertNotNull(parsedClaims);
        assertEquals(subjectId, parsedClaims.getSubject());
        assertEquals("解析测试", parsedClaims.get("name", String.class));
    }

    @Test
    @DisplayName("测试验证有效令牌")
    void testValidateValidToken() {
        String token = jwtTokenProvider.generateAccessToken("user123", null);

        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    @DisplayName("测试验证无效令牌")
    void testValidateInvalidToken() {
        String invalidToken = "invalid.token.here";

        assertFalse(jwtTokenProvider.validateToken(invalidToken));
    }

    @Test
    @DisplayName("测试提取Scopes")
    void testGetScopes() {
        String subjectId = "scope_user";
        Map<String, Object> claims = Map.of("scope", "read write delete");

        String token = jwtTokenProvider.generateAccessToken(subjectId, claims);
        Set<String> scopes = jwtTokenProvider.getScopes(token);

        assertNotNull(scopes);
        assertEquals(3, scopes.size());
        assertTrue(scopes.contains("read"));
        assertTrue(scopes.contains("write"));
        assertTrue(scopes.contains("delete"));
    }

    @Test
    @DisplayName("测试提取ClientId")
    void testGetClientId() {
        String subjectId = "client_user";
        String clientId = "my-client-app";
        Map<String, Object> claims = Map.of("client_id", clientId);

        String token = jwtTokenProvider.generateAccessToken(subjectId, claims);
        String extractedClientId = jwtTokenProvider.getClientId(token);

        assertEquals(clientId, extractedClientId);
    }

    @Test
    @DisplayName("测试令牌过期检测")
    void testIsTokenExpired() {
        String token = jwtTokenProvider.generateAccessToken("user123", null);

        assertFalse(jwtTokenProvider.isTokenExpired(token));
    }

    @Test
    @DisplayName("测试RSA令牌生成")
    void testGenerateRsaToken() {
        String subjectId = "rsa_user";
        Map<String, Object> claims = Map.of("name", "RSA User");

        String rsaToken = jwtTokenProvider.generateRsaToken(subjectId, claims, 3600, "rsa_access");

        assertNotNull(rsaToken);
        assertTrue(jwtTokenProvider.validateRsaToken(rsaToken));
    }

    @Test
    @DisplayName("测试RSA令牌解析")
    void testParseRsaToken() {
        String subjectId = "rsa_parse_user";
        Map<String, Object> claims = Map.of("department", "IT");

        String rsaToken = jwtTokenProvider.generateRsaToken(subjectId, claims, 3600, "rsa_access");
        Claims parsedClaims = jwtTokenProvider.parseRsaToken(rsaToken);

        assertNotNull(parsedClaims);
        assertEquals(subjectId, parsedClaims.getSubject());
        assertEquals("IT", parsedClaims.get("department", String.class));
    }

    @Test
    @DisplayName("测试RSA公钥获取")
    void testGetPublicKeyString() {
        String publicKey = jwtTokenProvider.getPublicKeyString();

        assertNotNull(publicKey);
        assertFalse(publicKey.isEmpty());
    }

    @Test
    @DisplayName("测试RSA私钥获取")
    void testGetPrivateKeyString() {
        String privateKey = jwtTokenProvider.getPrivateKeyString();

        assertNotNull(privateKey);
        assertFalse(privateKey.isEmpty());
    }

    @Test
    @DisplayName("测试令牌ID生成")
    void testGetTokenId() {
        String token = jwtTokenProvider.generateAccessToken("user123", null);
        String tokenId = jwtTokenProvider.getTokenId(token);

        assertNotNull(tokenId);
        assertFalse(tokenId.isEmpty());
    }

    @Test
    @DisplayName("测试不同Issuer")
    void testDifferentIssuers() {
        JwtTokenProvider provider1 = new JwtTokenProvider("secret1", 3600, 604800, "issuer1");
        JwtTokenProvider provider2 = new JwtTokenProvider("secret2", 3600, 604800, "issuer2");

        String token1 = provider1.generateAccessToken("user1", null);
        String token2 = provider2.generateAccessToken("user2", null);

        assertTrue(provider1.validateToken(token1));
        assertTrue(provider2.validateToken(token2));
    }

    @Test
    @DisplayName("测试令牌唯一性")
    void testTokenUniqueness() {
        String token1 = jwtTokenProvider.generateAccessToken("user123", null);
        String token2 = jwtTokenProvider.generateAccessToken("user123", null);

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
        Map<String, Object> claims = Map.of(
                "customClaim1", "value1",
                "customClaim2", 100
        );

        String token = jwtTokenProvider.generateAccessToken(subjectId, claims);
        Map<String, Object> allClaims = jwtTokenProvider.getClaimsFromToken(token);

        assertNotNull(allClaims);
        assertTrue(allClaims.containsKey("type"));
        assertTrue(allClaims.containsKey("iss"));
        assertTrue(allClaims.containsKey("iat"));
        assertTrue(allClaims.containsKey("exp"));
        assertTrue(allClaims.containsKey("jti"));
        assertTrue(allClaims.containsKey("customClaim1"));
        assertTrue(allClaims.containsKey("customClaim2"));
    }

    @Test
    @DisplayName("测试自定义有效期")
    void testCustomValidity() {
        long customValidity = 7200L;
        String subjectId = "custom_validity_user";
        Map<String, Object> claims = Map.of("type", "custom");

        String token = jwtTokenProvider.generateToken(subjectId, claims, customValidity, "custom_token");

        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    @DisplayName("测试解析篡改令牌")
    void testParseTamperedToken() {
        String validToken = jwtTokenProvider.generateAccessToken("user123", null);
        String tamperedToken = validToken.substring(0, validToken.length() - 5) + "xxxxx";

        assertFalse(jwtProvider.validateToken(tamperedToken));
    }

    @Test
    @DisplayName("测试使用不同密钥验证失败")
    void testValidateWithDifferentKey() {
        String token = jwtTokenProvider.generateAccessToken("user123", null);

        JwtTokenProvider differentKeyProvider = new JwtTokenProvider("differentSecretKey12345678901234", 3600, 604800, "test");

        assertFalse(differentKeyProvider.validateToken(token));
    }

    private JwtTokenProvider jwtProvider = new JwtTokenProvider();
}
