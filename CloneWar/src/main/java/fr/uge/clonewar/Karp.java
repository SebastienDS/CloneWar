package fr.uge.clonewar;

import fr.uge.clonewar.backend.database.Database;
import fr.uge.clonewar.backend.database.InstructionTable.InstructionRow;
import io.helidon.config.Config;
import io.helidon.dbclient.DbClient;

import java.io.IOException;
import java.util.*;

public class Karp {

  /**
   * Perform the Rabin Karp algorithm.
   * @param other Instructions
   * @param reference The reference
   * @return A tuple of matched lines and number of same instructions
   */
  public static Map.Entry<HashMap<InstructionRow, Set<InstructionRow>>, Integer> rabinKarp(List<InstructionRow> other, List<InstructionRow> reference) {
    var countSameInstr = 0;
    var mapIndex = new HashMap<InstructionRow, Set<InstructionRow>>();
    for (var tuple1: other) {
      for (var tuple2: reference) {
        if (tuple1.instruction().hash() == tuple2.instruction().hash()) {
          countSameInstr += 1;
          mapIndex.computeIfAbsent(tuple2, integer -> new HashSet<>()).add(tuple1);
          break;
        }
      }
    }
    return Map.entry(mapIndex, countSameInstr);
  }

  /**
   * Gets the average between succeed and total values.
   * @param succeed succeed number
   * @param total total number
   * @return The average
   */
  public static double average(int succeed, int total){
    return succeed * 100. / total;
  }
}
