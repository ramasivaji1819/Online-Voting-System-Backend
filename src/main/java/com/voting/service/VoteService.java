package com.voting.service;

import com.voting.exception.ApiException;
import com.voting.model.Candidate;
import com.voting.model.Election;
import com.voting.model.Role;
import com.voting.model.User;
import com.voting.model.Vote;
import com.voting.repository.CandidateRepository;
import com.voting.repository.UserRepository;
import com.voting.repository.VoteRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class VoteService {

    private final VoteRepository voteRepository;
    private final UserRepository userRepository;
    private final CandidateRepository candidateRepository;
    private final ElectionService electionService;

    public VoteService(VoteRepository voteRepository, UserRepository userRepository,
                       CandidateRepository candidateRepository, ElectionService electionService) {
        this.voteRepository = voteRepository;
        this.userRepository = userRepository;
        this.candidateRepository = candidateRepository;
        this.electionService = electionService;
    }

    public void castVote(String email, Long electionId, Long candidateId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));
        if (user.getRole() == Role.ADMIN) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Admins cannot vote");
        }
        Election election = electionService.getElection(electionId);
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(election.getStartTime()) || now.isAfter(election.getEndTime())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Election is not active");
        }
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Candidate not found"));
        if (!candidate.getElection().getId().equals(electionId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Candidate does not belong to this election");
        }
        if (voteRepository.existsByUserIdAndElectionId(user.getId(), electionId)) {
            throw new ApiException(HttpStatus.CONFLICT, "You have already voted in this election");
        }
        Vote vote = new Vote();
        vote.setUser(user);
        vote.setElection(election);
        vote.setCandidate(candidate);
        try {
            voteRepository.saveAndFlush(vote);
        } catch (DataIntegrityViolationException ex) {
            // unique (user_id, election_id) constraint: protects against double-click / race conditions
            throw new ApiException(HttpStatus.CONFLICT, "You have already voted in this election");
        }
    }

    public boolean hasVoted(String email, Long electionId) {
        return userRepository.findByEmail(email)
                .map(u -> voteRepository.existsByUserIdAndElectionId(u.getId(), electionId))
                .orElse(false);
    }
}
