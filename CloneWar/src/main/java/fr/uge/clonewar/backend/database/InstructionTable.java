package fr.uge.clonewar.backend.database;

import io.helidon.dbclient.DbClient;

import java.util.List;
import java.util.Objects;


public class InstructionTable {

  public record InstructionRow(int line, int hash, int fileId) {}

  private final DbClient dbClient;

  public InstructionTable(DbClient dbclient) {
    Objects.requireNonNull(dbclient);
    this.dbClient = dbclient;

    createTable();
  }

  private void createTable() {
    dbClient.execute(exec -> exec.update("CREATE TABLE IF NOT EXISTS instruction(id integer, " +
            "line integer, hash integer, fileId integer, PRIMARY KEY(id))"))
        .exceptionally(t -> {
          System.err.println(t.getMessage());
          return null;
        }).await();
  }

  public void insert(InstructionRow instruction) {
    Objects.requireNonNull(instruction);
    dbClient.execute(exec -> exec.createInsert("INSERT INTO instruction(line, hash, fileId) VALUES (?, ?, ?)")
        .addParam(instruction.line)
        .addParam(instruction.hash)
        .addParam(instruction.fileId)
        .execute()
    ).exceptionally((t -> {
      System.err.println(t.getMessage());
      return null;
    })).await();
  }

  public List<Tuple> getLineAndHash(String filename) {
    Objects.requireNonNull(filename);
    var query = """
      SELECT line, hash
      FROM file AS f
      NATURAL JOIN instruction AS i ON f.id = i.fileId
      WHERE filename = ?
      """;
    return dbClient.execute(exec -> exec.query(query, filename))
        .map(dbRow -> new Tuple(
            dbRow.column("line").as(Integer.class),
            dbRow.column("hash").as(Integer.class))
        ).collectList()
        .exceptionally((t -> {
          System.err.println(t.getMessage());
          return null;
        })).await();
  }

  public record Tuple(int line, int hash) {}

}
