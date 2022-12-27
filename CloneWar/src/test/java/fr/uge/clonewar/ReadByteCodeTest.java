package fr.uge.clonewar;

import fr.uge.clonewar.backend.FileStorage;
import fr.uge.clonewar.utils.JarBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;


public class ReadByteCodeTest {

  @Test
  public void testAnalyze() throws IOException {
    try (var storage = new FileStorage()) {
      var jar = new JarBuilder(storage.storageDir(), "Test");
      jar.addFile("fr.uge.test.Test",
          """
          package fr.uge.test;
          
          import java.util.Objects;
          
          public class Test {
          
            private final int a;
            private final int b;
            
            public Test(int a, int b) {
              this.a = a;
              this.b = b;
            }
            
            private void cc() {
              var c = a + b;
              System.out.println(a + b);
              System.out.println(a + b);
              System.out.println(a + b);
            }
          }
          """);

      var jar2 = new JarBuilder(storage.storageDir(), "Test2");
      jar2.addFile("fr.uge.test.Test2",
          """
          package fr.uge.test;
              
          public record Test2(int a, int b) {
            private void cc() {
              System.out.println(a + b);
            }
          }
          """);

      var artefact = jar.get();
      var artefact2 = jar2.get();

      var readByteCode = new ReadByteCode(artefact.main());
      readByteCode.analyze();
      var readByteCode2 = new ReadByteCode(artefact2.main());
      readByteCode2.analyze();

      System.out.println(readByteCode);
      System.out.println(readByteCode2);
      var l1 = new ArrayList<Instruction>();
      var l2 = new ArrayList<Instruction>();
      readByteCode.forEach((f, instruction) -> l1.add(instruction));
      readByteCode2.forEach((f, instruction) -> l2.add(instruction));

      var result = Karp.rabinKarp(l1, l2);
      var succeed = result.getValue();
      Assertions.assertEquals(100., Karp.average(Karp.rabinKarp(l1, l1).getValue(), l1.size()));
      System.out.println(succeed);
      System.out.println(l1.size());
      System.out.println(Karp.average(succeed, l1.size()));
    }
  }
}
