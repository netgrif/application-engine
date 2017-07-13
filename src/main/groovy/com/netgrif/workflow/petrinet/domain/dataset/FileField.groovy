package com.netgrif.workflow.petrinet.domain.dataset

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.mongodb.core.mapping.Document

@Document
public class FileField extends FieldWithDefault<String> {

    private boolean generated = false
    @JsonIgnore
    private Set<String> logic

    public FileField() {
        super();
    }

    public void addLogic(String logic){
        if(this.logic == null)
            this.logic = new LinkedHashSet<>()

        String[] parts = logic.split(" ",2)
        if(parts[0] == "generate")
            generated = true

        this.logic.add(parts[1])
    }

    String getFilePath(String fileName){
        return "storage/"+ (this.generated?"generated/":"") +getObjectId()+"-"+fileName
    }

    boolean isGenerated() {
        return generated
    }

    Set<String> getLogic() {
        return logic
    }
}