package com.voting.model;

import jakarta.persistence.*;

@Entity
@Table(name = "candidates")
public class Candidate {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    private String party;
    @ManyToOne(optional = false)
    @JoinColumn(name = "election_id")
    private Election election;

    public Candidate() {}
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getParty() { return party; }
    public void setParty(String party) { this.party = party; }
    public Election getElection() { return election; }
    public void setElection(Election election) { this.election = election; }
}
