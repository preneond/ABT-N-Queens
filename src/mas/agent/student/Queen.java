package mas.agent.student;

public class Queen {
    private final int number;
    private int position;

    Queen(int number, int position, int size) {
        this.number = number;
        this.position = position;
    }

    int getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return String.format("[Q%d:%d]", number, position);
    }

    void setPosition(int xj) {
        position = xj;
    }
}
