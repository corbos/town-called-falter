package corbos.towncalledfalter.network;

import corbos.towncalledfalter.game.Game;
import corbos.towncalledfalter.service.BaseRequest;
import corbos.towncalledfalter.service.GamePool;
import corbos.towncalledfalter.service.GameRequest;
import corbos.towncalledfalter.service.ResponseStatus;
import corbos.towncalledfalter.service.Result;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GameController {

    private final GamePool pool;

    public GameController(GamePool pool) {
        this.pool = pool;
    }

    @PostMapping("/create")
    public ResponseEntity<Result<Game>> create(@RequestBody BaseRequest request) {
        Result<Game> result = pool.create(request.getPlayerName());
        HttpStatus status = HttpStatus.CREATED;
        if (result.hasError()) {
            status = HttpStatus.UNPROCESSABLE_ENTITY;
        }
        return new ResponseEntity(result, status);
    }

    @PutMapping("/join")
    public ResponseEntity<Result<Game>> join(@RequestBody GameRequest request) {
        Result<Game> result = pool.join(request.getGameCode(), request.getPlayerName());
        HttpStatus status = HttpStatus.OK;
        if (result.getStatus() == ResponseStatus.INVALID) {
            status = HttpStatus.UNPROCESSABLE_ENTITY;
        } else if (result.getStatus() == ResponseStatus.NOT_FOUND) {
            status = HttpStatus.NOT_FOUND;
        }
        return new ResponseEntity(result, status);
    }
}
