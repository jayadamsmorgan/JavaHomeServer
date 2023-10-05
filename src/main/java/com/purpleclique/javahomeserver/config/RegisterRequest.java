package com.purpleclique.javahomeserver.config;

import com.purpleclique.javahomeserver.models.auth.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    private String username;
    private User.UserType userType;
    private String password;

}
