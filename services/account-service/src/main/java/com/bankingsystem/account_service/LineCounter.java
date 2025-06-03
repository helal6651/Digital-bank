package com.bankingsystem.account_service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class LineCounter {
    public static void main(String[] args) {

        String projectPath = "C:\\Users\\BJIT\\Desktop\\CrossSkillProject\\FromBranch\\Digital-bank\\services\\account-service"; // Replace with your project path
        String[] extensions = {".java", ".js", ".sql", ".xml", ".properties"}; // Add extensions as needed
        int totalLines = countLinesOfCode(new File(projectPath), extensions);
        System.out.println("Total lines of code: " + totalLines);
    }

    public static int countLinesOfCode(File directory, String[] extensions) {
        int lines = 0;
        if (directory.isDirectory()) {
            for (File file : directory.listFiles()) {
                if (file.isDirectory()) {
                    lines += countLinesOfCode(file, extensions);
                } else if (matchesExtension(file.getName(), extensions)) {
                    lines += countLinesInFile(file);
                }
            }
        }
        return lines;
    }

    private static boolean matchesExtension(String fileName, String[] extensions) {
        for (String ext : extensions) {
            if (fileName.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    private static int countLinesInFile(File file) {
        int lines = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            while (reader.readLine() != null) {
                lines++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }
}

