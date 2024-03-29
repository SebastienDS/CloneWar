package fr.uge.clonewar;


import fr.uge.clonewar.backend.database.*;
import fr.uge.clonewar.backend.database.ArtefactTable.ArtefactRow;
import fr.uge.clonewar.backend.database.CloneTable.CloneRow;
import fr.uge.clonewar.backend.database.FileTable.FileRow;
import fr.uge.clonewar.backend.database.InstructionTable.InstructionRow;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class CloneDetectors {

  /**
   * Index artefact in the database
   * @param db The database
   * @param artefact The artefact to be indexed
   * @return The artefact details
   * @throws IOException if an I/O error occurs
   */
  public static fr.uge.clonewar.backend.model.Artefact indexArtefact(Database db, Artefact artefact) throws IOException {
    Objects.requireNonNull(db);
    Objects.requireNonNull(artefact);

    var jarName = artefact.main().getFileName().toString();
    var now = System.currentTimeMillis();

    var artefactId = db.artefactTable().insert(new ArtefactRow(jarName, now));

    var sources = ReadByteCode.extractSources(artefact.source());
    var files = insertFiles(db, artefactId, sources);

    insertInstructions(db, artefact, files);
    return new fr.uge.clonewar.backend.model.Artefact(artefactId, jarName, now);
  }

  private static Map<String, Integer> insertFiles(Database db, int artefactId, List<Map.Entry<String, String>> sources) {
    return sources.stream()
        .map(entry -> {
          var file = ReadByteCode.extractExtension(entry.getKey());
          return new FileRow(file.getKey(), file.getValue(), entry.getValue(), artefactId);
        })
        .map(row -> {
          var fileId = db.fileTable().insert(row);
          return Map.entry(row.filename(), fileId);
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private static void insertInstructions(Database db, Artefact artefact, Map<String, Integer> files) throws IOException {
    var readByteCode = new ReadByteCode(artefact.main());
    readByteCode.analyze(files.keySet());
    readByteCode.forEach((f, instruction) -> {
      var filename = ReadByteCode.extractExtension(f);
      var fileId = files.get(filename.getKey());
      if (fileId == null) {
        return;
      }

      var row = new InstructionRow(instruction, fileId);
      db.instructionTable().bufferedInsert(row);
    });
    db.instructionTable().flushBuffer();
  }

  /**
   * Computes indexed artefacts similarity.
   * @param db The database
   * @param reference The reference
   * @param toCompute Artefacts to compute
   */
  public static void computeClones(Database db, fr.uge.clonewar.backend.model.Artefact reference, List<fr.uge.clonewar.backend.model.Artefact> toCompute) {
    Objects.requireNonNull(db);
    Objects.requireNonNull(reference);
    Objects.requireNonNull(toCompute);
    var instructionsReference = db.instructionTable().getAll(reference.id());

    for (var artefact : toCompute) {
      var instruction = db.instructionTable().getAll(artefact.id());
      var result = Karp.rabinKarp(instruction, instructionsReference);
      var percentage = Karp.average(result.getValue(), instruction.size());

      db.cloneTable().insert(new CloneRow(reference.id(), artefact.id(), (int)percentage));
      insertDiff(db, result.getKey());
    }
  }

  private static void insertDiff(Database db, HashMap<InstructionRow, Set<InstructionRow>> result) {
    result.entrySet()
        .stream()
        .flatMap(e -> {
          var ref = e.getKey();
          return e.getValue()
              .stream()
              .map(i -> new DiffTable.DiffRow(ref.fileId(), i.fileId(), ref.instruction().line(), i.instruction().line()));
        }).forEach(db.diffTable()::insert);
  }
}
