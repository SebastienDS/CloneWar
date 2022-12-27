package fr.uge.clonewar.backend;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import fr.uge.clonewar.ReadByteCode;
import fr.uge.clonewar.backend.database.ArtefactTable;
import fr.uge.clonewar.backend.database.Database;
import fr.uge.clonewar.backend.database.FileTable;
import fr.uge.clonewar.backend.database.InstructionTable;
import fr.uge.clonewar.backend.model.Artefact;
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
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ExecutorService;


public final class ApiService implements Service {
  private record Artefact(Path main, Path source) {
    public Artefact {
      Objects.requireNonNull(main);
      Objects.requireNonNull(source);
    }
  }

  private final Database db;
  private final FileStorage storage;
  private final ExecutorService executor = ThreadPoolSupplier.create("multipart-thread-pool").get();

  public ApiService(Database db, FileStorage storage) {
    Objects.requireNonNull(db);
    Objects.requireNonNull(storage);
    this.db = db;
    this.storage = storage;
  }

  @Override
  public void update(Routing.Rules rules) {
    rules.get("/", (req, res) -> res.send("Hello World"))
        .post("/analyze", this::analyze)
        .get("/artefacts", this::listArtefacts);
  }

  private void analyze(ServerRequest request, ServerResponse response) {
    downloadArtefact(request)
        .onError(response::send)
        .thenAccept(artefact -> {
          System.out.println("Analyzing... " + artefact);
          var jarName = artefact.main().getFileName().toString();

          var readByteCode = new ReadByteCode(artefact.main());
          try {
            readByteCode.analyze();

            var sources = ReadByteCode.extractSources(artefact.source());

            var artefactId = db.artefactTable().insert(
                new ArtefactTable.ArtefactRow(jarName)
            );

            var files = sources.stream()
                .map(entry -> {
                  var file = ReadByteCode.extractExtension(entry.getKey());
                  return new FileTable.FileRow(file.getKey(), file.getValue(), entry.getValue(), artefactId);
                })
                .toList();

            var map = db.fileTable().insertAll(files);

            readByteCode.forEach((f, instruction) -> {
              var filename = ReadByteCode.extractExtension(f);
              var fileId = map.get(filename.getKey());
              if (fileId == null) {
                return;
              }

              var row = new InstructionTable.InstructionRow(instruction, fileId);
              db.instructionTable().bufferedInsert(row);
            });

            db.instructionTable().flushBuffer();

            storage.delete(artefact.main());
            storage.delete(artefact.source());

            System.out.println("Done ! ");

            var insertedArtefact = new fr.uge.clonewar.backend.model.Artefact(artefactId, jarName);

            try {
              var json = toJson(insertedArtefact);
              response.status(Http.Status.OK_200).send(json);
            } catch (JsonProcessingException e) {
              throw new AssertionError(e);
            }
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

  private void listArtefacts(ServerRequest request, ServerResponse response) {
    var artefacts = db.detailTable().getAll();
    try {
      var json = toJson(artefacts);
      response.status(Http.Status.OK_200).send(json);
    } catch (JsonProcessingException e) {
      throw new AssertionError(e);
    }
  }

  private static String toJson(Object object) throws JsonProcessingException {
    var mapper = new ObjectMapper();
    return mapper.writeValueAsString(object);
  }

}
