package pl.xsware.infrastructure.security.audit;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("currentUserAuditorAware")
public class CurrentUserAuditorAware implements AuditorAware<Long> {

    @Override
    public Optional<Long> getCurrentAuditor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getDetails() == null) return Optional.empty();

        try {
            return Optional.of(Long.parseLong(auth.getDetails().toString()));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}