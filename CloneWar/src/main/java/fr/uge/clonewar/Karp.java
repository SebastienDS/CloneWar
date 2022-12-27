package fr.uge.clonewar;

import fr.uge.clonewar.backend.database.Database;
import io.helidon.dbclient.jdbc.JdbcDbClientProviderBuilder;

import java.io.IOException;
import java.util.*;

public class Karp {

  public static Map.Entry<HashMap<Integer, Set<Integer>>, Integer> rabinKarp(List<Instruction> listTuple1, List<Instruction> listTuple2) {
    var countSameInstr = 0;
    var mapIndex = new HashMap<Integer, Set<Integer>>();
    for (var tuple1: listTuple1) {
      for(var tuple2: listTuple2){
        if(tuple1.hash() == tuple2.hash()){
          countSameInstr += 1;
          mapIndex.computeIfAbsent(tuple2.line(), integer -> new HashSet<>()).add(tuple1.line());
          break;
        }
      }
    }
    return Map.entry(mapIndex, countSameInstr);

  }

  //pour les tests
  public static void main(String[] args) throws IOException {
    var dbClient = JdbcDbClientProviderBuilder.create()
        .url("jdbc:sqlite:cloneWar.db")
        .build();
    var db = new Database(dbClient);
    db.instructionTable().flushBuffer();
    var listHashDoc1 = db.instructionTable().getLineAndHash("cc1.jar");
    var listHashDoc2 = db.instructionTable().getLineAndHash("cc.jar");
    var a = rabinKarp(listHashDoc1, listHashDoc2);
  }

  static double average(int succeed, int total){
    return succeed * 100. / total;
  }
}
