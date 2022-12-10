package fr.uge.clonewar.backend;

import fr.uge.clonewar.ReadByteCode;
import io.helidon.dbclient.DbClient;
import io.helidon.dbclient.jdbc.JdbcDbClientProviderBuilder;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;


public class InstructionDB {

  private final DbClient dbClient;

  public InstructionDB() {
    this.dbClient = JdbcDbClientProviderBuilder.create()
        .url("jdbc:sqlite:cloneWar.db")
        .build();
  }

  public void createTable() {
    dbClient.execute(exec -> exec.update("create table if not exists instruction (id integer, documentName VARCHAR(25), " +
            "line integer, hash integer, PRIMARY KEY(id))"))
        .exceptionally(t -> {
          System.err.println(t.getMessage());
          return null;
        }).await();
  }

  public void add(String name, int line, int hash) {
    Objects.requireNonNull(name);
    dbClient.execute(exec -> exec.createInsert("Insert into Instruction(documentName, line, hash) values (?, ?, ?)")
        .addParam(name)
        .addParam(line)
        .addParam(hash)
        .execute()
    ).exceptionally((t -> {
      System.err.println(t.getMessage());
      return null;
    })).await();
  }

  public List<Tuple> getLineAndHash(String documentName) {
    Objects.requireNonNull(documentName);
    return dbClient.execute(exec -> exec.createQuery("SELECT line, hash FROM INSTRUCTION WHERE documentName = ?")
            .addParam(documentName)
            .execute()
        ).map(dbRow -> new Tuple(
            dbRow.column("line").as(Integer.class),
            dbRow.column("hash").as(Integer.class))
        ).collectList()
        .exceptionally((t -> {
          System.err.println(t.getMessage());
          return null;
        })).await();
  }

  public record Tuple(int line, int hash) {}

  public void addToBase(ReadByteCode readByteCode) {
    Objects.requireNonNull(readByteCode);
    readByteCode.stream()
        .forEach(entry -> addToBase(entry.getKey(), entry.getValue()));
  }

  private void addToBase(String filename, Iterator<ReadByteCode.Tuple> instructions) {
    if (!instructions.hasNext()) {
      return;
    }
    var hash = 0;
    var size = 50;
    var fifo = new ArrayDeque<Tuple>(size);
    for (int i = 0; i < size; i++) {
      if (instructions.hasNext()) {
        hash = addHash(fifo, instructions, hash);
      }
    }
    add(filename, peek(fifo).line, hash);
    while (instructions.hasNext()) {
      hash = rollingHash(fifo, filename, instructions, hash);
    }
  }

  private static Tuple peek(ArrayDeque<Tuple> fifo) {
    return fifo.peek();
  }

  private int addHash(ArrayDeque<Tuple> fifo, Iterator<ReadByteCode.Tuple> iterator, int hash) {
    var nextTuple = getNextTuple(iterator);
    hash += nextTuple.hash;
    fifo.add(nextTuple);
    return hash;
  }

  private int rollingHash(ArrayDeque<Tuple> fifo, String filename, Iterator<ReadByteCode.Tuple> iterator, int hash) {
    var lastElement = fifo.remove();
    hash -= lastElement.hash;
    hash = addHash(fifo, iterator, hash);
    add(filename, peek(fifo).line, hash);
    return hash;
  }

  private Tuple getNextTuple(Iterator<ReadByteCode.Tuple> iterator){
    var nextElement = iterator.next();
    var nextElementHash = nextElement.opcode().hashCode();
    return new Tuple(nextElement.line(), nextElementHash);
  }
}
