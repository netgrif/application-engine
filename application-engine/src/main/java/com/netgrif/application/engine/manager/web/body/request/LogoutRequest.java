package com.netgrif.application.engine.manager.web.body.request;


import lombok.Data;

import java.util.List;

@Data
public class LogoutRequest {

    private List<String> users;

}
