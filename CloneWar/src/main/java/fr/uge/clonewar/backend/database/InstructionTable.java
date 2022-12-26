package fr.uge.clonewar.backend.database;

import fr.uge.clonewar.Instruction;
import io.helidon.common.reactive.CompletionAwaitable;
import io.helidon.dbclient.DbClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class InstructionTable {

  public record InstructionRow(Instruction instruction, int fileId) {
    public InstructionRow {
      Objects.requireNonNull(instruction);
    }
  }

  private static final int MAX_CAPACITY = 25_000;
  private final DbClient dbClient;
  private final ArrayList<InstructionRow> buffer = new ArrayList<>();

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

  public void insert(InstructionRow row) {
    Objects.requireNonNull(row);
    dbClient.execute(exec -> exec.createInsert("INSERT INTO instruction(line, hash, fileId) VALUES (?, ?, ?)")
        .addParam(row.instruction.line())
        .addParam(row.instruction.hash())
        .addParam(row.fileId)
        .execute()
    ).exceptionally((t -> {
      System.err.println(t.getMessage());
      return null;
    })).await();
  }

  public void insertAll(List<InstructionRow> rows) {
    Objects.requireNonNull(rows);
    if (rows.isEmpty()) {
      throw new IllegalArgumentException("Require instructions");
    }

    var chunkSize = 25_000;
    var size = rows.size();

    var awaitables = IntStream.range(0, (size + chunkSize - 1) / chunkSize)
        .mapToObj(i -> rows.subList(i * chunkSize, Math.min(chunkSize * (i + 1), size)))
        .map(chunk -> chunk.stream()
            .map(row -> "(" + row.instruction.line() + ", " + row.instruction.hash() + ", " + row.fileId + ")")
            .collect(Collectors.joining(", "))
        ).map(values ->
            dbClient.execute(exec -> exec.insert("INSERT INTO instruction(line, hash, fileId) VALUES " + values))
                .exceptionally((t -> {
                  t.printStackTrace();
                  return null;
                }))
        ).toList();

    awaitables.forEach(CompletionAwaitable::await);
  }

  public void bufferedInsert(InstructionRow instruction) {
    Objects.requireNonNull(instruction);

    buffer.add(instruction);

    if (buffer.size() >= MAX_CAPACITY) {
      flushBuffer();
    }
  }

  public void flushBuffer() {
    if (buffer.isEmpty()) {
      return;
    }

    var values = buffer.stream()
        .map(row -> "(" + row.instruction.line() + ", " + row.instruction.hash() + ", " + row.fileId + ")")
        .collect(Collectors.joining(", "));

    dbClient.execute(exec -> exec.insert("INSERT INTO instruction(line, hash, fileId) VALUES " + values))
        .exceptionally((t -> {
          t.printStackTrace();
          return null;
        })).await();

    buffer.clear();
  }

  public List<Instruction> getLineAndHash(String filename) {
    Objects.requireNonNull(filename);
    var query = """
      SELECT line, hash
      FROM file AS f
      JOIN artefact AS a ON f.artefactId = a.id
      JOIN instruction as i ON i.fileId = f.id
      WHERE a.jarName = ?
      """;
    return dbClient.execute(exec -> exec.query(query, filename))
        .map(dbRow -> new Instruction(
            dbRow.column("line").as(Integer.class),
            dbRow.column("hash").as(Integer.class))
        ).collectList()
        .exceptionally((t -> {
            t.printStackTrace();
          return null;
        })).await();
  }

}
