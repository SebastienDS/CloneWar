package fr.uge.clonewar.backend.database;

import io.helidon.dbclient.DbClient;

import java.util.Objects;

public class Database {
  private final ArtefactTable artefactTable;
  private final DetailTable detailTable;
  private final FileTable fileTable;
  private final InstructionTable instructionTable;
  private final SourceTable sourceTable;

  public Database(DbClient dbClient) {
    Objects.requireNonNull(dbClient);

    artefactTable = new ArtefactTable(dbClient);
    detailTable = new DetailTable(dbClient);
    fileTable = new FileTable(dbClient);
    instructionTable = new InstructionTable(dbClient);
    sourceTable = new SourceTable(dbClient);
  }

  public ArtefactTable artefactTable() {
    return artefactTable;
  }

  public DetailTable detailTable() {
    return detailTable;
  }

  public FileTable fileTable() {
    return fileTable;
  }

  public InstructionTable instructionTable() {
    return instructionTable;
  }

  public SourceTable sourceTable() {
    return sourceTable;
  }
}
