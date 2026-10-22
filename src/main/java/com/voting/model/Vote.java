package com.voting.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** One vote per user per election (enforced by the unique constraint). */
@Entity
@Table(name = "votes",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "election_id"}))
public class Vote {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;
    @ManyToOne(optional = false)
    @JoinColumn(name = "election_id")
    private Election election;
    @ManyToOne(optional = false)
    @JoinColumn(name = "candidate_id")
    private Candidate candidate;
    private LocalDateTime castAt = LocalDateTime.now();

    public Vote() {}
    public Long getId() { return id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Election getElection() { return election; }
    public void setElection(Election election) { this.election = election; }
    public Candidate getCandidate() { return candidate; }
    public void setCandidate(Candidate candidate) { this.candidate = candidate; }
    public LocalDateTime getCastAt() { return castAt; }
    public void setCastAt(LocalDateTime castAt) { this.castAt = castAt; }
}
