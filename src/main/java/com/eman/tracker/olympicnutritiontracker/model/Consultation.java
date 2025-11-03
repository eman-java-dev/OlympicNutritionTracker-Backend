package com.eman.tracker.olympicnutritiontracker.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "consultations")
public class Consultation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message", length = 300, nullable = false)
    private String message;

    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    // انتبهي: اسم الخاصية في الجافا هو coach/athlete
    // واسم العمود في DB هو coach_ref / athlete_ref
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "athlete_ref", nullable = false)
    private Athlete athlete;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "coach_ref", nullable = false)
    private Coach coach;

    // ===== Getters/Setters =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }
    public Athlete getAthlete() { return athlete; }
    public void setAthlete(Athlete athlete) { this.athlete = athlete; }
    public Coach getCoach() { return coach; }
    public void setCoach(Coach coach) { this.coach = coach; }
}
