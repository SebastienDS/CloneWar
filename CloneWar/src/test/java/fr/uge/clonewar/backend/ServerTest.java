package fr.uge.clonewar.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.uge.clonewar.utils.JarBuilder;
import io.helidon.common.http.Http;
import io.helidon.common.http.MediaType;
import io.helidon.media.jsonp.JsonpSupport;
import io.helidon.media.multipart.FileFormParams;
import io.helidon.media.multipart.MultiPartSupport;
import io.helidon.webclient.WebClient;
import io.helidon.webserver.WebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ServerTest {
  private static WebServer webServer;
  private static WebClient webClient;

  @BeforeAll
  public static void startServer() {
    webServer = Server.startTestServer().await();

    webClient = WebClient.builder()
        .baseUri("http://localhost:" + webServer.port() + "/api")
        .addHeader(Http.Header.ACCEPT, MediaType.APPLICATION_JSON.toString())
        .addMediaSupport(MultiPartSupport.create())
        .addMediaSupport(JsonpSupport.create())
        .build();
  }

  @AfterAll
  public static void stopServer() {
    webServer.shutdown()
        .await(2, TimeUnit.SECONDS);
  }

  @Test
  public void testHelloWorld() {
    var response = webClient.get()
        .request()
        .await();

    var content = response.content().as(String.class).await();
    response.close();

    assertEquals(content, "Hello World");
    assertEquals(response.status(), Http.Status.OK_200);
  }

  @Test
  public void testUpload() throws IOException {
    try (var storage = new FileStorage()) {
      var jar = new JarBuilder(storage.storageDir(), "Test");
      jar.addFile("fr.uge.test.Test",
          """
          package fr.uge.test;
              
          public record Test(int a, int b) {
            private void cc() {
              System.out.println(a + b);
            }
          }
          """);
      var artefact = jar.get();

      var response = webClient.post()
          .path("/analyze")
          .contentType(MediaType.MULTIPART_FORM_DATA)
          .submit(FileFormParams.builder()
              .addFile("main", artefact.main().getFileName().toString(), artefact.main())
              .addFile("source", artefact.source().getFileName().toString(), artefact.source())
              .build())
          .await();

      var content = response.content().as(String.class).await();
      var mapper = new ObjectMapper();
      var obj = mapper.readValue(content, fr.uge.clonewar.backend.model.Artefact.class);

      assertEquals(response.status(), Http.Status.OK_200);
      assertEquals(obj.name(), artefact.main().getFileName().toString());
    }
  }
}
