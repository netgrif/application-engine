package api.petrinet.domain;

public final class PlaceDto extends NodeDto {

    private Integer tokens;

    private Boolean isStatic;

    public PlaceDto() {
    }

    public PlaceDto(String id) {
        super(id);
    }

    public PlaceDto(PositionDto position, I18nStringDto title) {
        super(position, title);
    }

    public PlaceDto(String id, PositionDto position, I18nStringDto title) {
        super(id, position, title);
    }

    public PlaceDto(Integer tokens, Boolean isStatic) {
        this.tokens = tokens;
        this.isStatic = isStatic;
    }

    public PlaceDto(String id, Integer tokens, Boolean isStatic) {
        super(id);
        this.tokens = tokens;
        this.isStatic = isStatic;
    }

    public PlaceDto(PositionDto position, I18nStringDto title, Integer tokens, Boolean isStatic) {
        super(position, title);
        this.tokens = tokens;
        this.isStatic = isStatic;
    }

    public PlaceDto(String id, PositionDto position, I18nStringDto title, Integer tokens, Boolean isStatic) {
        super(id, position, title);
        this.tokens = tokens;
        this.isStatic = isStatic;
    }

    public Integer getTokens() {
        return tokens;
    }

    public void setTokens(Integer tokens) {
        this.tokens = tokens;
    }

    public Boolean getStatic() {
        return isStatic;
    }

    public void setStatic(Boolean aStatic) {
        isStatic = aStatic;
    }
}
