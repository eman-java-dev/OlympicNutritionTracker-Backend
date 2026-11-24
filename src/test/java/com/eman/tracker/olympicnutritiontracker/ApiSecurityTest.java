package com.eman.tracker.olympicnutritiontracker;

import com.eman.tracker.olympicnutritiontracker.config.JwtService;
import com.eman.tracker.olympicnutritiontracker.model.Athlete;
import com.eman.tracker.olympicnutritiontracker.model.Coach;
import com.eman.tracker.olympicnutritiontracker.model.Consultation;
import com.eman.tracker.olympicnutritiontracker.model.NutritionEntry;
import com.eman.tracker.olympicnutritiontracker.model.Role;
import com.eman.tracker.olympicnutritiontracker.model.User;
import com.eman.tracker.olympicnutritiontracker.repository.AthleteRepository;
import com.eman.tracker.olympicnutritiontracker.repository.CoachRepository;
import com.eman.tracker.olympicnutritiontracker.repository.ConsultationRepository;
import com.eman.tracker.olympicnutritiontracker.repository.NutritionEntryRepository;
import com.eman.tracker.olympicnutritiontracker.repository.UserRepository;
import com.eman.tracker.olympicnutritiontracker.service.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.MockMvcPrint;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@BackendIntegrationTest
@AutoConfigureMockMvc(print = MockMvcPrint.NONE)
@Transactional
class ApiSecurityTest {

    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired CoachRepository coaches;
    @Autowired AthleteRepository athletes;
    @Autowired NutritionEntryRepository entries;
    @Autowired ConsultationRepository consultations;
    @Autowired CustomUserDetailsService userDetails;
    @Autowired PasswordEncoder encoder;
    @Autowired JwtService jwt;

    private final String testPassword = UUID.randomUUID().toString();

    @Test
    void passwordPolicyAppliesOnlyToNewAccountsAndCountsUnicodeCodePoints() throws Exception {
        String legacy = "legacy";
        users.saveAndFlush(User.builder().username("old-policy").password(encoder.encode(legacy))
                .role(Role.ATHLETE).build());
        authRequest("login", "old-policy", legacy).andExpect(status().isOk());

        String emoji = new String(Character.toChars(0x1f3c5));
        authRequest("register", "short-policy", emoji.repeat(14)).andExpect(status().isBadRequest());
        authRequest("register", "too-many-bytes", emoji.repeat(18) + "x").andExpect(status().isBadRequest());
        authRequest("register", "exact-minimum", emoji.repeat(15)).andExpect(status().isOk());
        authRequest("register", "exact-byte-limit", emoji.repeat(18)).andExpect(status().isOk());
        assertThat(users.existsByUsername("short-policy")).isFalse();
        assertThat(users.existsByUsername("too-many-bytes")).isFalse();
    }

    @Test
    void forwardedHeadersCannotResetAccountBudget() throws Exception {
        for (int attempt = 0; attempt < 11; attempt++) {
            var result = mvc.perform(post("/api/auth/login")
                    .header("Forwarded", "for=192.0.2." + attempt)
                    .header("X-Forwarded-For", "198.51.100." + attempt + ", 203.0.113.1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {"username":"throttle-check","password":"incorrect"}
                            """));
            if (attempt < 10) {
                result.andExpect(status().isUnauthorized());
            } else {
                result.andExpect(status().isTooManyRequests())
                        .andExpect(header().exists(HttpHeaders.RETRY_AFTER));
            }
        }
    }

    @Test
    void healthAliasesAreAvailableWithoutAuthentication() throws Exception {
        mvc.perform(get("/health")).andExpect(status().isOk()).andExpect(content().string(""));
        mvc.perform(get("/healthz")).andExpect(status().isOk()).andExpect(content().string(""));
        mvc.perform(post("/healthz")).andExpect(status().isUnauthorized());
    }

    @Test
    void invalidWritesLeaveDataUnchangedAndOptionalNutritionFieldsRemainOptional() throws Exception {
        account("validation-admin", Role.ADMIN, null, null);
        String token = bearer("validation-admin");
        long athleteCount = athletes.count();
        long entryCount = entries.count();
        mvc.perform(post("/api/athletes").header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":" ", "age":0, "gender":"12345678901","height":"Infinity"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.name").exists())
                .andExpect(jsonPath("$.fields.age").exists())
                .andExpect(jsonPath("$.fields.gender").exists())
                .andExpect(jsonPath("$.fields.height").exists());
        assertThat(athletes.count()).isEqualTo(athleteCount);
        mvc.perform(post("/api/nutrition-entries").header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"fat\":-1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.date").exists())
                .andExpect(jsonPath("$.fields.athleteId").exists())
                .andExpect(jsonPath("$.fields.fat").exists());
        mvc.perform(post("/api/nutrition-entries").header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"date":"+10000-01-01","athleteId":1,"calories":0}
                                """))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.fields.date").exists());
        mvc.perform(post("/api/nutrition-entries").header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"date":"2020-01-01","athleteId":1,"calories":1.5}
                                """))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Invalid request body"));
        mvc.perform(post("/api/consultations").header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"scheduledAt":"0999-01-01T12:00:00","athleteId":1,"coachId":1,"message":"Fixture"}
                                """))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.fields.scheduledAt").exists());
        assertThat(entries.count()).isEqualTo(entryCount);

        Athlete athlete = new Athlete();
        athlete.setName("Optional measures");
        athletes.saveAndFlush(athlete);
        mvc.perform(post("/api/nutrition-entries").header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"date":"2020-01-01","athleteId":%d,"protein":0,"notes":null}
                                """.formatted(athlete.getId())))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.protein").value(0));
        assertThat(entries.count()).isEqualTo(entryCount + 1);
    }

    @Test
    void currentDatabaseRoleAndCoachDirectoryApplyToIssuedTokens() throws Exception {
        User account = account("changed-role", Role.ADMIN, null, null);
        String token = bearer(account.getUsername());
        account.setRole(Role.ATHLETE);
        users.saveAndFlush(account);

        mvc.perform(get("/api/coaches")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/coaches").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk());
        mvc.perform(get("/api/athletes").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(0));
        mvc.perform(post("/api/athletes").header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Test athlete\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void readinessAndCorsExposeOnlyTheirIntendedPublicContract() throws Exception {
        mvc.perform(get("/readyz")).andExpect(status().isOk()).andExpect(content().string(""));
        mvc.perform(get("/health").header(HttpHeaders.ORIGIN, "http://localhost:4200"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.RETRY_AFTER));
        mvc.perform(options("/api/athletes").header(HttpHeaders.ORIGIN, "https://example.invalid")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }


    private org.springframework.test.web.servlet.ResultActions authRequest(String action, String username,
                                                                          String password) throws Exception {
        return mvc.perform(post("/api/auth/" + action).contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"username":"%s","password":"%s"}
                        """.formatted(username, password)));
    }


    @Test
    void registrationAlwaysStoresAnAthleteAndKeepsTheResponseContract() throws Exception {
        for (Role requestedRole : new Role[] {Role.ADMIN, Role.COACH}) {
            String username = "signup-" + requestedRole.name();
            mvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"username":"%s","password":"%s","role":"%s"}
                                    """.formatted(username, testPassword, requestedRole)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").isNotEmpty())
                    .andExpect(jsonPath("$.username").value(username))
                    .andExpect(jsonPath("$.role").value("ATHLETE"));

            User saved = users.findByUsername(username).orElseThrow();
            assertThat(saved.getRole()).isEqualTo(Role.ATHLETE);
            assertThat(encoder.matches(testPassword, saved.getPassword())).isTrue();
            assertThat(saved.getAthleteProfileId()).isNull();
            mvc.perform(get("/api/athletes").header(HttpHeaders.AUTHORIZATION, bearer(username)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(0));
        }
    }

    @Test
    void loginReturnsTheDatabaseRoleForEveryRole() throws Exception {
        for (Role role : Role.values()) {
            String username = "login-" + role.name();
            account(username, role, null, null);
            mvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"username":"%s","password":"%s","role":"ADMIN"}
                                    """.formatted(username, testPassword)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").isNotEmpty())
                    .andExpect(jsonPath("$.username").value(username))
                    .andExpect(jsonPath("$.role").value(role.name()));
        }
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"login-ADMIN","password":"%s"}
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void protectedRoutesRejectMissingInvalidAndDeletedAccountTokens() throws Exception {
        mvc.perform(get("/api/athletes")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/athletes").header(HttpHeaders.AUTHORIZATION, "Bearer invalid"))
                .andExpect(status().isUnauthorized());

        User removed = account("removed-account", Role.ADMIN, null, null);
        String authorization = bearer(removed.getUsername());
        users.delete(removed);
        users.flush();
        mvc.perform(get("/api/athletes").header(HttpHeaders.AUTHORIZATION, authorization))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void athleteReadsOnlyTheirOwnRecordsAndCannotWrite() throws Exception {
        Coach coach = coach("Assigned coach");
        Athlete owner = athlete("Owner", coach);
        Athlete other = athlete("Other", coach);
        entry(owner);
        NutritionEntry otherEntry = entry(other);
        consultation(owner, coach);
        Consultation otherConsultation = consultation(other, coach);
        account("athlete-account", Role.ATHLETE, owner.getId(), null);
        String authorization = bearer("athlete-account");

        for (String path : new String[] {"/api/athletes", "/api/nutrition-entries", "/api/consultations"}) {
            mvc.perform(get(path).header(HttpHeaders.AUTHORIZATION, authorization))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(1));
        }
        mvc.perform(get("/api/coaches/{id}/athletes", coach.getId())
                        .header(HttpHeaders.AUTHORIZATION, authorization))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get("/api/nutrition-entries").param("athleteId", other.getId().toString())
                        .header(HttpHeaders.AUTHORIZATION, authorization))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(0));
        mvc.perform(get("/api/athletes/{id}", other.getId()).header(HttpHeaders.AUTHORIZATION, authorization))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/nutrition-entries/{id}", otherEntry.getId())
                        .header(HttpHeaders.AUTHORIZATION, authorization))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/consultations/{id}", otherConsultation.getId())
                        .header(HttpHeaders.AUTHORIZATION, authorization))
                .andExpect(status().isNotFound());
        mvc.perform(post("/api/nutrition-entries").header(HttpHeaders.AUTHORIZATION, authorization)
                        .contentType(MediaType.APPLICATION_JSON).content(entryRequest(owner)))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/db-check").header(HttpHeaders.AUTHORIZATION, authorization))
                .andExpect(status().isForbidden());
    }

    @Test
    void coachWritesStayWithinAssignmentsAndAdminRetainsFullAccess() throws Exception {
        Coach assigned = coach("Coach one");
        Coach outside = coach("Coach two");
        Athlete owned = athlete("Assigned athlete", assigned);
        Athlete other = athlete("Other athlete", outside);
        NutritionEntry ownEntry = entry(owned);
        NutritionEntry otherEntry = entry(other);
        Consultation ownConsultation = consultation(owned, assigned);
        Consultation otherConsultation = consultation(other, outside);
        account("coach-account", Role.COACH, null, assigned.getId());
        account("admin-account", Role.ADMIN, null, null);
        String coachBearer = bearer("coach-account");
        String adminBearer = bearer("admin-account");

        mvc.perform(put("/api/nutrition-entries/{id}", ownEntry.getId())
                        .header(HttpHeaders.AUTHORIZATION, coachBearer)
                        .contentType(MediaType.APPLICATION_JSON).content(entryRequest(owned)))
                .andExpect(status().isOk());
        mvc.perform(put("/api/nutrition-entries/{id}", ownEntry.getId())
                        .header(HttpHeaders.AUTHORIZATION, coachBearer)
                        .contentType(MediaType.APPLICATION_JSON).content(entryRequest(other)))
                .andExpect(status().isForbidden());
        mvc.perform(put("/api/nutrition-entries/{id}", otherEntry.getId())
                        .header(HttpHeaders.AUTHORIZATION, coachBearer)
                        .contentType(MediaType.APPLICATION_JSON).content(entryRequest(owned)))
                .andExpect(status().isNotFound());

        mvc.perform(put("/api/consultations/{id}", ownConsultation.getId())
                        .header(HttpHeaders.AUTHORIZATION, coachBearer)
                        .contentType(MediaType.APPLICATION_JSON).content(consultationRequest(owned, assigned)))
                .andExpect(status().isOk());
        mvc.perform(put("/api/consultations/{id}", ownConsultation.getId())
                        .header(HttpHeaders.AUTHORIZATION, coachBearer)
                        .contentType(MediaType.APPLICATION_JSON).content(consultationRequest(owned, outside)))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/consultations").header(HttpHeaders.AUTHORIZATION, coachBearer)
                        .contentType(MediaType.APPLICATION_JSON).content(consultationRequest(other, assigned)))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/consultations/{id}", ownConsultation.getId())
                        .header(HttpHeaders.AUTHORIZATION, coachBearer))
                .andExpect(status().isForbidden());

        mvc.perform(get("/api/nutrition-entries").header(HttpHeaders.AUTHORIZATION, adminBearer))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(2));
        mvc.perform(put("/api/consultations/{id}", otherConsultation.getId())
                        .header(HttpHeaders.AUTHORIZATION, adminBearer)
                        .contentType(MediaType.APPLICATION_JSON).content(consultationRequest(owned, assigned)))
                .andExpect(status().isOk());
        mvc.perform(get("/api/db-check").header(HttpHeaders.AUTHORIZATION, adminBearer))
                .andExpect(status().isOk());
    }

    @Test
    void corsPreflightAllowsConfiguredAngularOrigin() throws Exception {
        mvc.perform(options("/api/athletes")
                        .header(HttpHeaders.ORIGIN, "http://localhost:4200")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:4200"));
        mvc.perform(options("/api/athletes")
                        .header(HttpHeaders.ORIGIN, "https://unlisted.example")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isForbidden());
    }

    private User account(String username, Role role, Long athleteId, Long coachId) {
        return users.save(User.builder().username(username).role(role)
                .password(encoder.encode(testPassword)).athleteProfileId(athleteId).coachProfileId(coachId).build());
    }

    private String bearer(String username) {
        return "Bearer " + jwt.generateToken(userDetails.loadUserByUsername(username));
    }

    private Coach coach(String name) {
        Coach coach = new Coach();
        coach.setName(name);
        return coaches.save(coach);
    }

    private Athlete athlete(String name, Coach coach) {
        Athlete athlete = new Athlete();
        athlete.setName(name);
        athlete.setCoach(coach);
        return athletes.save(athlete);
    }

    private NutritionEntry entry(Athlete athlete) {
        NutritionEntry entry = new NutritionEntry();
        entry.setAthlete(athlete);
        entry.setDate(LocalDate.now());
        return entries.save(entry);
    }

    private Consultation consultation(Athlete athlete, Coach coach) {
        Consultation consultation = new Consultation();
        consultation.setAthlete(athlete);
        consultation.setCoach(coach);
        consultation.setMessage("Appointment");
        consultation.setScheduledAt(LocalDateTime.now().plusDays(1));
        return consultations.save(consultation);
    }

    private static String entryRequest(Athlete athlete) {
        return """
                {"athleteId":%d,"date":"%s","calories":2000,"protein":100,"carbs":200,"fat":60}
                """.formatted(athlete.getId(), LocalDate.now());
    }

    private static String consultationRequest(Athlete athlete, Coach coach) {
        return """
                {"athleteId":%d,"coachId":%d,"scheduledAt":"%s","message":"Appointment"}
                """.formatted(athlete.getId(), coach.getId(), LocalDateTime.now().plusDays(1));
    }
}
