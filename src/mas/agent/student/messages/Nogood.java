package mas.agent.student.messages;

import mas.agent.student.Chessboard;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Nogood {

    /**
     * key is queen number, value its position
     **/
    private final Map<Integer, Integer> constraints;

    public Nogood() {
        this.constraints = new HashMap<>();
    }

    /**
     * @return <tt>true</tt> IFF this no-good is empty
     */
    public boolean isEmpty() {
        return constraints.isEmpty();
    }

    /**
     * @return a position assigned to given queen if any
     */
    public int getQueenPosition(int queen) {
        return constraints.containsKey(queen) ? constraints.get(queen) : -1;
    }

    /**
     * Assigns a position for given queen.
     */
    public void setQueenPosition(int queen, int position) {
        constraints.put(queen, position);
    }

    public Nogood createNogoodForQueen(int queen) {
        Nogood nogood = new Nogood();
        for (Integer q : constraints.keySet()) {
            if (q != queen) {
                nogood.setQueenPosition(q, constraints.get(q));
            }
        }
        return nogood;
    }

    public Map<Integer, Integer> getConstraints() {
        return constraints;
    }

    @Override
    public String toString() {
        return "Nogood{" +
                "constraints=" + constraints +
                '}';
    }

    public int getAgentWithLowestPriority() {
        return Collections.max(constraints.keySet());
    }
}
