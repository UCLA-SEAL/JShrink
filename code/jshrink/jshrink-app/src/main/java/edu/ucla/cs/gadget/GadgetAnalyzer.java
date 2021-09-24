package edu.ucla.cs.gadget;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

public class GadgetAnalyzer {
    public static void main(String[] args) throws IOException {
        HashMap<String, Set<String>> gadgets = loadGadgetChains();
        String log_dir_path = "/home/troy/Downloads/field_removal_with_tamiflex_output_log";
        File log_dir = new File(log_dir_path);
        for(File proj : log_dir.listFiles()) {
            File log_file = new File(proj.getAbsolutePath() + File.separator + "verbose_log.dat");
            List<String> lines = FileUtils.readLines(log_file, Charset.defaultCharset());

            HashSet<String> usedClasses = new HashSet<String>();
            HashSet<String> unusedClasses = new HashSet<String>();
            for(String line : lines) {
                String[] ss = line.split(",");
                if (ss[0].equals("LIB_CLASS_USED")) {
                    usedClasses.add(ss[1]);
                } else if (ss[0].equals("LIB_CLASS_UNUSED")) {
                    unusedClasses.add(ss[1]);
                }
            }

            HashMap<String, Set<String>> contained_gadget_chains = new HashMap<String, Set<String>>();
            HashMap<String, Set<String>> remaining_gadget_chains = new HashMap<String, Set<String>>();
            for(String gadget_chain_name : gadgets.keySet()) {
                Set<String> gadget_chain = gadgets.get(gadget_chain_name);
                boolean hasGadgetChain = true;
                boolean hasGadgetChainAfterDebloating = true;
                for(String gadget : gadget_chain) {
                    if(gadget.startsWith("com.sun") || gadget.startsWith("sun") || gadget.startsWith("java")) {
                        // skip the JDK classes
                        continue;
                    }

                    if(!usedClasses.contains(gadget) && !unusedClasses.contains(gadget)) {
                        hasGadgetChain = false;
                    }

                    if(!usedClasses.contains(gadget)) {
                        hasGadgetChainAfterDebloating = false;
                    }
                }

                if(hasGadgetChain) {
                    contained_gadget_chains.put(gadget_chain_name, gadget_chain);
                }

                if(hasGadgetChainAfterDebloating) {
                    remaining_gadget_chains.put(gadget_chain_name, gadget_chain);
                }
            }

            System.out.println("BEFORE_DEBLOATING, " + proj.getName() + ", " + contained_gadget_chains.size() + ", " + contained_gadget_chains.keySet());
//            for(String gadget_chain_name : contained_gadget_chains.keySet()) {
//                System.out.println("Gadget chains in " + proj.getName());
//                System.out.println("\t" + gadget_chain_name + "\t" + contained_gadget_chains.get(gadget_chain_name));
//            }
            System.out.println("AFTER_DEBLOATING, " + proj.getName() + ", " + remaining_gadget_chains.size() + ", " + remaining_gadget_chains.keySet());
        }
    }

    public static HashMap<String, Set<String>> loadGadgetChains() throws IOException {
        HashMap<String, Set<String>> allGadgetChains = new HashMap<String, Set<String>>();
        File dir = new File(GadgetAnalyzer.class.getClassLoader().getResource("gadget-chain").getFile());
        for(File f : dir.listFiles()) {
            if(f.getName().endsWith(".txt")) {
                HashSet<String> gadgets = new HashSet<String>();
                List<String> lines = FileUtils.readLines(f, Charset.defaultCharset());
                for(String line : lines) {
                    line = line.trim();
                    if(line.isEmpty()) {
                        // skip the JDK classes
                        continue;
                    }
                    gadgets.add(line.trim());
                }
                allGadgetChains.put(f.getName().substring(0, f.getName().indexOf('.')), gadgets);
            }
        }

        return allGadgetChains;
    }
}
