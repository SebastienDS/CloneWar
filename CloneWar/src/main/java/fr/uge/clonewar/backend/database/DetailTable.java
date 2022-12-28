package fr.uge.clonewar.backend.database;

import fr.uge.clonewar.backend.model.Artefact;
import io.helidon.dbclient.DbClient;

import java.util.List;
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
    dbClient.execute(exec -> exec.update("CREATE TABLE IF NOT EXISTS detail(id integer, author VARCHAR, artefactId integer, PRIMARY KEY(id))"))
        .exceptionally(t -> {
          System.err.println(t.getMessage());
          return null;
        }).await();
  }

  public void insert(DetailRow detail) {
    Objects.requireNonNull(detail);
    throw new UnsupportedOperationException();
  }

  public List<Artefact> getAll() {
    return dbClient.execute(exec -> exec.query("SELECT id, jarName FROM artefact"))
        .map(dbRow -> new Artefact(
            dbRow.column("id").as(Integer.class),
            dbRow.column("jarName").as(String.class))
        ).collectList()
        .exceptionally((t -> {
          t.printStackTrace();
          return null;
        })).await();
  }

  public List<Artefact> getAll(int withoutMe) {
    return dbClient.execute(exec -> exec.query("SELECT id, jarName FROM artefact WHERE id != ?", withoutMe))
        .map(dbRow -> new Artefact(
            dbRow.column("id").as(Integer.class),
            dbRow.column("jarName").as(String.class))
        ).collectList()
        .exceptionally((t -> {
          t.printStackTrace();
          return null;
        })).await();
  }

  public Artefact get(int id) {
    return dbClient.execute(exec -> exec.query("SELECT id, jarName FROM artefact WHERE id = ?", id))
        .first()
        .map(dbRow -> new Artefact(
            dbRow.column("id").as(Integer.class),
            dbRow.column("jarName").as(String.class))
        ).exceptionally((t -> {
          t.printStackTrace();
          return null;
        })).await();
  }
}
