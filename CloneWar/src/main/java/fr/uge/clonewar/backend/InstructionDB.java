package fr.uge.clonewar.backend;

import io.helidon.dbclient.DbClient;
import io.helidon.dbclient.jdbc.JdbcDbClientProviderBuilder;

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
        dbClient.execute(exec -> exec.update("create table if not exists instruction (documentName VARCHAR(25), " +
                        "line integer, hash integer, PRIMARY KEY(documentName, line))"))
                .exceptionally(t -> {
                    System.out.println(t.getMessage());
                    return null;
                }).await();
    }

    public void add(String name, int line, int hash) {
        Objects.requireNonNull(name);
        dbClient.execute(exec -> exec.createInsert("Insert into Instruction values (?, ?, ?)")
                .addParam(name)
                .addParam(line)
                .addParam(hash)
                .execute()
        ).exceptionally((t -> {
            System.out.println(t.getMessage());
            return null;
        })).await();
    }
}
