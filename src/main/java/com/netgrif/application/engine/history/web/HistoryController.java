package com.netgrif.application.engine.history.web;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/history")
@ConditionalOnProperty(
        value = "nae.history.web.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class HistoryController {

}