package itmo.andrey.lab_backend.util;

import io.jsonwebtoken.*;
import io.valkey.Jedis;
import io.valkey.JedisPool;
import io.valkey.JedisPoolConfig;
import itmo.andrey.lab_backend.service.KeyService;
import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.PrivateKey;
import java.util.Date;

@Component
public class JwtTokenUtil {

    private final KeyService keyService;
    private JedisPool jedisPool;
    private PrivateKey privateKey;

    @Value("${valkey.io.host}")
    private String valkeyHost;

    @Value("${valkey.io.port}")
    private int valkeyPort;

    @Value("${valkey.io.password}")
    private String valkeyPassword;

    @Value("${jwt.expiration}")
    private int jwtExpiration;

    public JwtTokenUtil(KeyService keyService) {
        this.keyService = keyService;
    }

    @PostConstruct
    public void init() {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(32);
        config.setMaxIdle(32);
        config.setMinIdle(16);
        this.jedisPool = new JedisPool(config, valkeyHost, valkeyPort, 2000, valkeyPassword);
        loadPrivateKey();
    }

    @SneakyThrows
    private void loadPrivateKey() {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.auth(valkeyPassword);
            String privateKeyString = jedis.get("privateKey");
            if (privateKeyString == null) {
                throw new IllegalStateException("Private key not found in Redis");
            }
            this.privateKey = keyService.convertStringToPrivateKey(privateKeyString);
        }
    }

    public String generateJwtToken(String name) {
        try {
            return Jwts.builder()
                    .setSubject(name)
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                    .signWith(SignatureAlgorithm.RS256, privateKey)
                    .compact();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean validateJwtToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(privateKey)
                    .parseClaimsJws(token);
            return true;
        } catch (MalformedJwtException | ExpiredJwtException | UnsupportedJwtException | IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public String getNameFromJwtToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(privateKey)
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}