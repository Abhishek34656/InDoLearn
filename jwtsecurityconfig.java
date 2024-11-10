import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class JwtSecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${jwt.secret}")
    private String secretKey;

    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 10; // 10 hours

    // JWT Utility Methods
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", username);
        claims.put("created", new Date());
        return createToken(claims);
    }

    private String createToken(Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
    }

    public Claims extractClaims(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    public boolean validateToken(String token, String username) {
        return (username.equals(extractUsername(token)) && !isTokenExpired(token));
    }

    // JWT Authentication Filter
    public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
        private final JwtSecurityConfig jwtSecurityConfig;

        public JwtAuthenticationFilter(JwtSecurityConfig jwtSecurityConfig) {
            this.jwtSecurityConfig = jwtSecurityConfig;
        }

        @Override
        protected void doFilterInternal(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response, javax.servlet.FilterChain filterChain)
                throws javax.servlet.ServletException, java.io.IOException {

            final String token = request.getHeader("Authorization");
            String username = null;

            if (token != null && token.startsWith("Bearer ")) {
                username = jwtSecurityConfig.extractUsername(token.substring(7));
            }

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (jwtSecurityConfig.validateToken(token, username)) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(username, null, new java.util.ArrayList<>());
                    authentication.setDetails(new org.springframework.security.web.authentication.WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
            filterChain.doFilter(request, response);
        }
    }

    // Spring Security Configuration
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeRequests()
                .antMatchers("/auth/login", "/auth/signup").permitAll()
                .anyRequest().authenticated();
        http.addFilterBefore(new JwtAuthenticationFilter(this), UsernamePasswordAuthenticationFilter.class);
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    // Controller for Authentication
    @RestController
    @RequestMapping("/auth")
    public static class AuthController {

        private final AuthenticationManager authenticationManager;
        private final JwtSecurityConfig jwtSecurityConfig;

        public AuthController(AuthenticationManager authenticationManager, JwtSecurityConfig jwtSecurityConfig) {
            this.authenticationManager = authenticationManager;
            this.jwtSecurityConfig = jwtSecurityConfig;
        }

        @PostMapping("/login")
        public String login(@RequestBody AuthRequest authRequest) {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));
            return jwtSecurityConfig.generateToken(authRequest.getUsername());
        }

        public static class AuthRequest {
            private String username;
            private String password;

            // Getters and Setters
        }
    }
}
