package edu.ucla.cs.stats;

import edu.ucla.cs.gadget.GadgetAnalyzer;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

public class GadgetChainStatsAnalysis {
    public static void main(String[] args) throws IOException {
        HashMap<String, Set<String>> gadgets = GadgetAnalyzer.loadGadgetChains();
        int sum = 0;
        for(String name : gadgets.keySet()) {
            sum += gadgets.get(name).size();
        }
        System.out.println(sum / gadgets.size());
    }
}
