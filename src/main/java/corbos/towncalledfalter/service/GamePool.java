package corbos.towncalledfalter.service;

import corbos.towncalledfalter.game.Game;
import corbos.towncalledfalter.game.Move;
import corbos.towncalledfalter.game.MoveResult;
import corbos.towncalledfalter.game.MoveType;
import java.util.HashMap;
import java.util.Random;
import org.springframework.stereotype.Service;

@Service
public class GamePool {

    private static final Random RAND = new Random();
    // no 0/O to avoid confusion. maybe remove 1/l?
    private static final String ALPHABET
            = "123456789ABCDEFGHIJKLMNPQRSTUVWXYZ!@#$%&*+=?";
    private static final int CODE_LENGTH = 4;

    private final HashMap<String, Game> games = new HashMap<>();

    public Result<Game> create(String moderatorName) {

        if (Validation.isNullOrEmpty(moderatorName)) {
            return Result.invalid("moderator name is required");
        }

        Game game = new Game(getUniqueCode(), moderatorName);
        games.put(game.getCode(), game);

        return Result.success(game);
    }

    public Result<Game> move(String gameCode, Move m) {

        Result<Game> result = getGame(gameCode);
        if (result.hasError()) {
            return result;
        }

        Game game = result.getValue();
        MoveResult mr = game.move(m);

        if (mr == MoveResult.SUCCESS) {
            return Result.success(game);
        }

        return Result.invalid(getErrorMessage(m.getType(), mr));
    }

    public Result<Game> getGame(String code) {

        if (code == null || code.length() != CODE_LENGTH) {
            return Result.invalid(String.format(
                    "game code must have %s characters",
                    CODE_LENGTH));
        }

        Game game = games.get(code);
        if (game == null) {
            return Result.notFound(String.format("game %s not found", code));
        }

        return Result.success(game);
    }

    private String getErrorMessage(MoveType type, MoveResult result) {
        switch (result) {
            case NOT_AUTHORIZED:
                return "forbidden, player is not authorized";
            case INVALID_STATE:
                if (type == MoveType.JOIN) {
                    return "player name is already in use";
                }
                return "bad request";
            case INVALID_GAME_STATUS:
                if (type == MoveType.JOIN) {
                    return "game already started. it's not joinable";
                }
                return "game is in the incorrect status";
        }
        return "unknown error";
    }

    private String getUniqueCode() {
        String code = null;
        do {
            code = generateCode();
        } while (games.containsKey(code));
        return code;
    }

    private static String generateCode() {
        char[] code = new char[CODE_LENGTH];
        for (int i = 0; i < CODE_LENGTH; i++) {
            code[i] = ALPHABET.charAt(RAND.nextInt(ALPHABET.length()));
        }
        return new String(code);
    }

}
