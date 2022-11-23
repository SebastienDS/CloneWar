package fr.uge.clonewar.backend;

import fr.uge.clonewar.ReadByteCode;
import io.helidon.dbclient.DbClient;
import io.helidon.dbclient.jdbc.JdbcDbClientProviderBuilder;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Objects;


public class InstructionDB {

    private final DbClient dbClient;

    public InstructionDB() {
        this.dbClient = JdbcDbClientProviderBuilder.create()
                .url("jdbc:sqlite:cloneWar.db")
                .build();
        ;
    }

    public void createTable() {
        dbClient.execute(exec -> exec.update("create table if not exists instruction (id integer, documentName VARCHAR(25), " +
                        "line integer, hash integer, PRIMARY KEY(id))"))
                .exceptionally(t -> {
                    System.out.println(t.getMessage());
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
            System.out.println(t.getMessage());
            return null;
        })).await();
    }

    private record Tuple(int line, int hash){}

    public void addToBase(ReadByteCode readByteCode) {
        var iterator = readByteCode.iterator();
        if(!iterator.hasNext()){
            return;
        }
        var hash = 0;
        var size = 50;
        var fifo = new ArrayDeque<Tuple>(size);
        for (int i = 0; i < size; i++) {
            if(iterator.hasNext()){
                hash = addHash(fifo, iterator, hash);
            }
        }
        add("cc", peek(fifo).line, hash);
        while (iterator.hasNext()){
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
        var lastElement = fifo.poll();
        assert lastElement != null;
        hash -= lastElement.hash;
        hash = addHash(fifo, iterator, hash);
        add("cc", peek(fifo).line, hash);
        return hash;
    }

    private static Tuple peek(ArrayDeque<Tuple> fifo) {
        return fifo.peek();
    }

    private Tuple getNextTuple(Iterator<ReadByteCode.Tuple> iterator){
        var nextElement = iterator.next();
        var nextElementHash = nextElement.opcode().hashCode();
        return new Tuple(nextElement.line(), nextElementHash);
    }
}
