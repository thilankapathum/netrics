package dev.thilanka.netrics.config;

import dev.thilanka.netrics.entity.User;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

public class ApplicationAuditAware implements AuditorAware<String> {
    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()){
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof Jwt jwt){
            return Optional.ofNullable(jwt.getClaimAsString("sub"));
        }
        return Optional.empty();
    }
//    @Override
//    public Optional<Long> getCurrentAuditor() {
//        //-- Get current authentication from Security Context Holder of this session
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//
//        //-- Check whether properly authenticated
//        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken){
//            return Optional.empty();
//        }
//
//        User userPrincipal = (User) authentication.getPrincipal();
//
//        //-- Return authenticated user's ID
//        return Optional.ofNullable(userPrincipal.getId());
//    }
}
