public class Tabu {

    private int vertexPlace;
    private boolean changedTo;

    public Tabu() {
        this.vertexPlace = 0;
        this.changedTo = false;
    }

    public int getVertexPlace() {
        return vertexPlace;
    }

    public void setVertexPlace(int vertexPlace) {
        this.vertexPlace = vertexPlace;
    }

    public boolean getChangedTo() {
        return changedTo;
    }

    public void setChangedTo(boolean changedTo) {
        this.changedTo = changedTo;
    }
}
