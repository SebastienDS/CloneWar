package fr.uge.clonewar.backend.model;

import java.util.List;
import java.util.Objects;

public record Diff(List<FileDiff> diff) {
  public record FileDiff(String reference, String file, List<DiffItem> diffs) {
    public FileDiff {
      Objects.requireNonNull(reference);
      Objects.requireNonNull(file);
      Objects.requireNonNull(diffs);
      diffs = List.copyOf(diffs);
    }
  }
  public record DiffItem(DiffComponent reference, DiffComponent file) {
    public DiffItem {
      Objects.requireNonNull(reference);
      Objects.requireNonNull(file);
    }
  }
  public record DiffComponent(int line, String content) {
    public DiffComponent {
      Objects.requireNonNull(content);
    }
  }

  public Diff {
    Objects.requireNonNull(diff);
    diff = List.copyOf(diff);
  }
}
