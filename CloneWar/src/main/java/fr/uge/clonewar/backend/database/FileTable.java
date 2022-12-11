package fr.uge.clonewar.backend.database;

import io.helidon.common.reactive.Single;
import io.helidon.dbclient.DbClient;

import java.util.Objects;

public class FileTable {

  public record FileRow(String filename, int artefactId) {
    public FileRow {
      Objects.requireNonNull(filename);
    }
  }

  private final DbClient dbClient;

  public FileTable(DbClient dbClient) {
    Objects.requireNonNull(dbClient);
    this.dbClient = dbClient;

    createTable();
  }

  private void createTable() {
    dbClient.execute(exec -> exec.update("CREATE TABLE IF NOT EXISTS file(id integer, filename VARCHAR(25), artefactId integer, PRIMARY KEY(id))"))
        .exceptionally(t -> {
          System.err.println(t.getMessage());
          return null;
        }).await();
  }

  public int insert(FileRow file) {
    Objects.requireNonNull(file);
    return dbClient.execute(exec ->
            exec.query("INSERT INTO file(filename, artefactId) VALUES (?, ?) RETURNING id", file.filename, file.artefactId)
        ).first()
        .map(row -> row.column("id").as(Integer.class))
        .await();
  }

}
