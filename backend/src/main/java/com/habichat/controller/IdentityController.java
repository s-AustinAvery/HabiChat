package com.habichat.controller;

import com.habichat.dto.IdentityResponse;
import com.habichat.entity.Identity;
import com.habichat.security.TokenAuthFilter;
import com.habichat.service.IdentityService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/identity")
public class IdentityController {

    private final IdentityService identityService;

    public IdentityController(IdentityService identityService) {
        this.identityService = identityService;
    }

    @PostMapping
    public IdentityResponse create() {
        return identityService.createIdentity();
    }

    @GetMapping("/me")
    public ResponseEntity<IdentityResponse> me(HttpServletRequest request) {
        Identity identity = (Identity) request.getAttribute(TokenAuthFilter.IDENTITY_ATTRIBUTE);
        if (identity == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(new IdentityResponse(identity.getUsername(), identity.getToken()));
    }
}
