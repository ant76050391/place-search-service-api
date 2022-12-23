package org.example.filter;

import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;

public class ResponseDecorator extends ServerHttpResponseDecorator {
  private final ByteArrayOutputStream baos;

  public ResponseDecorator(ServerHttpResponse delegate, ByteArrayOutputStream baos) {
    super(delegate);
    this.baos = baos;
  }

  @Override
  public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
    Flux<DataBuffer> buffer = Flux.from(body);
    return super.writeWith(buffer.doOnNext(dataBuffer -> {
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
    }));
  }
}
