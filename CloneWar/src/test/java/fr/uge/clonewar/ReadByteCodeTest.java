package fr.uge.clonewar;

import fr.uge.clonewar.backend.FileStorage;
import fr.uge.clonewar.utils.JarUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;


public class ReadByteCodeTest {

  @Test
  public void testAnalyze() throws IOException {
    try (var storage = new FileStorage()) {
      var jarPath = storage.create("Test.jar");

      JarUtils.createJarFromProgram(storage, jarPath, "fr.uge.test.Test",
          """
          package fr.uge.test;
          
          public record Test(int a, int b) {}
          """);

      var readByteCode = new ReadByteCode(jarPath);
      readByteCode.analyze();

      System.out.println(readByteCode);
    }
  }
}
