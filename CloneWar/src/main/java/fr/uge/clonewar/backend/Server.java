package fr.uge.clonewar.backend;

import fr.uge.clonewar.backend.ApiService;
import io.helidon.common.reactive.Single;
import io.helidon.config.Config;
import io.helidon.media.jsonp.JsonpSupport;
import io.helidon.media.multipart.MultiPartSupport;
import io.helidon.openapi.OpenAPISupport;
import io.helidon.webserver.Routing;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.staticcontent.StaticContentSupport;

public class Server {

  public static Single<WebServer> startServer() {
    var config = Config.create();
    var server = WebServer.builder(createRouting())
        .config(config.get("server"))
        .addMediaSupport(MultiPartSupport.create())
        .addMediaSupport(JsonpSupport.create())
        .build()
        .start();

    server.thenAccept(ws -> System.out.println("Server is up: http://localhost:" + ws.port()))
        .exceptionally(t -> {
          System.err.println("Startup failed: " + t.getMessage());
          t.printStackTrace(System.err);
          return null;
        });
    return server;
  }

  private static Routing createRouting() {
    var staticContent = StaticContentSupport.builder("/dist")
        .welcomeFileName("index.html")
        .build();

    return Routing.builder()
        .register(OpenAPISupport.create())
        .register("/api", new ApiService())
        .register("/", staticContent) // frontend/dist
        .build();
  }
}
