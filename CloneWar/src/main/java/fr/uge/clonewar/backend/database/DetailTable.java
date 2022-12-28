package fr.uge.clonewar.backend.database;

import fr.uge.clonewar.backend.model.Artefact;
import io.helidon.dbclient.DbClient;

import java.util.List;
import java.util.Objects;

public class DetailTable {
  public record DetailRow(int artefactId, long insertionDate) {}

  private final DbClient dbClient;

  public DetailTable(DbClient dbClient) {
    Objects.requireNonNull(dbClient);
    this.dbClient = dbClient;
    createTable();
  }

  private void createTable() {
    dbClient.execute(exec -> exec.update("CREATE TABLE IF NOT EXISTS detail(artefactId INTEGER, insertionDate INTEGER, PRIMARY KEY(artefactId))"))
        .exceptionally(t -> {
          System.err.println(t.getMessage());
          return null;
        }).await();
  }

  public void insert(DetailRow detail) {
    Objects.requireNonNull(detail);
    dbClient.execute(exec -> exec.insert("INSERT INTO detail(artefactId, insertionDate) VALUES (?, ?)",
        detail.artefactId, detail.insertionDate)
    ).exceptionally((t -> {
      System.err.println(t.getMessage());
      return null;
    })).await();
  }

  public List<Artefact> getAll() {
    return dbClient.execute(exec -> exec.query("SELECT id, jarName, insertionDate FROM artefact JOIN detail ON id = artefactId"))
        .map(dbRow -> new Artefact(
            dbRow.column("id").as(Integer.class),
            dbRow.column("jarName").as(String.class),
            dbRow.column("insertionDate").as(Long.class))
        ).collectList()
        .exceptionally((t -> {
          t.printStackTrace();
          return null;
        })).await();
  }

  public List<Artefact> getAll(int withoutMe) {
    return dbClient.execute(exec -> exec.query("SELECT id, jarName, insertionDate FROM artefact JOIN detail ON id = artefactId WHERE id != ?", withoutMe))
        .map(dbRow -> new Artefact(
            dbRow.column("id").as(Integer.class),
            dbRow.column("jarName").as(String.class),
            dbRow.column("insertionDate").as(Long.class))
        ).collectList()
        .exceptionally((t -> {
          t.printStackTrace();
          return null;
        })).await();
  }

  public Artefact get(int id) {
    return dbClient.execute(exec -> exec.query("SELECT id, jarName, insertionDate FROM artefact JOIN detail ON id = artefactId WHERE id = ?", id))
        .first()
        .map(dbRow -> new Artefact(
            dbRow.column("id").as(Integer.class),
            dbRow.column("jarName").as(String.class),
            dbRow.column("insertionDate").as(Long.class))
        ).exceptionally((t -> {
          t.printStackTrace();
          return null;
        })).await();
  }
}
