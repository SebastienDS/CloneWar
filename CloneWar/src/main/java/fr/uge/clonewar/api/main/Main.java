package fr.uge.clonewar.api.main;

import fr.uge.clonewar.api.ApiService;
import io.helidon.config.Config;
import io.helidon.openapi.OpenAPISupport;
import io.helidon.webserver.Routing;
import io.helidon.webserver.WebServer;

public final class Main {

  public static void main(String[] args) {
    var config = Config.create();
    var server = WebServer.builder(createRouting())
        .config(config.get("server"))
        .build()
        .start();

    server.thenAccept(ws -> System.out.println("Server is up: http://localhost:" + ws.port()));
  }

  private static Routing createRouting() {
    return Routing.builder()
        .register(OpenAPISupport.create())
        .register("/", (rules) -> rules.get("/", (req, res) -> res.send("Static files should be sent here")))
        .register("/api", new ApiService())
        .build();
  }
}
