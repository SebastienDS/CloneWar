package fr.uge.clonewar.backend.main;

import fr.uge.clonewar.backend.ApiService;
import io.helidon.config.Config;
import io.helidon.openapi.OpenAPISupport;
import io.helidon.webserver.Routing;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.staticcontent.StaticContentSupport;

public final class Main {

  public static void main(String[] args) {
    var config = Config.create();
    var server = WebServer.builder(createRouting())
        .config(config.get("server"))
        .build()
        .start();

    server.thenAccept(ws -> System.out.println("Server is up: http://localhost:" + ws.port() + "/index.html"));
  }

  private static Routing createRouting() {
    return Routing.builder()
        .register(OpenAPISupport.create())
        .register("/", StaticContentSupport.create("/dist")) // frontend/dist
        .register("/api", new ApiService())
        .build();
  }
}
