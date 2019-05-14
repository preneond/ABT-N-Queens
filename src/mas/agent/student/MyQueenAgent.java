package mas.agent.student;

import java.util.*;
import java.util.stream.Collectors;

import cz.agents.alite.communication.content.Content;
import mas.agent.MASQueenAgent;
import cz.agents.alite.communication.Message;
import mas.agent.student.messages.*;

/**
 * This example agent illustrates the usage API available in the MASQueenAgent class.
 */
public class MyQueenAgent extends MASQueenAgent implements Runnable {

    private Set<Integer> neighborsSet;
    private Queen queen;
    private Chessboard chessboard;
    private Map<Integer, Integer> local_view;
    private List<Nogood> constraints;
    private Boolean[] terminatedAgents;
    private int idleCounter = 0;

    public MyQueenAgent(int agentId, int nAgents) {
        super(agentId, nAgents);
        this.neighborsSet = new HashSet<>();
        this.local_view = new HashMap<>();
        this.chessboard = new Chessboard(nAgents);
        this.constraints = new ArrayList<>();
        this.terminatedAgents = new Boolean[nAgents()];
        Arrays.fill(terminatedAgents, false);
    }

    @Override
    protected void start(int agentId, int nAgents) {
        initAgent();
        sendToken(agentId, new Token(agentId));
    }

    @Override
    protected void processMessages(List<Message> newMessages) {
        Token tmp_token = null;
        for (Message message : newMessages) {
            Object msgData = message.getContent().getData();
            int senderId = Integer.parseInt(message.getSender());
            if (msgData instanceof Ok) {
                handleOK(senderId, ((Ok) msgData).position);
                idleCounter = 0;
            } else if (msgData instanceof Nogood) {
                handleNogood(senderId, (Nogood) msgData);
                idleCounter = 0;
            } else if (msgData instanceof AddNeighbor) {
                handleAddNeighbor(senderId);
                idleCounter = 0;
            } else if (msgData instanceof Termination) {
                if (((Termination) msgData).solutionExists) {
                    handleAgentTermination(senderId);
                } else {
                    notifySolutionDoesNotExist();
                }
            } else if (msgData instanceof Token) {
                tmp_token = (Token) msgData;
            }
        }
        if (tmp_token != null) {
            handleToken(tmp_token);
        }

//        System.err.println(queen);
//        try {
//            Thread.sleep(100);
//        } catch (InterruptedException e) {
//        }
    }

    private boolean handleAgentTermination(int senderId) {
        terminatedAgents[senderId] = true;
        if (Arrays.asList(terminatedAgents).stream().allMatch(val -> val == true)) {
            notifySolutionFound(queen.getPosition());
            return true;
        }
        return false;
    }

    private void handleToken(Token token) {
        if (token.idleCounter > 20 && idleCounter != 0) {
            System.out.println(queen + " idle overflow");
            broadcastSolution(true);
            handleAgentTermination(getAgentId());
        } else {
            token.idleCounter = idleCounter;
            sendToken(getAgentId(), token);
            idleCounter++;
        }
    }

    private void sendToken(int j, Token token) {
        sendMessage(Integer.toString(j), new Content(token));
    }

    private void initAgent() {
        queen = new Queen(getAgentId(), 0, nAgents());
        for (int i = getAgentId() + 1; i < nAgents(); i++) {
            neighborsSet.add(i);
        }
        sendOKToNeighbors();
    }

    private void checkLocalView() {
        updateChessboard();
        boolean[] possibleDomains = getPossibleDomains();
        if (!chessboard.isQueenSafe(getAgentId()) || !possibleDomains[queen.getPosition()]) {
            for (int i = 0; i < nAgents(); i++) {
                chessboard.setPosition(getAgentId(), i);
                if (chessboard.isQueenSafe(getAgentId()) && possibleDomains[i]) {
                    queen.setPosition(i);
                    sendOKToNeighbors();
                    return;
                }
            }
            chessboard.setPosition(getAgentId(), queen.getPosition());
            backtrack();
        }
    }

    private boolean[] getPossibleDomains() {
        // vyfiltrujeme pouze nogoods ktere odpovidaji danemu local-view
        List<Nogood> current_nogoods = constraints.stream().filter(nogood -> {
            for (Map.Entry<Integer, Integer> constr_entry : nogood.getConstraints().entrySet()) {
                if (constr_entry.getKey() == getAgentId()) continue;
                if (chessboard.getQueenPosition(constr_entry.getKey()) != constr_entry.getValue()) {
                    return false;
                }
            }
            return true;
        }).collect(Collectors.toList());

        boolean[] available_domains = new boolean[nAgents()];
        Arrays.fill(available_domains, true);

        for (Nogood nogood : current_nogoods) {
            if (nogood.getQueenPosition(getAgentId()) != -1) {
                available_domains[nogood.getQueenPosition(getAgentId())] = false;
            }
        }

        return available_domains;
    }

    private void updateChessboard() {
        chessboard.invalidatePositions();
        chessboard.setPosition(getAgentId(), queen.getPosition());
        for (Map.Entry<Integer, Integer> viewEntry : local_view.entrySet()) {
            chessboard.setPosition(viewEntry.getKey(), viewEntry.getValue());
        }
    }

    private void backtrack() {
        Nogood nogood = chessboard.generateNogoodForQueen(getAgentId());
        if (nogood.isEmpty()) {
            broadcastSolution(false);
            notifySolutionDoesNotExist();
            return;
        } else {
            int lowest_prior_agent = nogood.getAgentWithLowestPriority();
            sendNogood(lowest_prior_agent, nogood);
            local_view.remove(lowest_prior_agent);
        }
        checkLocalView();
    }

    private void handleNogood(int j, Nogood nogood) {
        System.out.println(queen + localViewToString() + " received " + nogood + " from Q" + j);
        constraints.add(nogood);
//        for (Map.Entry<Integer, Integer> constr : nogood.getConstraints().entrySet()) {
//            if (!neighborsSet.contains(constr.getKey()) && constr.getKey() != getAgentId()) {
//                neighborsSet.add(constr.getKey());
//                sendAddNeighbor(constr.getKey());
//                local_view.put(constr.getKey(), constr.getValue());
//                updateChessboard();
//            }
//        }
        int old_val = queen.getPosition();
        checkLocalView();
        if (old_val != queen.getPosition()) {
            sendOk(j);
        }
    }

    private void handleAddNeighbor(int j) {
        neighborsSet.add(j);
    }

    private void handleOK(int j, int xj) {
        System.out.println(queen + localViewToString() + " received OK? from [Q" + j + ":" + xj + "]");
        local_view.put(j, xj);
        checkLocalView();
    }

    private void sendAddNeighbor(int j) {
        System.out.println(queen + localViewToString() + " sent Add Neighbor to: Q" + j);
        sendMessage(Integer.toString(j), new Content(new AddNeighbor()));
    }

    private void sendOk(int j) {
        System.out.println(queen + localViewToString() + " sent OK? to: Q" + j);
        sendMessage(Integer.toString(j), new Content(new Ok(getAgentId(), queen.getPosition())));
    }

    private void sendNogood(int j, Nogood nogood) {
        System.out.println(queen + localViewToString() + " sent " + nogood + " to: " + j);
        sendMessage(Integer.toString(j), new Content(nogood));
    }

    private void sendOKToNeighbors() {
        System.out.println(queen + localViewToString() + " sent OK? to neighbors" + neighborsSet.toString());
        Content msgContent = new Content(new Ok(getAgentId(), queen.getPosition()));
        for (int i : neighborsSet) {
            sendMessage(Integer.toString(i), msgContent);
        }
    }

    private void broadcastSolution(boolean solutionExists) {
        broadcast(new Content(new Termination(solutionExists)));
    }

    private String localViewToString() {
        final String[] str = {""};
        local_view.forEach((key, value) -> str[0] += "Q" + key + ": " + value + ",");
        return "(" + str[0] + ")";
    }
}