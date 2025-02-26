package itmo.andrey.lab_backend.util;

import io.jsonwebtoken.*;
import io.valkey.Jedis;
import io.valkey.JedisPool;
import io.valkey.JedisPoolConfig;
import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtTokenUtil {

    private JedisPool jedisPool;
    private String secretKey;

    @Value("${valkey.io.host}")
    private String valkeyHost;

    @Value("${valkey.io.port}")
    private int valkeyPort;

    @Value("${jwt.expiration}")
    private int jwtExpiration;

    public JwtTokenUtil() {
    }

    @PostConstruct
    public void init() {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(32);
        config.setMaxIdle(32);
        config.setMinIdle(16);
        this.jedisPool = new JedisPool(config, valkeyHost, valkeyPort, 2000);
        loadSecretKey();
    }

    @SneakyThrows
    private void loadSecretKey() {
        try (Jedis jedis = jedisPool.getResource()) {
            String secretKeyString = jedis.get("privateKey");
            if (secretKeyString == null) {
                throw new IllegalStateException("Secret key not found in valkey");
            }
            this.secretKey = secretKeyString;
        }
    }

    public String generateJwtToken(String name) {
        try {
            return Jwts.builder()
                    .setSubject(name)
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                    .signWith(SignatureAlgorithm.HS256, secretKey)
                    .compact();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public boolean validateJwtToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token);
            return true;
        } catch (MalformedJwtException | ExpiredJwtException | UnsupportedJwtException | IllegalArgumentException | io.jsonwebtoken.io.DecodingException ex) {
            return false;
        }
    }

    public String getNameFromJwtToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (Exception e) {
            return null;
        }
    }
}