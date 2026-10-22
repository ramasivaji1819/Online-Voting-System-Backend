package com.voting.service;

import com.voting.dto.CandidateRequest;
import com.voting.dto.CandidateResponse;
import com.voting.dto.ElectionRequest;
import com.voting.dto.ElectionResponse;
import com.voting.dto.ResultResponse;
import com.voting.exception.ApiException;
import com.voting.model.Candidate;
import com.voting.model.Election;
import com.voting.repository.CandidateRepository;
import com.voting.repository.ElectionRepository;
import com.voting.repository.VoteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ElectionService {

    private final ElectionRepository electionRepository;
    private final CandidateRepository candidateRepository;
    private final VoteRepository voteRepository;

    public ElectionService(ElectionRepository electionRepository, CandidateRepository candidateRepository,
                           VoteRepository voteRepository) {
        this.electionRepository = electionRepository;
        this.candidateRepository = candidateRepository;
        this.voteRepository = voteRepository;
    }

    // ---------- admin ----------
    public ElectionResponse createElection(ElectionRequest req) {
        if (!req.endTime().isAfter(req.startTime())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "endTime must be after startTime");
        }
        Election e = new Election();
        e.setTitle(req.title());
        e.setDescription(req.description());
        e.setStartTime(req.startTime());
        e.setEndTime(req.endTime());
        return toResponse(electionRepository.save(e));
    }

    public CandidateResponse addCandidate(Long electionId, CandidateRequest req) {
        Election e = getElection(electionId);
        if (LocalDateTime.now().isAfter(e.getStartTime())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cannot add candidates after the election has started");
        }
        Candidate c = new Candidate();
        c.setName(req.name());
        c.setParty(req.party());
        c.setElection(e);
        return toResponse(candidateRepository.save(c));
    }

    // ---------- common ----------
    public List<ElectionResponse> listElections() {
        return electionRepository.findAll().stream().map(this::toResponse).toList();
    }

    public List<CandidateResponse> listCandidates(Long electionId) {
        getElection(electionId);
        return candidateRepository.findByElectionId(electionId).stream().map(this::toResponse).toList();
    }

    public List<ResultResponse> results(Long electionId, boolean isAdmin) {
        Election e = getElection(electionId);
        if (!isAdmin && LocalDateTime.now().isBefore(e.getEndTime())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Results are available after the election ends");
        }
        return candidateRepository.findByElectionId(electionId).stream()
                .map(c -> new ResultResponse(c.getId(), c.getName(), c.getParty(),
                        voteRepository.countByCandidateId(c.getId())))
                .sorted((a, b) -> Long.compare(b.votes(), a.votes()))
                .toList();
    }

    public Election getElection(Long id) {
        return electionRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Election not found: " + id));
    }

    private ElectionResponse toResponse(Election e) {
        LocalDateTime now = LocalDateTime.now();
        String status = now.isBefore(e.getStartTime()) ? "UPCOMING"
                : now.isAfter(e.getEndTime()) ? "CLOSED" : "ACTIVE";
        return new ElectionResponse(e.getId(), e.getTitle(), e.getDescription(),
                e.getStartTime(), e.getEndTime(), status);
    }

    private CandidateResponse toResponse(Candidate c) {
        return new CandidateResponse(c.getId(), c.getName(), c.getParty());
    }
}
