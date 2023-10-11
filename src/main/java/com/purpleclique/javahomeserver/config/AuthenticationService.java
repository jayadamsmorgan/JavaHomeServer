package com.purpleclique.javahomeserver.config;

import com.purpleclique.javahomeserver.models.auth.User;
import com.purpleclique.javahomeserver.models.request.AuthenticationRequest;
import com.purpleclique.javahomeserver.models.request.HomeSetupRequest;
import com.purpleclique.javahomeserver.models.response.AuthenticationResponse;
import com.purpleclique.javahomeserver.models.auth.Token;
import com.purpleclique.javahomeserver.utils.DBUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse registerUser(@NonNull RegisterRequest request) {
        String username = request.getUsername().trim();
        if (username.length() < 6) {
            return AuthenticationResponse.builder().error("Username is too short").build();
        }
        if (request.getPassword().length() < 4) {
            return AuthenticationResponse.builder().error("Password is too short").build();
        }
        Optional<User> userOptional = DBUtil.getInstance().findUserByUsername(username);
        if (userOptional.isPresent()) {
            return AuthenticationResponse.builder().error("User already exists").build();
        }
        var user = User.builder()
                .userType(request.getUserType())
                .username(username)
                .password(passwordEncoder.encode(request.getPassword()))
                .build();
        DBUtil.getInstance().saveNewUser(user);
        var jwtToken = jwtService.generateToken(user);
        saveUserToken(user, jwtToken);
        var homeName = DBUtil.getInstance().getHomeName().orElseThrow();
        return AuthenticationResponse.builder()
                .homeName(homeName)
                .userID(user.getId())
                .token(jwtToken)
                .build();
    }

    public AuthenticationResponse registerHome(@NonNull HomeSetupRequest request) {
        String username = request.getRegistrationRequest().getUsername().trim();
        if (username.length() < 6) {
            return AuthenticationResponse.builder().error("Username is too short.").build();
        }
        if (request.getRegistrationRequest().getPassword().length() < 4) {
            return AuthenticationResponse.builder().error("Password is too short.").build();
        }
        Optional<User> userOptional = DBUtil.getInstance().findUserByUsername(request.getRegistrationRequest().getUsername());
        if (userOptional.isPresent()) {
            return AuthenticationResponse.builder().error("User already exists.").build();
        }
        if (request.getHomeName().length() < 3) {
            return AuthenticationResponse.builder().error("Home name is too short.").build();
        }
        var homeName = DBUtil.getInstance().getHomeName();
        if (homeName.isPresent()) {
            return AuthenticationResponse.builder().error("Home is already registered.").build();
        }
        var user = User.builder()
                .userType(User.UserType.USER_TYPE_ADMIN)
                .username(username)
                .password(passwordEncoder.encode(request.getRegistrationRequest().getPassword()))
                .build();
        DBUtil.getInstance().saveNewUser(user);
        DBUtil.getInstance().saveHome(request.getHomeName());
        var jwtToken = jwtService.generateToken(user);
        saveUserToken(user, jwtToken);
        return AuthenticationResponse.builder()
                .userID(user.getId())
                .token(jwtToken)
                .homeName(request.getHomeName())
                .build();
    }

    public AuthenticationResponse authenticate(@NonNull AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        var user = DBUtil.getInstance().findUserByUsername(request.getUsername())
                .orElseThrow();
        var jwtToken = jwtService.generateToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, jwtToken);
        var homeName = DBUtil.getInstance().getHomeName().orElseThrow();
        return AuthenticationResponse.builder()
                .userID(user.getId())
                .homeName(homeName)
                .token(jwtToken)
                .build();
    }

    private void saveUserToken(@NonNull User user, @NonNull String jwtToken) {
        var token = Token.builder()
                .tokenHolderId(user.getId())
                .token(jwtToken)
                .tokenType("BEARER")
                .expired(false)
                .revoked(false)
                .build();
        DBUtil.getInstance().saveNewToken(token);
    }

    private void revokeAllUserTokens(@NonNull User user) {
        DBUtil.getInstance().deleteAllValidTokensByPersonId(user.getId());
    }
}

