package com.purpleclique.javahomeserver.controllers;

import com.purpleclique.javahomeserver.models.auth.User;
import com.purpleclique.javahomeserver.models.dto.UserDTO;
import com.purpleclique.javahomeserver.utils.DBUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UsersController {

    @GetMapping
    public ResponseEntity<Set<UserDTO>> getAllUsers() {
        Set<User> users = DBUtil.getInstance().getAllUsers();
        Set<UserDTO> userDTOS = new HashSet<>();
        for (User user : users) {
            userDTOS.add(UserDTO.builder()
                            .username(user.getUsername())
                            .userType(user.getUserType())
                            .userId(user.getId())
                    .build());
        }
        return ResponseEntity.ok(userDTOS);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable int userId) {
        Optional<User> userOptional = DBUtil.getInstance().findUserById(userId);
        if (userOptional.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        UserDTO userDTO = UserDTO.builder()
                .userId(userId)
                .userType(userOptional.get().getUserType())
                .username(userOptional.get().getUsername())
                .build();
        return ResponseEntity.ok(userDTO);
    }

}
