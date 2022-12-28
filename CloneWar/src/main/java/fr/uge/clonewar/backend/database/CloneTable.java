package fr.uge.clonewar.backend.database;

import fr.uge.clonewar.backend.model.Artefact;
import fr.uge.clonewar.backend.model.Clones;
import io.helidon.dbclient.DbClient;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CloneTable {
  public record CloneRow(int artefactId, int cloneId, int percentage) {}

  private final DbClient dbClient;

  public CloneTable(DbClient dbClient) {
    Objects.requireNonNull(dbClient);
    this.dbClient = dbClient;
    createTable();
  }

  private void createTable() {
    dbClient.execute(exec -> exec.update("CREATE TABLE IF NOT EXISTS clone(artefactId INTEGER, cloneId INTEGER," +
            "percentage INTEGER, PRIMARY KEY(artefactId, cloneId))"))
        .exceptionally(t -> {
          System.err.println(t.getMessage());
          return null;
        }).await();
  }

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

  public List<Clones.Clone> getAll(int id) {
    var query = """
        SELECT cloneId, jarName, percentage
        FROM clone
        JOIN artefact ON id = cloneId
        WHERE artefactId = ?
        ORDER BY percentage DESC
        """;
    return dbClient.execute(exec -> exec.query(query, id))
        .map(dbRow -> new Clones.Clone(
              new Artefact(
                dbRow.column("cloneId").as(Integer.class),
                dbRow.column("jarName").as(String.class)
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
