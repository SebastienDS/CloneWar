package fr.uge.clonewar.backend.main;

import fr.uge.clonewar.backend.ApiService;
import io.helidon.config.Config;
import io.helidon.media.jsonp.JsonpSupport;
import io.helidon.media.multipart.MultiPartSupport;
import io.helidon.openapi.OpenAPISupport;
import io.helidon.webserver.Routing;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.staticcontent.StaticContentSupport;

public final class Main {

  public static void main(String[] args) {
    var config = Config.create();
    var server = WebServer.builder(createRouting())
        .config(config.get("server"))
        .addMediaSupport(MultiPartSupport.create())
        .addMediaSupport(JsonpSupport.create())
        .build()
        .start();

    server.thenAccept(ws -> System.out.println("Server is up: http://localhost:" + ws.port()));
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
