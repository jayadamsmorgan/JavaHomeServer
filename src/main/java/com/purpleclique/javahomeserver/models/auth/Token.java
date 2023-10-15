package com.purpleclique.javahomeserver.models.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Token {

    private String id;

    private String token;

    private boolean revoked;

    private boolean expired;

    private String tokenHolderId;

    private String tokenType;

}
