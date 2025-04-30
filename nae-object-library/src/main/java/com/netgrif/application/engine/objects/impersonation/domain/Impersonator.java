package com.netgrif.application.engine.objects.impersonation.domain;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Impersonator implements Serializable {

    protected String impersonatorId;

    protected String impersonatedId;

    protected List<String> configIds = new ArrayList<>();

    protected LocalDateTime impersonatingSince;
    protected LocalDateTime impersonatingUntil;
}
