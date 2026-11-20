package com.eman.tracker.olympicnutritiontracker.service;

import com.eman.tracker.olympicnutritiontracker.model.Athlete;
import com.eman.tracker.olympicnutritiontracker.model.Consultation;
import com.eman.tracker.olympicnutritiontracker.model.NutritionEntry;
import com.eman.tracker.olympicnutritiontracker.model.User;
import com.eman.tracker.olympicnutritiontracker.repository.UserRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
class RecordAccess {

    private static final Scope NO_ACCESS = new Scope(false, null, null);

    private final UserRepository users;

    RecordAccess(UserRepository users) {
        this.users = users;
    }

    Scope current() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return NO_ACCESS;
        }
        if (auth.getAuthorities().stream().anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()))) {
            return new Scope(true, null, null);
        }
        return users.findByUsername(auth.getName()).map(RecordAccess::scopeFor).orElse(NO_ACCESS);
    }

    private static Scope scopeFor(User user) {
        return switch (user.getRole()) {
            case COACH -> new Scope(false, null, user.getCoachProfileId());
            case ATHLETE -> new Scope(false, user.getAthleteProfileId(), null);
            case ADMIN -> NO_ACCESS;
        };
    }

    record Scope(boolean admin, Long athleteId, Long coachId) {

        Specification<Athlete> athletes() {
            return (root, query, builder) -> athletePredicate(root, builder);
        }

        Specification<NutritionEntry> nutritionEntries() {
            return (root, query, builder) -> athletePredicate(root.get("athlete"), builder);
        }

        Specification<Consultation> consultations() {
            return (root, query, builder) -> {
                Predicate athlete = athletePredicate(root.get("athlete"), builder);
                return coachId == null ? athlete : builder.and(athlete,
                        builder.equal(root.get("coach").get("id"), coachId));
            };
        }

        boolean canWriteFor(Athlete athlete) {
            return admin || (coachId != null && athlete.getCoach() != null
                    && coachId.equals(athlete.getCoach().getId()));
        }

        boolean canWriteAs(Long consultationCoachId) {
            return admin || (coachId != null && coachId.equals(consultationCoachId));
        }

        private Predicate athletePredicate(Path<Athlete> athlete, CriteriaBuilder builder) {
            if (admin) {
                return builder.conjunction();
            }
            if (coachId != null) {
                return builder.equal(athlete.get("coach").get("id"), coachId);
            }
            if (athleteId != null) {
                return builder.equal(athlete.get("id"), athleteId);
            }
            return builder.disjunction();
        }
    }
}
