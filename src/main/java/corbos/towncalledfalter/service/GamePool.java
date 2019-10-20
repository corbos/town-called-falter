package corbos.towncalledfalter.service;

import corbos.towncalledfalter.game.Game;
import corbos.towncalledfalter.game.GameStatus;
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

    public synchronized Result<Game> create(String moderatorName) {

        if (Validation.isNullOrEmpty(moderatorName)) {
            return Result.invalid("moderator name is required");
        }

        Game game = new Game(getUniqueCode(), moderatorName);
        games.put(game.getCode(), game);

        return Result.success(game);
    }

    public synchronized Result<Game> join(String gameCode, String playerName) {

        if (Validation.isNullOrEmpty(playerName)) {
            return Result.invalid("player name is required");
        }

        Result<Game> result = getGame(gameCode);
        if (result.hasError()) {
            return result;
        }

        Game game = result.getValue();
        if (!game.join(playerName)) {
            String msg = "game already started, it's not joinable";
            if (game.getStatus() == GameStatus.JOINABLE) {
                msg = String.format("name %s is already used", playerName);
            }
            return Result.invalid(msg);
        }

        return Result.success(game);
    }

    public Result<Game> startSetup(String gameCode, String playerName) {

        Result<Game> result = getGame(gameCode);
        if (result.hasError()) {
            return result;
        }

        Game game = result.getValue();
        if (!game.startSetup(playerName)) {
            String msg = "game already started, can't start setup";
            if (game.getStatus() == GameStatus.JOINABLE) {
                msg = "forbidden, not the moderator";
            }
            return Result.invalid(msg);
        }

        return Result.success(game);
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
        String code = "";
        for (int i = 0; i < CODE_LENGTH; i++) {
            code += ALPHABET.charAt(RAND.nextInt(ALPHABET.length()));
        }
        return code;
    }

}
