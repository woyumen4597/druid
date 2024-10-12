package com.alibaba.druid.bvt.sql.bigquery;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GotoTest {
    static File PREFIX_SQL_DIR = new File("/Users/jinrongchuan/Documents/work/goto/1000plussqls/");
    static File PREFIX_LOGS_DIR = new File("/Users/jinrongchuan/Documents/work/goto/logs/");

    @Test
    public void gotoTest() throws FileNotFoundException {
        File outputFile = new File(PREFIX_LOGS_DIR, "goto_druid_scan.log");
        gotoTest(PREFIX_SQL_DIR, outputFile);
    }


    @Test
    public void singleFileTest() throws IOException {
        String fileName = "dimension_customer_first_bank_transfer_dailyquery.sql";
        File outputFile = new File(PREFIX_LOGS_DIR, fileName.replace(".sql", "") + ".log");
        File file = findFile(fileName);
        if (file != null) {
            fileTest(file, outputFile);
        } else {
            System.err.println("Can not find file: " + fileName);
        }
    }

    public File findFile(String name) {
        List<File> files = new ArrayList<File>();
        listFiles(PREFIX_SQL_DIR, files);
        return files.stream().filter(f -> f.getName().equals(name)).findFirst().get();
    }


    public static void gotoTest(File dir, File outputFile) throws FileNotFoundException {
        List<File> files = new ArrayList<File>();
        PrintStream fileOut = new PrintStream(outputFile);
        System.setOut(fileOut);
        System.setErr(fileOut);
        listFiles(dir, files);
        Map<String, Integer> map = new HashMap<>();
        int errorCount = 0;

        for (File file : files) {
            System.err.println();
            System.err.println(file.getAbsolutePath());
            try {
                String sql = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
                SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, DbType.bigquery);
                SQLStatement stmt = parser.parseStatement();
            } catch (Throwable e) {
                errorCount++;
                e.printStackTrace();
                if (e.getMessage() != null) {
                    String message = e.getMessage().length() >= 42 ? e.getMessage().substring(0, 42) : e.getMessage();
                    map.put(message, map.getOrDefault(message, 0) + 1);
                    System.err.println("Goto parser file failed:" + message + " file:" + file.getName());
                }
            }
        }
        // for each map entry
        System.out.println("Failed parsed file: " + errorCount);
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
        }
    }

    public static void fileTest(File file, File outputFile) throws IOException {
        String sql = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        try {
            PrintStream fileOut = new PrintStream(outputFile);
            System.setOut(fileOut);
            System.setErr(fileOut);
            SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, DbType.bigquery);
            List<SQLStatement> stmt = parser.parseStatementList();
        } catch (Throwable e) {
            System.out.println("SQL file: " + file.getName());
            System.out.println("-------------------\n\t");
            System.out.println(sql);
            System.out.println("-------------------\n\t");
            System.out.println("File parse failed with stacktrace!\n\t");
            e.printStackTrace();
        }
    }

    public static void listFiles(File dir, List<File> files) {
        File[] subFiles = dir.listFiles();
        for (File file : subFiles) {
            if (file.isDirectory()) {
                listFiles(file, files);
            } else {
                files.add(file);
            }
        }
    }
}
