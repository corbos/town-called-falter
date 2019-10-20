package corbos.towncalledfalter;

import corbos.towncalledfalter.network.SocketHandler;
import corbos.towncalledfalter.service.GamePool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private GamePool pool;

    @Autowired
    public void setPool(GamePool pool) {
        this.pool = pool;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new SocketHandler(pool), "/messages");
    }
}
