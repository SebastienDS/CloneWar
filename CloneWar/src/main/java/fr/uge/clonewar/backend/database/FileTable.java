package fr.uge.clonewar.backend.database;

import io.helidon.dbclient.DbClient;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class FileTable {

  public record FileRow(String filename, String extension, String content, int artefactId) {
    public FileRow {
      Objects.requireNonNull(filename);
      Objects.requireNonNull(extension);
      Objects.requireNonNull(content);
    }
  }

  private final DbClient dbClient;

  public FileTable(DbClient dbClient) {
    Objects.requireNonNull(dbClient);
    this.dbClient = dbClient;

    createTable();
  }

  private void createTable() {
    dbClient.execute(exec -> exec.update("CREATE TABLE IF NOT EXISTS file(id integer, filename VARCHAR, " +
            "extension VARCHAR, content TEXT, artefactId integer, PRIMARY KEY(id))"))
        .exceptionally(t -> {
          System.err.println(t.getMessage());
          return null;
        }).await();
  }

  public int insert(FileRow file) {
    Objects.requireNonNull(file);
    return dbClient.execute(exec ->
            exec.query("INSERT INTO file(filename, extension, content, artefactId) VALUES (?, ?, ?, ?) RETURNING id",
                file.filename, file.extension, file.content, file.artefactId)
        ).first()
        .map(row -> row.column("id").as(Integer.class))
        .await();
  }

  public Map<String, Integer> insertAll(List<FileRow> files) {
    Objects.requireNonNull(files);

    return files.stream()
        .map(row -> Map.entry(row.filename, insert(row)))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

}
