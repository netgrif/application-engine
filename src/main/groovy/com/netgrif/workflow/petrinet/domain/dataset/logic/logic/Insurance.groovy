package com.netgrif.workflow.petrinet.domain.dataset.logic.logic

import com.netgrif.workflow.petrinet.domain.dataset.FileField
import com.netgrif.workflow.workflow.domain.Case


class Insurance {

    private Case useCase
    private FileField field

    Insurance(Case useCase, FileField field) {
        this.useCase = useCase
        this.field = field
    }

    File offerPDF(){
        String name = "offer.txt"

        File f = new File(field.getFilePath(name))
        f.parentFile.mkdirs()
        if(!f.createNewFile()){
            f.delete()
            f.createNewFile()
        }

        f << "Timestamp"+new Date()+"\n"
        f << "Toto je generovaný súbor.\n"
        f << "Vytvorený iba na testovanie\n"
        f << "\n Nejaka hodnota:\n\t"
        f << useCase.dataSet.values().first().value

        useCase.dataSet.get(field.objectId).value = name

        return f
    }
}
