package com.voting.controller;

import com.voting.dto.CandidateRequest;
import com.voting.dto.CandidateResponse;
import com.voting.dto.ElectionRequest;
import com.voting.dto.ElectionResponse;
import com.voting.service.ElectionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final ElectionService electionService;

    public AdminController(ElectionService electionService) { this.electionService = electionService; }

    @PostMapping("/elections")
    public ResponseEntity<ElectionResponse> createElection(@Valid @RequestBody ElectionRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(electionService.createElection(req));
    }

    @PostMapping("/elections/{electionId}/candidates")
    public ResponseEntity<CandidateResponse> addCandidate(@PathVariable Long electionId,
                                                          @Valid @RequestBody CandidateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(electionService.addCandidate(electionId, req));
    }
}
