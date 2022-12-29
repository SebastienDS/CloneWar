package fr.uge.clonewar.backend.database;

import io.helidon.dbclient.DbClient;

import java.util.Objects;

/**
 * Represents a Database.
 */
public class Database {
  private final ArtefactTable artefactTable;
  private final DetailTable detailTable;
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
    detailTable = new DetailTable(dbClient);
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
   * Gets the detail table.
   * @return The detail table
   */
  public DetailTable detailTable() {
    return detailTable;
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
