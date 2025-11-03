package com.netgrif.application.engine.objects.dto.response.petrinet;

import java.io.Serializable;
import java.util.List;

public record TransactionDto(List<String> transitions, String title) implements Serializable {

}