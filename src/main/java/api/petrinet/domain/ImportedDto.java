package api.petrinet.domain;

public class ImportedDto {
    private String importId;

    public String getImportId() {
        return importId;
    }

    public void setImportId(String id) {
        this.importId = id;
    }

    public ImportedDto() {
    }

    public ImportedDto(String importId) {
        this.importId = importId;
    }
}
