package api.petrinet.domain;

public class PositionDto {

    private Integer x;

    private Integer y;

    public PositionDto() {
    }

    public PositionDto(Integer x, Integer y) {
        this.x = x;
        this.y = y;
    }

    public Integer getX() {
        return x;
    }

    public void setX(Integer x) {
        this.x = x;
    }

    public Integer getY() {
        return y;
    }

    public void setY(Integer y) {
        this.y = y;
    }
}
