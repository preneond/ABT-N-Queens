package mas.agent.student;

import mas.agent.student.messages.Nogood;

import java.util.Arrays;
import java.util.List;

public class Chessboard {
    private final int size;

    private final int[] queenPositions;

    Chessboard(int size) {
        this.size = size;
        this.queenPositions = new int[size];
        invalidatePositions();
    }

    void invalidatePositions() {
        Arrays.fill(queenPositions, -1);
    }

    Nogood generateNogoodForQueen(int n) {
        final Nogood nogood = new Nogood();
        for (int i = 0; i < n; i++) {
            int queenPosition = getQueenPosition(i);
            if (queenPosition != -1) {
                nogood.setQueenPosition(i, queenPosition);
            }
        }
        return nogood;
    }

    void setPosition(int queen, int position) {
        queenPositions[queen] = position;
    }

    int getQueenPosition(int p) {
        return queenPositions[p];
    }

    boolean isQueenSafe(int q) {
        int queen = queenPositions[q];
        if (queen == -1) throw new RuntimeException("isQueenSafe: Queen is not initialized!");

        for (int i = 0; i < size; i++) {
            int other = queenPositions[i];
            if (q != i && other != -1) {
                if (queen == other) {
                    return false;
                }
                if (queen - q == other - i || queen + q == other + i) {
                    return false;
                }
            }
        }
        return true;
    }

    boolean isStateConsistent() {
        for (int i = 0; i < size; i++) {
            if (getQueenPosition(i) == -1) continue;
            if (!isQueenSafe(i)) return false;
        }
        return true;
    }
}
