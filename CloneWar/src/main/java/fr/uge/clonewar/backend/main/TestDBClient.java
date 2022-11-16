package fr.uge.clonewar.backend.main;

import io.helidon.dbclient.jdbc.JdbcDbClientProviderBuilder;


public class TestDBClient {
  private record Tuple(int id, String name) {}

  public static void main(String[] args) {
    var dbClient = JdbcDbClientProviderBuilder.create()
        .url("jdbc:sqlite:test.db")
        .build();

    dbClient.execute(exec -> exec.update("create table if not exists person (id integer PRIMARY KEY, name VARCHAR(25))"))
            .exceptionally(t -> {
              System.out.println(t);
              return null;
            }).await();
    dbClient.execute(exec -> exec.insert("insert into person values (3, 'zeofughzef')"))
            .exceptionally(t -> {
              System.out.println(t);
              return null;
            }).await();
    dbClient.execute(exec -> exec.insert("insert into person values (4, 'zeef')"))
            .exceptionally(t -> {
              System.out.println(t);
              return null;
            }).await();

    var res = dbClient.execute(exec -> exec.query("select * from person"));
    res.forEach(System.out::println);

    var list = res.map(row -> new Tuple(
        row.column("id").as(Integer.class),
        row.column("name").as(String.class)
    )).collectList().await();

    System.out.println(list);
  }
}
