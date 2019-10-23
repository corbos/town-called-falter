package corbos.towncalledfalter.service;

import corbos.towncalledfalter.game.Game;
import java.util.HashMap;
import java.util.List;
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

    public Result<Game> join(String gameCode, String playerName) {

        if (Validation.isNullOrEmpty(playerName)) {
            return Result.invalid("player name is required");
        }

        Result<Game> result = getGame(gameCode);
        if (result.hasError()) {
            return result;
        }

        Game game = result.getValue();
        switch (game.join(playerName)) {
            case SUCCESS:
                return Result.success(game);
            case INVALID_GAME_STATUS:
                return Result.invalid("game already started, it's not joinable");
            case INVALID_STATE:
                String msg = String.format("name %s is already used", playerName);
                return Result.invalid(msg);
            default:
                return Result.invalid("unknown error");
        }
    }

    public Result<Game> setup(String gameCode, String playerName) {

        Result<Game> result = getGame(gameCode);
        if (result.hasError()) {
            return result;
        }

        Game game = result.getValue();
        switch (game.setup(playerName)) {
            case SUCCESS:
                return Result.success(game);
            case INVALID_GAME_STATUS:
                return Result.invalid("game already started, can't setup");
            case NOT_AUTHORIZED:
                return Result.invalid("forbidden, not the moderator");
            default:
                return Result.invalid("unknow error");
        }
    }

    public Result<Game> start(String gameCode,
            String playerName, List<String> orderedPlayers) {

        Result<Game> result = getGame(gameCode);
        if (result.hasError()) {
            return result;
        }

        Game game = result.getValue();
        switch (game.start(playerName, orderedPlayers)) {
            case SUCCESS:
                return Result.success(game);
            case INVALID_GAME_STATUS:
                return Result.invalid("game must be in setup to start");
            case NOT_AUTHORIZED:
                return Result.invalid("forbidden, not the moderator");
            case INVALID_STATE:
                return Result.invalid("sorted player list is invalid");
            default:
                return Result.invalid("unknow error");
        }

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
