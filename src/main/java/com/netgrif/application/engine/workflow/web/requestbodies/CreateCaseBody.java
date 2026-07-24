package com.netgrif.application.engine.workflow.web.requestbodies;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Milan on 4.2.2017.
 */
public class CreateCaseBody {

    public String title;
    public String netId;
    public String color;
    public Map<String, String> params = new HashMap<>();

    public CreateCaseBody() {
    }
}
