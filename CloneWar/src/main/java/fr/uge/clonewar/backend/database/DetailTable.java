package fr.uge.clonewar.backend.database;

import io.helidon.dbclient.DbClient;

import java.util.Objects;

public class DetailTable {

  public record DetailRow(String author, int artefactId) {
    public DetailRow {
      Objects.requireNonNull(author);
    }
  }

  private final DbClient dbClient;

  public DetailTable(DbClient dbClient) {
    Objects.requireNonNull(dbClient);
    this.dbClient = dbClient;

    createTable();
  }

  private void createTable() {
    dbClient.execute(exec -> exec.update("CREATE TABLE IF NOT EXISTS detail(id integer, author VARCHAR(25), artefactId integer, PRIMARY KEY(id))"))
        .exceptionally(t -> {
          System.err.println(t.getMessage());
          return null;
        }).await();
  }

  public void insert(DetailRow detail) {
    Objects.requireNonNull(detail);
  }

}
