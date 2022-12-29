package fr.uge.clonewar.backend.main;


import fr.uge.clonewar.backend.Server;
import io.helidon.config.Config;

public final class Main {

  public static void main(String[] args) {
    Server.startServer(Config.create().get("main"));
  }
}
