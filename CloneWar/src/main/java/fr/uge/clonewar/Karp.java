package fr.uge.clonewar;

import fr.uge.clonewar.backend.database.InstructionTable;
import io.helidon.dbclient.jdbc.JdbcDbClientProviderBuilder;

import java.util.*;

public class Karp {

    public static HashMap<Integer, Set<Integer>> rabinKarp(List<InstructionTable.Tuple> listTuple1, List<InstructionTable.Tuple> listTuple2){
        var mapIndex = new HashMap<Integer, Set<Integer>>();
        for (var tuple2: listTuple2) {
            for(var tuple1: listTuple1){
                if(tuple1.hash() == tuple2.hash()){
                    mapIndex.computeIfAbsent(tuple2.line(), integer -> new HashSet<>()).add(tuple1.line());
                }
            }
        }
        return mapIndex;

    }

    //pour les tests
    public static void main(String[] args) {
        var dbClient = JdbcDbClientProviderBuilder.create()
            .url("jdbc:sqlite:cloneWar.db")
            .build();
        var instructionDB = new InstructionTable(dbClient);
        var listHashDoc1 = instructionDB.getLineAndHash("cc");
        var listHashDoc2 = instructionDB.getLineAndHash("cc2");

        System.out.println(rabinKarp(listHashDoc1, listHashDoc2));
    }
}
