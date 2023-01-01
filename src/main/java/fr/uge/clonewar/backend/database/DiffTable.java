package fr.uge.clonewar.backend.database;

import fr.uge.clonewar.backend.model.Diff;
import fr.uge.clonewar.backend.model.Diff.DiffItem;
import fr.uge.clonewar.backend.model.Diff.FileDiff;
import io.helidon.dbclient.DbClient;

import java.util.List;
import java.util.Objects;

/**
 * Represents a Diff entity of the database.
 */
public class DiffTable {
  /**
   * Represents a Row of the Diff entity.
   * @param referenceId The id of the reference file
   * @param cloneId The id of the file compared to
   * @param lineReference The line reference
   * @param lineClone The line clone
   */
  public record DiffRow(int referenceId, int cloneId, int lineReference, int lineClone) {}

  private final DbClient dbClient;

  /**
   * Creates an instance of the entity.
   * @param dbClient The database connection
   */
  public DiffTable(DbClient dbClient) {
    Objects.requireNonNull(dbClient);
    this.dbClient = dbClient;
    createTable();
  }

  private void createTable() {
    dbClient.execute(exec -> exec.update("CREATE TABLE IF NOT EXISTS diff(id INTEGER, referenceId INTEGER, " +
            "cloneId INTEGER, lineReference INTEGER, lineClone INTEGER, PRIMARY KEY(id))")
        ).exceptionally(t -> {
          System.err.println(t.getMessage());
          return null;
        }).await();
  }

  /**
   * Insert a row to the database.
   * @param row The row to be inserted
   */
  public void insert(DiffRow row) {
    Objects.requireNonNull(row);
    dbClient.execute(exec -> exec.insert("INSERT INTO diff(referenceId, cloneId, lineReference, lineClone) VALUES (?, ?, ?, ?)",
            row.referenceId, row.cloneId, row.lineReference, row.lineClone)
    ).exceptionally((t -> {
      System.err.println(t.getMessage());
      return null;
    })).await();
  }

  /**
   * Gets the diff of those artefacts
   *
   * @param referenceId The id of the reference artefact
   * @param cloneId     The id of the tested artefact
   * @return The diff
   */
  public List<FileDiff> getDiff(int referenceId, int cloneId) {
    var query = """
        SELECT DISTINCT referenceId, f1.filename AS reference, f1.content AS refContent, cloneId, f2.filename AS file, f2.content AS fileContent
        FROM diff
        JOIN file AS f1 ON f1.id = referenceId
        JOIN file AS f2 ON f2.id = cloneId
        WHERE f1.artefactId = ? AND f2.artefactId = ?
        ORDER BY reference, file
        """;
    return dbClient.execute(exec -> exec.query(query, referenceId, cloneId))
        .map(row -> {
          var refId = row.column("referenceId").as(Integer.class);
          var reference = row.column("reference").as(String.class);
          var refContent = row.column("refContent").as(String.class);

          var fileId = row.column("cloneId").as(Integer.class);
          var file = row.column("file").as(String.class);
          var fileContent = row.column("fileContent").as(String.class);

          var diffs = getFileDiff(refId, refContent, fileId, fileContent);
          return new FileDiff(reference, file, diffs);
        }).collectList()
        .exceptionally((t -> {
          t.printStackTrace();
          return null;
        })).await();
  }

  private List<DiffItem> getFileDiff(int referenceId, String refContent, int cloneId, String fileContent) {
    var refLines = refContent.lines().toList();
    var fileLines = fileContent.lines().toList();
    var query = """
        SELECT DISTINCT lineReference, lineClone
        FROM diff
        WHERE referenceId = ? AND cloneId = ?
        ORDER BY lineReference, lineClone
        """;
    return dbClient.execute(exec -> exec.query(query, referenceId, cloneId))
        .map(row -> {
          var lineReference = row.column("lineReference").as(Integer.class);
          var lineClone = row.column("lineClone").as(Integer.class);

          return new DiffItem(
              new Diff.DiffComponent(lineReference, refLines.get(lineReference - 1)),
              new Diff.DiffComponent(lineClone, fileLines.get(lineClone - 1))
          );
        }).collectList()
        .exceptionally((t -> {
          t.printStackTrace();
          return null;
        })).await();
  }

}
