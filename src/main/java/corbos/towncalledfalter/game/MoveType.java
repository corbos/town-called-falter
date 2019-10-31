package corbos.towncalledfalter.game;

public enum MoveType {

    // moderator moves
    SETUP(true, new GameStatus[]{GameStatus.JOINABLE}),
    START(true, new GameStatus[]{GameStatus.SETUP}),
    KILL(true, new GameStatus[]{
        GameStatus.DAY_NOMINATE, GameStatus.DAY_VOTE, GameStatus.NIGHT}),
    // all player moves
    JOIN(false, new GameStatus[]{GameStatus.JOINABLE}),
    NOMINATE(false, new GameStatus[]{GameStatus.DAY_NOMINATE}),
    VOTE(false, new GameStatus[]{GameStatus.DAY_VOTE}),
    USE_ABILITY(false, new GameStatus[]{GameStatus.NIGHT});

    private final boolean moderator;
    private final GameStatus[] validStatuses;

    MoveType(boolean moderator, GameStatus[] validStatuses) {
        this.moderator = moderator;
        this.validStatuses = validStatuses;
    }

    public boolean requiresModerator() {
        return moderator;
    }

    public boolean isValid(GameStatus status) {
        for (GameStatus st : validStatuses) {
            if (st == status) {
                return true;
            }
        }
        return false;
    }
}
