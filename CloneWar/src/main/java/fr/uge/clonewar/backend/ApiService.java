package fr.uge.clonewar.backend;

import io.helidon.common.configurable.ThreadPoolSupplier;
import io.helidon.common.http.DataChunk;
import io.helidon.common.http.Http;
import io.helidon.common.reactive.IoMulti;
import io.helidon.media.multipart.ReadableBodyPart;
import io.helidon.webserver.Routing;
import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;
import io.helidon.webserver.Service;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;

public final class ApiService implements Service {

  private final FileStorage storage = new FileStorage();
  private final ExecutorService executor = ThreadPoolSupplier.create("multipart-thread-pool").get();


  @Override
  public void update(Routing.Rules rules) {
    rules.get("/", (req, res) -> res.send("Hello World"))
        .post("/jars", this::jarsHandler);
  }

  private void jarsHandler(ServerRequest request, ServerResponse response) {
    request.content().asStream(ReadableBodyPart.class)
        .forEach(part -> {
          part.content().map(DataChunk::data)
              .flatMapIterable(Arrays::asList)
              .to(IoMulti.writeToFile(storage.create(part.name()))
                  .executor(executor)
                  .build());
        })
        .onError(response::send)
        .onComplete(() -> {
          System.out.println(storage.listFiles());
          response.status(Http.Status.OK_200);
          response.send("Received files");
        }).ignoreElement();
  }

}
