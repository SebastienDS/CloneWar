package fr.uge.clonewar.backend;

import io.helidon.webserver.Routing;
import io.helidon.webserver.Service;

public class ApiService implements Service {

  @Override
  public void update(Routing.Rules rules) {
    rules.get("/", (req, res) -> res.send("Hello World"));
  }

}
