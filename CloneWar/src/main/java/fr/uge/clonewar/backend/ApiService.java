package fr.uge.clonewar.backend;

import fr.uge.clonewar.Artefact;
import fr.uge.clonewar.ReadByteCode;
import io.helidon.common.configurable.ThreadPoolSupplier;
import io.helidon.common.http.DataChunk;
import io.helidon.common.http.Http;
import io.helidon.common.reactive.IoMulti;
import io.helidon.common.reactive.Single;
import io.helidon.media.multipart.ReadableBodyPart;
import io.helidon.webserver.Routing;
import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;
import io.helidon.webserver.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;

public final class ApiService implements Service {

  private final FileStorage storage = new FileStorage();
  private final ExecutorService executor = ThreadPoolSupplier.create("multipart-thread-pool").get();


  @Override
  public void update(Routing.Rules rules) {
    rules.get("/", (req, res) -> res.send("Hello World"))
        .post("/analyze", this::analyze);
  }

  private void analyze(ServerRequest request, ServerResponse response) {
    downloadArtefact(request)
        .onError(response::send)
        .thenAccept(artefact -> {
          System.out.println("Analyzing... " + artefact);

          var readByteCode = new ReadByteCode();
          try {
            readByteCode.analyze(artefact.main());
          } catch (IOException e) {
            response.send(e);
          }

          System.out.println("Done ! ");

          response.status(Http.Status.OK_200);
          response.send("Received files");
        });
  }

  private Single<Artefact> downloadArtefact(ServerRequest request) {
    System.out.println("Downloading ... ");
    return request.content().asStream(ReadableBodyPart.class)
        .map(part -> {
          var path = storage.create(part.filename());
          part.content().map(DataChunk::data)
              .flatMapIterable(Arrays::asList)
              .to(IoMulti.writeToFile(path)
                  .executor(executor)
                  .build());
          return path;
        }).collectList()
        .map(files -> new Artefact(files.get(0), files.get(1)));
  }
}
