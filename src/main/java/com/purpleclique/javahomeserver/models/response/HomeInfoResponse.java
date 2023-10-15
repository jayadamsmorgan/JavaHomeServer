package com.purpleclique.javahomeserver.models.response;

import com.purpleclique.javahomeserver.models.dto.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HomeInfoResponse {

    String homeName;
    Set<UserDTO> users;

}
