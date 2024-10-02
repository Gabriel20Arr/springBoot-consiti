package com.apirest.apirestfull.security.jwt;

import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import com.apirest.apirestfull.security.dto.JwtDto;
import com.apirest.apirestfull.security.entity.UsuarioPrincipal;
import com.nimbusds.jwt.*;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Component
public class JwtProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtProvider.class);

    // Obtenemos el secret y el expiration desde el application.properties
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private int expiration;

    // Método para generar el token JWT
    public String generateToken(Authentication authentication) {
        UsuarioPrincipal usuarioPrincipal = (UsuarioPrincipal) authentication.getPrincipal();
        List<String> roles= usuarioPrincipal.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
        // Creamos el token usando la API de jjwt
        return Jwts.builder()
                .setSubject(usuarioPrincipal.getUsername()) // Establecer el nombre de usuario en el token
                .claim("roles", roles)
                .setIssuedAt(new Date()) // Fecha de creación del token
                .setExpiration(new Date(new Date().getTime() + expiration)) // Fecha de expiración
                .signWith(SignatureAlgorithm.HS512, secret.getBytes()) // Firmar el token con el algoritmo HS512 y la clave secreta
                .compact(); // Compactar el JWT
    }

    // Método para obtener el username (subject) del token JWT
    public String getNombreUsuarioFromToken(String token) {
        return Jwts.parser().setSigningKey(secret.getBytes()).parseClaimsJws(token).getBody().getSubject();
    }

    // Método para validar el token JWT
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secret.getBytes()).parseClaimsJws(token);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Token malformado");
        } catch (UnsupportedJwtException e) {
            logger.error("Token no soportado");
        } catch (ExpiredJwtException e) {
            logger.error("Token expirado");
        } catch (IllegalArgumentException e) {
            logger.error("Token vacío");
        } catch (SignatureException e) {
            logger.error("Fallo en la firma del token");
        }
        return false;
    }

    public String generateRefreshToken(JwtDto jwtDto) throws ParseException {

        try {
            Jwts.parser().setSigningKey(secret.getBytes()).parseClaimsJws(jwtDto.getToken());
        } catch (ExpiredJwtException e) {
            JWT jwt = JWTParser.parse(jwtDto.getToken());
            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            String nombreUsuario = claims.getSubject();
            List<String> roles = (List<String>)claims.getClaim("roles");

            return Jwts.builder()
                .setSubject(nombreUsuario)
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() + expiration)) // Usa refreshExpiration
                .signWith(SignatureAlgorithm.HS512, secret.getBytes())
                .compact();
        }
        return null;
    }
}

