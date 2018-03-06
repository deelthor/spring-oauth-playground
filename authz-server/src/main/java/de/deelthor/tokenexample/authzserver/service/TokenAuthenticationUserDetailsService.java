package de.deelthor.tokenexample.authzserver.service;

import com.auth0.jwt.exceptions.InvalidClaimException;
import com.auth0.jwt.interfaces.DecodedJWT;
import de.deelthor.tokenexample.authzserver.TokenUserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class TokenAuthenticationUserDetailsService implements AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> {
    private TokenService tokenService;

    public TokenAuthenticationUserDetailsService(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public UserDetails loadUserDetails(PreAuthenticatedAuthenticationToken authentication) throws UsernameNotFoundException {
        if (authentication.getPrincipal() != null && authentication.getPrincipal() instanceof String && authentication.getCredentials() instanceof String) {
            DecodedJWT token;
            try {
                token = tokenService.decode((String) authentication.getPrincipal());
            } catch (InvalidClaimException ex) {
                throw new UsernameNotFoundException("Token has been expired", ex);
            }
            return new TokenUserDetails(token.getSubject(), token.getClaim("usr").asString(), (String) authentication.getCredentials(), token.getToken(), true, token
                    .getClaim("role")
                    .asList(String.class)
                    .stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList()));
        } else {
            throw new UsernameNotFoundException("Could not retrieve user details for '" + authentication.getPrincipal() + "'");
        }
    }
}