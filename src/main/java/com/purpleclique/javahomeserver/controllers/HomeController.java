package com.purpleclique.javahomeserver.controllers;

import com.purpleclique.javahomeserver.models.auth.User;
import com.purpleclique.javahomeserver.models.dto.UserDTO;
import com.purpleclique.javahomeserver.models.request.HomeNameUpdateRequest;
import com.purpleclique.javahomeserver.models.response.HomeInfoResponse;
import com.purpleclique.javahomeserver.services.UserService;
import com.purpleclique.javahomeserver.utils.DBUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/home")
@RequiredArgsConstructor
public class HomeController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<HttpStatus> updateHomeName(@RequestHeader("Authorization") String header,
                                                     @RequestBody HomeNameUpdateRequest request) {
        if (request == null || request.getHomeName() == null) {
            return null;
        }
        var user = userService.getUserFromToken(header);
        if (user.getUserType() != User.UserType.USER_TYPE_ADMIN) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        DBUtil.getInstance().updateHomeName(request.getHomeName());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<HomeInfoResponse> getHomeInfo() {
        var homeName = DBUtil.getInstance().getHomeName().orElseThrow();
        Set<UserDTO> userDTOs = new HashSet<>();
        var users = DBUtil.getInstance().getAllUsers();
        for (User user : users) {
            userDTOs.add(UserDTO.builder()
                    .userId(user.getId())
                    .userType(user.getUserType())
                    .username(user.getUsername())
                    .build());
        }
        return ResponseEntity.ok(HomeInfoResponse.builder()
                .homeName(homeName)
                .users(userDTOs)
                .build());
    }

}
