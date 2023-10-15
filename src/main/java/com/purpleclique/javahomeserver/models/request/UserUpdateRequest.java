package com.purpleclique.javahomeserver.models.request;

import com.purpleclique.javahomeserver.models.auth.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

    String newPassword;
    int userId;
    User.UserType userType;
    String username;

}
