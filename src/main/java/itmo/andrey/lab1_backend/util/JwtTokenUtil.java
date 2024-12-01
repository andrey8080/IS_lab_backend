package itmo.andrey.lab1_backend.util;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtTokenUtil {

	@Value("${jwt.secret}")
	private String jwtSecret;

	@Value("${jwt.expiration}")
	private Long jwtExpiration;

	public String generateJwtToken(String name) {
		return Jwts.builder()
				.setSubject(name)
				.setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
				.signWith(SignatureAlgorithm.HS512, jwtSecret)
				.compact();
	}


	public boolean validateJwtToken(String token) {
		try {
			Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
			return true;
		} catch (MalformedJwtException | ExpiredJwtException | UnsupportedJwtException | IllegalArgumentException ex) {
			ex.printStackTrace();
		}
		return false;
	}

	public String getNameFromJwtToken(String token) {
		try {
			String name = Jwts.parser()
					.setSigningKey(jwtSecret)
					.parseClaimsJws(token)
					.getBody()
					.getSubject();
			return name;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
