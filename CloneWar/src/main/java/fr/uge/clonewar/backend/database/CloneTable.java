package fr.uge.clonewar.backend.database;

import fr.uge.clonewar.backend.model.Artefact;
import fr.uge.clonewar.backend.model.Clones;
import io.helidon.dbclient.DbClient;

import java.util.List;
import java.util.Objects;

/**
 * Represents a Clone entity of the database.
 */
public class CloneTable {
  /**
   * Represents a Row of the Clone entity.
   * @param artefactId The id of the reference artefact
   * @param cloneId The id of the artefact compared to
   * @param percentage The percentage of similarity
   */
  public record CloneRow(int artefactId, int cloneId, int percentage) {}

  private final DbClient dbClient;

  /**
   * Creates an instance of the entity.
   * @param dbClient The database connection
   */
  public CloneTable(DbClient dbClient) {
    Objects.requireNonNull(dbClient);
    this.dbClient = dbClient;
    createTable();
  }

  private void createTable() {
    dbClient.execute(exec -> exec.update("CREATE TABLE IF NOT EXISTS clone(id INTEGER, artefactId INTEGER, cloneId INTEGER," +
            "percentage INTEGER, PRIMARY KEY(id))"))
        .exceptionally(t -> {
          System.err.println(t.getMessage());
          return null;
        }).await();
  }

  /**
   * Insert a row to the database.
   * @param clone The row to be inserted
   */
  public void insert(CloneRow clone) {
    Objects.requireNonNull(clone);
    dbClient.execute(exec -> exec.createInsert("INSERT INTO clone(artefactId, cloneId, percentage) VALUES (?, ?, ?)")
        .addParam(clone.artefactId)
        .addParam(clone.cloneId)
        .addParam(clone.percentage)
        .execute()
    ).exceptionally((t -> {
      System.err.println(t.getMessage());
      return null;
    })).await();
  }

  /**
   * Gets all clones for a given artefact id.
   * @param artefactId The id of an artefact
   * @return The list of clones
   */
  public List<Clones.Clone> getAll(int artefactId) {
    var query = """
        SELECT cloneId, jarName, insertionDate, percentage
        FROM clone AS c
        JOIN artefact AS a ON a.id = cloneId
        WHERE artefactId = ?
        ORDER BY percentage DESC, jarName ASC
        """;
    return dbClient.execute(exec -> exec.query(query, artefactId))
        .map(dbRow ->
          new Clones.Clone(
            new Artefact(
              dbRow.column("cloneId").as(Integer.class),
              dbRow.column("jarName").as(String.class),
              dbRow.column("insertionDate").as(Long.class)
            ),
            dbRow.column("percentage").as(Integer.class)
          )
        ).collectList()
        .exceptionally((t -> {
          t.printStackTrace();
          return null;
        })).await();
  }


}
