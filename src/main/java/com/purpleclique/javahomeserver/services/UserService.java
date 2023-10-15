package com.purpleclique.javahomeserver.services;

import com.purpleclique.javahomeserver.models.auth.User;
import com.purpleclique.javahomeserver.utils.DBUtil;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    public User getUserFromToken(String header) {
        var token = DBUtil.getInstance().findToken(header.substring(7)).orElseThrow();
        return DBUtil.getInstance().findUserById(token.getTokenHolderId()).orElseThrow();
    }

}
