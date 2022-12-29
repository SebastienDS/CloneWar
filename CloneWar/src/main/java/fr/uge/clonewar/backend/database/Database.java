package fr.uge.clonewar.backend.database;

import io.helidon.dbclient.DbClient;

import java.util.Objects;

/**
 * Represents a Database.
 */
public class Database {
  private final ArtefactTable artefactTable;
  private final FileTable fileTable;
  private final InstructionTable instructionTable;
  private final CloneTable cloneTable;

  /**
   * Creates an instance of the database.
   * @param dbClient The database connection
   */
  public Database(DbClient dbClient) {
    Objects.requireNonNull(dbClient);

    artefactTable = new ArtefactTable(dbClient);
    fileTable = new FileTable(dbClient);
    instructionTable = new InstructionTable(dbClient);
    cloneTable = new CloneTable(dbClient);
  }

  /**
   * Gets the artefact table.
   * @return The artefact table
   */
  public ArtefactTable artefactTable() {
    return artefactTable;
  }

  /**
   * Gets the file table.
   * @return The file table
   */
  public FileTable fileTable() {
    return fileTable;
  }

  /**
   * Gets the instruction table.
   * @return The instruction table
   */
  public InstructionTable instructionTable() {
    return instructionTable;
  }

  /**
   * Gets the clone table.
   * @return The clone table
   */
  public CloneTable cloneTable() {
    return cloneTable;
  }

}
