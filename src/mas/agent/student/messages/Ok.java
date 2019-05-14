package mas.agent.student.messages;

public class Ok {
    public int position;

    public Ok(int agent, int position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return "Ok{" +
                "position=" + position +
                '}';
    }
}
