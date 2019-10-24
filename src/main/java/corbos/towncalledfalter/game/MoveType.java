package corbos.towncalledfalter.game;

public enum MoveType {

    // moderator moves
    SETUP(true, new GameStatus[]{GameStatus.JOINABLE}),
    START(true, new GameStatus[]{GameStatus.SETUP}),
    KILL(true, new GameStatus[]{GameStatus.DAY, GameStatus.NIGHT}),
    // all player moves
    JOIN(false, new GameStatus[]{GameStatus.JOINABLE}),
    VOTE(false, new GameStatus[]{GameStatus.DAY, GameStatus.NIGHT}),
    USE_POWER(false, new GameStatus[]{GameStatus.DAY, GameStatus.NIGHT});

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
