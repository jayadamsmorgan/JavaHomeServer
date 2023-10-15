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

    private int id;

    private String token;

    private boolean revoked;

    private boolean expired;

    private int tokenHolderId;

    private String tokenType;

}
