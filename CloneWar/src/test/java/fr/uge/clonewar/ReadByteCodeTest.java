package fr.uge.clonewar;

import fr.uge.clonewar.backend.FileStorage;
import fr.uge.clonewar.backend.database.Database;
import fr.uge.clonewar.utils.JarBuilder;
import io.helidon.config.Config;
import io.helidon.dbclient.DbClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.stream.IntStream;


public class ReadByteCodeTest {

  private static final Database db = new Database(DbClient.create(Config.create().get("test.db")));

  @Test
  public void testSameArtefact() throws IOException {
    try (var storage = new FileStorage()) {
      var jar = new JarBuilder(storage.storageDir(), "Test");
      jar.addFile("fr.uge.test.Test",
          """
          package fr.uge.test;

          import java.util.Objects;

          public class Test {
            public static int factorial(int n) {
              var fact = 1;
              for (var i = 1; i <= n; i++) {
                fact = fact * i;
              }
              return fact;
            }
            
            public static void main(String[] args) {
              System.out.println(factorial(10));
            }
          }
          """);
      var artefact = jar.get();

      var indexedArtefact = CloneDetectors.indexArtefact(db, artefact);
      var l1 = db.instructionTable().getAll(indexedArtefact.id());
      Assertions.assertEquals(100., Karp.average(Karp.rabinKarp(l1, l1).getValue(), l1.size()));
    }
  }

  @Test
  public void testSameCodeBasicRefactor() throws IOException {
    try (var storage = new FileStorage()) {
      var jar = new JarBuilder(storage.storageDir(), "Test");
      jar.addFile("fr.uge.test.Test",
          """
          package fr.uge.test;

          public class Test {
            public static int factorial(int n) {
              var fact = 1;
              for (var i = 1; i <= n; i++) {
                fact = fact * i;
              }
              return fact;
            }
            
            public static void main(String[] args) {
              System.out.println(factorial(10));
            }
          }
          """);
      var artefact = jar.get();

      var jar2 = new JarBuilder(storage.storageDir(), "Test2");
      jar2.addFile("fr.uge.test.Test2",
          """
          package fr.uge.test;

          public class Test2 {
            public static int factorial(int n) {
              int res = 1;
              for (int count = 1; count <= n; count++) {
                res *= count;
              }
              return res;
            }
            
            public static void main(String[] args) {
              System.out.println(factorial(10));
            }
          }
          """);
      var artefact2 = jar2.get();

      var indexedArtefact = CloneDetectors.indexArtefact(db, artefact);
      var indexedArtefact2 = CloneDetectors.indexArtefact(db, artefact2);
      var l1 = db.instructionTable().getAll(indexedArtefact.id());
      var l2 = db.instructionTable().getAll(indexedArtefact2.id());
      Assertions.assertEquals(100., Karp.average(Karp.rabinKarp(l1, l2).getValue(), l1.size()));
    }
  }

  @Test
  public void testSameCodeStreamRefactor() throws IOException {
    try (var storage = new FileStorage()) {
      var jar = new JarBuilder(storage.storageDir(), "Test");
      jar.addFile("fr.uge.test.Test",
          """
          package fr.uge.test;

          public class Test {
            public static int factorial(int n) {
              var fact = 1;
              for (var i = 1; i <= n; i++) {
                fact = fact * i;
              }
              return fact;
            }
            
            public static void main(String[] args) {
              System.out.println(factorial(10));
            }
          }
          """);
      var artefact = jar.get();

      var jar2 = new JarBuilder(storage.storageDir(), "Test2");
      jar2.addFile("fr.uge.test.Test2",
          """
              package fr.uge.test;

              import java.util.stream.IntStream;

              public class Test2 {
                public static int factorial(int n) {
                  return IntStream.rangeClosed(1, n).reduce(1, (a, b) -> a * b);
                }
                
                public static void main(String[] args) {
                  System.out.println(factorial(10));
                }
              }
              """);
      var artefact2 = jar2.get();

      var indexedArtefact = CloneDetectors.indexArtefact(db, artefact);
      var indexedArtefact2 = CloneDetectors.indexArtefact(db, artefact2);
      var l1 = db.instructionTable().getAll(indexedArtefact.id());
      var l2 = db.instructionTable().getAll(indexedArtefact2.id());

      Assertions.assertTrue(Karp.average(Karp.rabinKarp(l1, l2).getValue(), l1.size()) < 15);
    }
  }
}
