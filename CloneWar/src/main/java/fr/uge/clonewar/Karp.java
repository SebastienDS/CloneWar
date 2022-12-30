package fr.uge.clonewar;

import fr.uge.clonewar.backend.database.Database;
import fr.uge.clonewar.backend.database.InstructionTable.InstructionRow;
import io.helidon.config.Config;
import io.helidon.dbclient.DbClient;

import java.io.IOException;
import java.util.*;

public class Karp {

  public static Map.Entry<HashMap<InstructionRow, Set<InstructionRow>>, Integer> rabinKarp(List<InstructionRow> listTuple1, List<InstructionRow> listTuple2) {
    var countSameInstr = 0;
    var mapIndex = new HashMap<InstructionRow, Set<InstructionRow>>();
    for (var tuple1: listTuple1) {
      for (var tuple2: listTuple2) {
        if (tuple1.instruction().hash() == tuple2.instruction().hash()) {
          countSameInstr += 1;
          mapIndex.computeIfAbsent(tuple2, integer -> new HashSet<>()).add(tuple1);
          break;
        }
      }
    }
    return Map.entry(mapIndex, countSameInstr);
  }

  //pour les tests
  public static void main(String[] args) throws IOException {
    var dbClient = DbClient.create(Config.create().get("test.db"));
    var db = new Database(dbClient);
    db.instructionTable().flushBuffer();
    var listHashDoc1 = db.instructionTable().getAll(1);
    var listHashDoc2 = db.instructionTable().getAll(2);
    var a = rabinKarp(listHashDoc1, listHashDoc2);
  }

  public static double average(int succeed, int total){
    return succeed * 100. / total;
  }
}
