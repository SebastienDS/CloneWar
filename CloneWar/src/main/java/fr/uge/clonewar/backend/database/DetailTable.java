package fr.uge.clonewar.backend.database;

import fr.uge.clonewar.backend.model.Artefact;
import io.helidon.dbclient.DbClient;

import java.util.List;
import java.util.Objects;

/**
 * Represents a detail entity of the database.
 */
public class DetailTable {
  /**
   * Represents a Row of the Detail entity.
   * @param artefactId The id of the artefact to detail
   * @param insertionDate The date of insertion of the artefact
   */
  public record DetailRow(int artefactId, long insertionDate) {}

  private final DbClient dbClient;

  /**
   * Creates an instance of the entity.
   * @param dbClient The database connection
   */
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

  /**
   * Insert a row of the database.
   * @param detail The row to be inserted
   */
  public void insert(DetailRow detail) {
    Objects.requireNonNull(detail);
    dbClient.execute(exec -> exec.insert("INSERT INTO detail(artefactId, insertionDate) VALUES (?, ?)",
        detail.artefactId, detail.insertionDate)
    ).exceptionally((t -> {
      System.err.println(t.getMessage());
      return null;
    })).await();
  }

  /**
   * Gets all artefacts with details.
   * @return The list of artefacts
   */
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

  /**
   * Gets all artefacts without a given artefact.
   * @param withoutMe The id of the artefact to ignore 🥺
   * @return The list of artefacts
   */
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

  /**
   * Gets the artefact details of the given id.
   * @param id The id of an artefact
   * @return The selected artefact details
   */
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
