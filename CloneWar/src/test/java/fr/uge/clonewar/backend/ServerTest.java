package fr.uge.clonewar.backend;

import io.helidon.common.http.Http;
import io.helidon.common.http.MediaType;
import io.helidon.webclient.WebClient;
import io.helidon.webserver.WebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ServerTest {
  private static WebServer webServer;
  private static WebClient webClient;

  @BeforeAll
  public static void startServer() {
    webServer = Server.startServer().await();

    webClient = WebClient.builder()
        .baseUri("http://localhost:" + webServer.port() + "/api")
        .addHeader(Http.Header.ACCEPT, MediaType.APPLICATION_JSON.toString())
        .build();
  }

  @AfterAll
  public static void stopServer() {
    webServer.shutdown()
        .await(2, TimeUnit.SECONDS);
  }

  @Test
  public void testHelloWorld() {
    webClient.get()
        .request()
        .thenAccept(response -> {
          var content = response.content().as(String.class).await();
          response.close();

          assertEquals(content, "Hello World");
          assertEquals(response.status(), Http.Status.OK_200);
        });
  }
}
