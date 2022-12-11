package fr.uge.clonewar.backend.database;

import io.helidon.dbclient.DbClient;

import javax.xml.transform.Source;
import java.util.Objects;

public class SourceTable {

  public record SourceRow(int line, String content, int fileId) {
    public SourceRow {
      Objects.requireNonNull(content);
    }
  }

  private final DbClient dbClient;

  public SourceTable(DbClient dbClient) {
    Objects.requireNonNull(dbClient);
    this.dbClient = dbClient;

    createTable();
  }

  private void createTable() {
    dbClient.execute(exec -> exec.update("CREATE TABLE IF NOT EXISTS source(id integer, line integer, content VARCHAR(200), fileId integer, PRIMARY KEY(id))"))
        .exceptionally(t -> {
          System.err.println(t.getMessage());
          return null;
        }).await();
  }

  public void add() {
  }

}
