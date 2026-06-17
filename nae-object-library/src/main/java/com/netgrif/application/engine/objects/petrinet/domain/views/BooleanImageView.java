package com.netgrif.application.engine.objects.petrinet.domain.views;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BooleanImageView extends View {

    private String trueImage;

    private String falseImage;
}
