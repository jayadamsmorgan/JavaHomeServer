package com.purpleclique.javahomeserver.controllers;

import com.purpleclique.javahomeserver.config.AuthenticationService;
import com.purpleclique.javahomeserver.config.RegisterRequest;
import com.purpleclique.javahomeserver.models.request.AuthenticationRequest;
import com.purpleclique.javahomeserver.models.request.HomeSetupRequest;
import com.purpleclique.javahomeserver.models.response.AuthenticationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register/home")
    public ResponseEntity<AuthenticationResponse> registerHome(@RequestBody HomeSetupRequest request) {
        var response = authenticationService.registerHome(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register/user")
    public ResponseEntity<AuthenticationResponse> registerUser(@RequestBody RegisterRequest request) {
        var response = authenticationService.registerUser(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody AuthenticationRequest request) {
        var response = authenticationService.authenticate(request);
        return ResponseEntity.ok(response);
    }

}
