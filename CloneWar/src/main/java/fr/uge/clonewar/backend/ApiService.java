package fr.uge.clonewar.backend;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.uge.clonewar.Artefact;
import fr.uge.clonewar.CloneDetectors;
import fr.uge.clonewar.backend.database.Database;
import fr.uge.clonewar.backend.model.Clones;
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
import java.util.Objects;
import java.util.concurrent.ExecutorService;


public final class ApiService implements Service {
  private final Database db;
  private final FileStorage storage;
  private final ExecutorService executor = ThreadPoolSupplier.create("multipart-thread-pool").get();

  public ApiService(Database db, FileStorage storage) {
    Objects.requireNonNull(db);
    Objects.requireNonNull(storage);
    this.db = db;
    this.storage = storage;
  }

  /**
   * Define routes for the service.
   * @param rules Server rules that contains routes
   */
  @Override
  public void update(Routing.Rules rules) {
    rules.get("/", (req, res) -> res.send("Hello World"))
        .post("/analyze", (req, res) -> interceptError(req, res, this::analyze))
        .get("/artefacts", (req, res) -> interceptError(req, res, this::listArtefacts))
        .get("/clones/{id}", (req, res) -> interceptError(req, res, this::listClones));
  }

  @FunctionalInterface
  private interface Handler {
    void handle(ServerRequest request, ServerResponse response) throws Exception;
  }

  private static void interceptError(ServerRequest request, ServerResponse response, Handler consumer) {
    try {
      consumer.handle(request, response);
    } catch (Throwable e) {
      e.printStackTrace();
      response.send(e);
    }
  }

  private void analyze(ServerRequest request, ServerResponse response) {
    downloadArtefact(request)
        .onError(response::send)
        .thenAccept(artefact -> {
          try {
            System.out.println("Indexing artefact ... ");
            var indexedArtefact = CloneDetectors.indexArtefact(db, artefact);
            System.out.println("Computing clones ... ");
            CloneDetectors.computeClones(db, indexedArtefact.id());

            var json = toJson(indexedArtefact);
            response.status(Http.Status.OK_200).send(json);
          } catch (IOException e) {
            response.send(e);
          }
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

  private void listArtefacts(ServerRequest request, ServerResponse response) throws IOException {
    var artefacts = db.detailTable().getAll();
    var json = toJson(artefacts);
    response.status(Http.Status.OK_200).send(json);
  }

  private void listClones(ServerRequest request, ServerResponse response) throws IOException {
    var id = Integer.parseInt(request.path().param("id"));
    var clones = db.cloneTable().getAll(id);
    var reference = db.detailTable().get(id);
    var json = toJson(new Clones(reference, clones));
    response.status(Http.Status.OK_200).send(json);
  }

  private static String toJson(Object object) throws JsonProcessingException {
    var mapper = new ObjectMapper();
    return mapper.writeValueAsString(object);
  }

}
