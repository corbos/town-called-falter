package corbos.towncalledfalter.service;

import corbos.towncalledfalter.game.Move;

public class ActionRequest extends GameRequest {

    private Move move;

    public Move getMove() {
        return move;
    }

    public void setMove(Move move) {
        this.move = move;
    }

}
