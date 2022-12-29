package fr.uge.clonewar.backend.database;

import fr.uge.clonewar.Instruction;
import io.helidon.common.reactive.CompletionAwaitable;
import io.helidon.dbclient.DbClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * Represents an Instruction entity of the database.
 */
public class InstructionTable {
  /**
   * Represents a Row of the Instruction entity.
   * @param instruction The instruction
   * @param fileId the fileId
   */
  public record InstructionRow(Instruction instruction, int fileId) {
    public InstructionRow {
      Objects.requireNonNull(instruction);
    }
  }

  private static final int MAX_CAPACITY = 25_000;
  private final DbClient dbClient;
  private final ArrayList<InstructionRow> buffer = new ArrayList<>();

  /**
   * Creates an instance of the entity.
   * @param dbClient The database connection
   */
  public InstructionTable(DbClient dbClient) {
    Objects.requireNonNull(dbClient);
    this.dbClient = dbClient;
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

  /**
   * Insert a row to the database.
   * @param row The row to be inserted
   */
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

  /**
   * Insert a row to a buffer that will make a unique insertion when he will be full.
   * Prevents database spamming due to massive insertions
   * @param instruction The row to be inserted
   */
  public void bufferedInsert(InstructionRow instruction) {
    Objects.requireNonNull(instruction);

    buffer.add(instruction);

    if (buffer.size() >= MAX_CAPACITY) {
      flushBuffer();
    }
  }

  /**
   * Flush the buffer and insert instructions to database.
   */
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

  /**
   * Gets instructions of a given artefact.
   * @return The list of instructions
   */
  public List<Instruction> getAll(int artefactId) {
    var query = """
      SELECT line, hash
      FROM artefact AS a
      JOIN file AS f ON a.id = f.artefactId
      JOIN instruction AS i ON f.id = i.fileId
      WHERE a.id = ?
      """;
    return dbClient.execute(exec -> exec.query(query, artefactId))
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
