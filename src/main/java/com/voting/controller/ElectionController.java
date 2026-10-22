package com.voting.controller;

import com.voting.dto.CandidateResponse;
import com.voting.dto.ElectionResponse;
import com.voting.dto.ResultResponse;
import com.voting.dto.VoteRequest;
import com.voting.service.ElectionService;
import com.voting.service.VoteService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/elections")
public class ElectionController {

    private final ElectionService electionService;
    private final VoteService voteService;

    public ElectionController(ElectionService electionService, VoteService voteService) {
        this.electionService = electionService;
        this.voteService = voteService;
    }

    @GetMapping
    public List<ElectionResponse> list() { return electionService.listElections(); }

    @GetMapping("/{id}/candidates")
    public List<CandidateResponse> candidates(@PathVariable Long id) {
        return electionService.listCandidates(id);
    }

    @PostMapping("/{id}/vote")
    public Map<String, String> vote(@PathVariable Long id, @Valid @RequestBody VoteRequest req,
                                    Authentication auth) {
        voteService.castVote(auth.getName(), id, req.candidateId());
        return Map.of("message", "Vote cast successfully");
    }

    @GetMapping("/{id}/has-voted")
    public Map<String, Boolean> hasVoted(@PathVariable Long id, Authentication auth) {
        return Map.of("hasVoted", voteService.hasVoted(auth.getName(), id));
    }

    @GetMapping("/{id}/results")
    public List<ResultResponse> results(@PathVariable Long id, Authentication auth) {
        boolean admin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return electionService.results(id, admin);
    }
}
