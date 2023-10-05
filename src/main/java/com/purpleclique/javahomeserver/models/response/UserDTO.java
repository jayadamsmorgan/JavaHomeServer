package com.purpleclique.javahomeserver.models.response;

import com.purpleclique.javahomeserver.models.auth.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {

    int userId;
    User.UserType userType;
    String username;

}
