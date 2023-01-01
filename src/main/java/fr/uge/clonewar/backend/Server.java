package fr.uge.clonewar.backend;

import fr.uge.clonewar.backend.database.Database;
import io.helidon.common.reactive.Single;
import io.helidon.config.Config;
import io.helidon.dbclient.DbClient;
import io.helidon.media.jsonp.JsonpSupport;
import io.helidon.media.multipart.MultiPartSupport;
import io.helidon.openapi.OpenAPISupport;
import io.helidon.webserver.Routing;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.staticcontent.StaticContentSupport;

import java.util.Objects;

public class Server {

  /**
   * Start the server.
   * @param db The database instance
   * @param config The server config
   * @return The started server
   */
  public static Single<WebServer> startServer(Database db, Config config) {
    Objects.requireNonNull(db);
    Objects.requireNonNull(config);

    var storage = new FileStorage();

    var server = WebServer.builder(createRouting(db, storage))
        .config(config.get("server"))
        .addMediaSupport(MultiPartSupport.create())
        .addMediaSupport(JsonpSupport.create())
        .build()
        .start();

    server.thenAccept(ws -> {
          System.out.println("Server is up: http://localhost:" + ws.port());
          ws.whenShutdown().thenRun(storage::close);
        })
        .exceptionally(t -> {
          System.err.println("Startup failed: " + t.getMessage());
          t.printStackTrace(System.err);
          return null;
        });
    return server;
  }

  /**
   * Start the main server.
   * @param config The server config
   * @return The started server
   */
  public static Single<WebServer> startServer(Config config) {
    Objects.requireNonNull(config);
    var dbClient = DbClient.create(config.get("db"));
    var db = new Database(dbClient);
    return startServer(db, config);
  }

  private static Routing createRouting(Database db, FileStorage storage) {
    var staticContent = StaticContentSupport.builder("/dist")
        .welcomeFileName("index.html")
        .build();

    return Routing.builder()
        .register(OpenAPISupport.create())
        .register("/api", new ApiService(db, storage))
        .register("/", staticContent) // frontend/dist
        .build();
  }
}
