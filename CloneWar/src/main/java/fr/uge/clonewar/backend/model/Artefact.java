package fr.uge.clonewar.backend.model;


import java.util.Objects;

public record Artefact(int id, String name, long insertionDate) {
  public Artefact {
    Objects.requireNonNull(name);
  }
}

//    var obj = mapper.readValue(json, new TypeReference<List<Artefact>>() {});
//    System.out.println(obj);