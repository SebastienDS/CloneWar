package fr.uge.clonewar;

import fr.uge.clonewar.backend.FileStorage;
import fr.uge.clonewar.utils.JarUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;

import static fr.uge.clonewar.ReadByteCode.consumeInstructions;


public class ReadByteCodeTest {

  @Test
  public void testAnalyze() throws IOException {
    try (var storage = new FileStorage()) {
      var jarPath = storage.create("Test.jar");

      JarUtils.createJarFromProgram(storage, jarPath, "fr.uge.test.Test",
          """
          package fr.uge.test;
          
          import java.util.Objects;
          
          public class Test{
          
              private final int a;
              private final int b;
              
              public Test(int a, int b){
                this.a = a;
                this.b = b;
              }
              
              private void cc(){
                System.out.println(a + b);
              }
          }
          """);

      var jarPath2 = storage.create("Test2.jar");

      JarUtils.createJarFromProgram(storage, jarPath2, "fr.uge.test.Test2",
              """
              package fr.uge.test;
              
              public record Test2(int a, int b) {
                  private void cc(){
                    System.out.println(a + b);
                  }
              }
              """);

      var readByteCode = new ReadByteCode(jarPath);
      readByteCode.analyze();
      var readByteCode2 = new ReadByteCode(jarPath2);
      readByteCode2.analyze();
      var l1 = new ArrayList<Instruction>();
      var l2 = new ArrayList<Instruction>();
      System.out.println(readByteCode);
      readByteCode.forEach((f, iterator) -> {
        consumeInstructions(iterator, l1::add);
      });
      readByteCode2.forEach((f, iterator) -> {
        consumeInstructions(iterator, l2::add);
      });
      System.out.println(l1);
      System.out.println(l2);
      var result = Karp.rabinKarp(l1, l2);
      var succeed = result.getValue();
      Assertions.assertTrue(Karp.average(succeed, l1.size()) >= 84);
      Assertions.assertEquals(100., Karp.average(Karp.rabinKarp(l1, l1).getValue(), l1.size()));
      System.out.println(succeed);
      System.out.println(l1.size());
      System.out.println(Karp.average(succeed, l1.size()));
    }
  }
}
