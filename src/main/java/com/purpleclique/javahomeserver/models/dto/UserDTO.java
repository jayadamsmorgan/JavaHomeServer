package com.purpleclique.javahomeserver.models.dto;

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

    String userId;
    User.UserType userType;
    String username;

}
