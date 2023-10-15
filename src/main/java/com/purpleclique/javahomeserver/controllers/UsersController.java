package com.purpleclique.javahomeserver.controllers;

import com.purpleclique.javahomeserver.models.auth.User;
import com.purpleclique.javahomeserver.models.dto.UserDTO;
import com.purpleclique.javahomeserver.models.request.UserUpdateRequest;
import com.purpleclique.javahomeserver.services.UserService;
import com.purpleclique.javahomeserver.utils.DBUtil;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UsersController {

    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<Set<UserDTO>> getAllUsers() {
        var users = DBUtil.getInstance().getAllUsers();
        Set<UserDTO> userDTOS = new HashSet<>();
        for (var user : users) {
            userDTOS.add(UserDTO.builder()
                            .username(user.getUsername())
                            .userType(user.getUserType())
                            .userId(user.getId())
                    .build());
        }
        return ResponseEntity.ok(userDTOS);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable String userId) {
        var userOptional = DBUtil.getInstance().findUserById(userId);
        if (userOptional.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        var userDTO = UserDTO.builder()
                .userId(userId)
                .userType(userOptional.get().getUserType())
                .username(userOptional.get().getUsername())
                .build();
        return ResponseEntity.ok(userDTO);
    }
    @PutMapping("/new")
    public ResponseEntity<HttpStatus> createNewUser(@RequestHeader("Authorization") @NotNull String header,
                                                    @RequestBody UserUpdateRequest updateRequest) {
        if (updateRequest == null || updateRequest.getUserType() == null) {
            return null;
        }
        var user = userService.getUserFromToken(header);
        if (user.getUserType() != User.UserType.USER_TYPE_ADMIN) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        var newUser = User.builder()
                .username(updateRequest.getUsername())
                .userType(updateRequest.getUserType())
                .password(passwordEncoder.encode(updateRequest.getNewPassword()))
                .build();
        DBUtil.getInstance().saveNewUser(newUser);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/update")
    public ResponseEntity<HttpStatus> updateUser(@RequestHeader("Authorization") @NotNull String header,
                                                 @RequestBody UserUpdateRequest updateRequest) {
        if (updateRequest == null || updateRequest.getUserType() == null) {
            return null;
        }
        var user = userService.getUserFromToken(header);
        if (user.getUserType() == User.UserType.USER_TYPE_ADMIN) {
            var targetUserOpt = DBUtil.getInstance().findUserById(updateRequest.getUserId());
            if (targetUserOpt.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            var targetUser = targetUserOpt.get();
            targetUser.setUserType(updateRequest.getUserType());
            targetUser.setUsername(updateRequest.getUsername());
            targetUser.setPassword(passwordEncoder.encode(updateRequest.getNewPassword()));
            DBUtil.getInstance().updateUser(targetUser);
            return new ResponseEntity<>(HttpStatus.OK);
        } else if (!Objects.equals(user.getId(), updateRequest.getUserId())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        if (updateRequest.getUsername() != null && !updateRequest.getUsername().equals("")) {
            user.setUsername(updateRequest.getUsername());
        }
        if (updateRequest.getNewPassword() != null && updateRequest.getNewPassword().length() >= 6) {
            user.setPassword(passwordEncoder.encode(updateRequest.getNewPassword()));
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
