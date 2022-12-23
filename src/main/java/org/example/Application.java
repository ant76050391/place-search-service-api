package org.example;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.embedded.netty.NettyServerCustomizer;
import org.springframework.context.annotation.Bean;
import reactor.netty.http.server.HttpServer;
import reactor.tools.agent.ReactorDebugAgent;

@Slf4j
@SpringBootApplication
public class Application {
  public static void main(String[] args) {
    ReactorDebugAgent.init();
    // NOTE : 성능 안나올때 의심 구간 설정해서 확인 해보기. 주의! 프로덕트 용 절대 아님.
    // BlockHound.builder().install();
    SpringApplication.run(Application.class, args);
  }

  @Bean
  public NettyReactiveWebServerFactory nettyReactiveWebServerFactory() {
    NettyReactiveWebServerFactory webServerFactory = new NettyReactiveWebServerFactory();
    webServerFactory.addServerCustomizers(new EventLoopNettyCustomizer());
    return webServerFactory;
  }

  private static class EventLoopNettyCustomizer implements NettyServerCustomizer {
    @Override
    public HttpServer apply(HttpServer httpServer) {
      EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
      eventLoopGroup.register(new NioServerSocketChannel());
      return httpServer.runOn(eventLoopGroup);
    }
  }
}
