package fr.uge.clonewar;


import fr.uge.clonewar.backend.database.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class CloneDetectors {

  public static fr.uge.clonewar.backend.model.Artefact indexArtefact(Database db, Artefact artefact) throws IOException {
    Objects.requireNonNull(db);
    Objects.requireNonNull(artefact);

    var jarName = artefact.main().getFileName().toString();
    var now = System.currentTimeMillis();

    var artefactId = insertArtefact(db, jarName, now);

    var sources = ReadByteCode.extractSources(artefact.source());
    var files = db.fileTable().insertAll(convertToRow(artefactId, sources));
    var javaFiles = extractFilenames(sources);

    insertInstructions(db, artefact, files, javaFiles);
    return new fr.uge.clonewar.backend.model.Artefact(artefactId, jarName, now);
  }

  private static int insertArtefact(Database db, String jarName, long insertionDate) {
    var artefactId = db.artefactTable().insert(new ArtefactTable.ArtefactRow(jarName));
    db.detailTable().insert(new DetailTable.DetailRow(artefactId, insertionDate));
    return artefactId;
  }

  private static void insertInstructions(Database db, Artefact artefact, Map<String, Integer> files, Set<String> javaFiles) throws IOException {
    var readByteCode = new ReadByteCode(artefact.main());
    readByteCode.analyze(javaFiles);
    readByteCode.forEach((f, instruction) -> {
      var filename = ReadByteCode.extractExtension(f);
      var fileId = files.get(filename.getKey());
      if (fileId == null) {
        return;
      }

      var row = new InstructionTable.InstructionRow(instruction, fileId);
      db.instructionTable().bufferedInsert(row);
    });
    db.instructionTable().flushBuffer();
  }
  private static Set<String> extractFilenames(List<Map.Entry<String, String>> sources) {
    return sources.stream()
        .map(Map.Entry::getKey)
        .map(file -> ReadByteCode.extractExtension(file).getKey())
        .collect(Collectors.toSet());
  }

  private static List<FileTable.FileRow> convertToRow(int artefactId, List<Map.Entry<String, String>> sources) {
    return sources.stream()
        .map(entry -> {
          var file = ReadByteCode.extractExtension(entry.getKey());
          return new FileTable.FileRow(file.getKey(), file.getValue(), entry.getValue(), artefactId);
        })
        .toList();
  }

  public static void computeClones(Database db, int artefactId, String jarName) {
    Objects.requireNonNull(db);
    Objects.requireNonNull(jarName);
    var artefacts = db.detailTable().getAll(artefactId);
    var instructionsReference = db.instructionTable().getLineAndHash(jarName);

    for (var artefact : artefacts) {
      var instruction = db.instructionTable().getLineAndHash(artefact.name());
      var result = Karp.rabinKarp(instructionsReference, instruction);
      var succeed = result.getValue();
      var percentage = Karp.average(succeed, instructionsReference.size());
      db.cloneTable().insert(new CloneTable.CloneRow(artefactId, artefact.id(), (int)percentage));
    }
  }
}
