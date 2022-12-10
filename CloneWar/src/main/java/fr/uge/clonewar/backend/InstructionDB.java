package fr.uge.clonewar.backend;

import fr.uge.clonewar.ReadByteCode;
import io.helidon.dbclient.DbClient;
import io.helidon.dbclient.DbRow;
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
    var iterator = readByteCode.iterator();
    if (!iterator.hasNext()) {
      return;
    }
    var hash = 0;
    var size = 50;
    var fifo = new ArrayDeque<Tuple>(size);
    for (int i = 0; i < size; i++) {
      if (iterator.hasNext()) {
        hash = addHash(fifo, iterator, hash);
      }
    }
    add("cc2", fifo.remove().line, hash);
    while (iterator.hasNext()) {
      hash = rollingHash(fifo, iterator, hash);
    }
  }

  private int addHash(ArrayDeque<Tuple> fifo, Iterator<ReadByteCode.Tuple> iterator, int hash) {
    var nextTuple = getNextTuple(iterator);
    hash += nextTuple.hash;
    fifo.add(nextTuple);
    return hash;
  }

  private int rollingHash(ArrayDeque<Tuple> fifo, Iterator<ReadByteCode.Tuple> iterator, int hash) {
    var lastElement = fifo.remove();
    hash -= lastElement.hash;
    hash = addHash(fifo, iterator, hash);
    add("cc2", fifo.remove().line, hash);
    return hash;
  }

  private Tuple getNextTuple(Iterator<ReadByteCode.Tuple> iterator){
    var nextElement = iterator.next();
    var nextElementHash = nextElement.opcode().hashCode();
    return new Tuple(nextElement.line(), nextElementHash);
  }
}
