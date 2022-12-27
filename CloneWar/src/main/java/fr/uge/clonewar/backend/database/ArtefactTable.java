package fr.uge.clonewar.backend.database;

import io.helidon.dbclient.DbClient;

import java.util.Objects;

public class ArtefactTable {

  public record ArtefactRow(String jarName) {
    public ArtefactRow {
      Objects.requireNonNull(jarName);
    }
  }

  private final DbClient dbClient;

  public ArtefactTable(DbClient dbClient) {
    Objects.requireNonNull(dbClient);
    this.dbClient = dbClient;
    createTable();
  }

  private void createTable() {
    dbClient.execute(exec -> exec.update("CREATE TABLE IF NOT EXISTS artefact(id integer, jarName VARCHAR, PRIMARY KEY(id))"))
        .exceptionally(t -> {
          System.err.println(t.getMessage());
          return null;
        }).await();
  }

  public int insert(ArtefactRow artefact) {
    Objects.requireNonNull(artefact);
    return dbClient.execute(exec ->
            exec.query("INSERT INTO artefact(jarName) VALUES (?) RETURNING id", artefact.jarName)
        ).first()
        .map(row -> row.column("id").as(Integer.class))
        .await();
  }

}
