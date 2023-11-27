package cn.chenjun.cloud.management.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.server.ServerEndpoint;

/**
 * @author chenjun
 */
@Slf4j
@Component
@ServerEndpoint(value = "/api/component/ws")
public class ComponentWsService extends AbstractWsService {

}
