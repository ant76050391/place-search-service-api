package org.example.filter;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import reactor.core.publisher.Flux;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;

public class RequestDecorator extends ServerHttpRequestDecorator {
  private final ByteArrayOutputStream baos;

  public RequestDecorator(ServerHttpRequest delegate, ByteArrayOutputStream baos) {
    super(delegate);
    this.baos = baos;
  }

  @Override
  public Flux<DataBuffer> getBody() {
    return super.getBody().doOnNext(dataBuffer -> {
      try {
        Channels.newChannel(baos).write(dataBuffer.asByteBuffer().asReadOnlyBuffer());
      } catch (IOException e) {
        e.printStackTrace();
      } finally {
        try {
          baos.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    });
  }
}