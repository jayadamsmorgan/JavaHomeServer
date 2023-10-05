package com.purpleclique.javahomeserver.models.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HomeSetupRequest {

    private RegistrationRequest registrationRequest;
    private String homeName;

}
