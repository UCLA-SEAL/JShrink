package edu.ucla.cs.stats;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

public class LogAnalysis {
    public static void main(String[] args) throws IOException {
        String log_dir_path = "/home/troy/Downloads/field_removal_with_tamiflex_output_log";
        File log_dir = new File(log_dir_path);
        for(File proj : log_dir.listFiles()) {
            File log_file = new File(proj.getAbsolutePath() + File.separator + "log.dat");
            List<String> lines = FileUtils.readLines(log_file, Charset.defaultCharset());
            int app_size = 0;
            int lib_size = 0;
            int app_method_num = 0;
            int lib_method_num = 0;
            int app_field_num = 0;
            int lib_field_num = 0;
            for(String line : lines) {
                String[] ss = line.split(",");
                if(ss[0].equals("app_size_before")) {
                    app_size = Integer.parseInt(ss[1]);
                } else if (ss[0].equals("libs_size_before")) {
                    lib_size = Integer.parseInt(ss[1]);
                } else if (ss[0].equals("app_num_methods_before")) {
                    app_method_num = Integer.parseInt(ss[1]);
                } else if (ss[0].equals("libs_num_methods_before")) {
                    lib_method_num = Integer.parseInt(ss[1]);
                } else if (ss[0].equals("app_num_fields_before")) {
                    app_field_num = Integer.parseInt(ss[1]);
                } else if (ss[0].equals("lib_num_fields_before")) {
                    lib_field_num = Integer.parseInt(ss[1]);
                }
            }

            System.out.println(proj.getName() + "," + app_method_num + "," + lib_method_num + "," + app_field_num
                            + "," + lib_field_num + "," + app_size + "," + lib_size + "," + (app_method_num + lib_method_num)
                            + "," + (app_field_num + lib_field_num) + "," + (app_size + lib_size));
        }
    }
}
