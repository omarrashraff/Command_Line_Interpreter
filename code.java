import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;
import java.util.zip.*;

// ==============================
// Parser Class
// Parses user input into a command name and arguments
// ==============================
class Parser {
    private String commandName;
    private String[] args;

    public boolean parse(String input) {
        if (input == null || input.trim().equals("")) return false;
        input = input.trim();
        String[] parts = splitBySpaces(input);
        if (parts.length == 0) return false;

        commandName = parts[0];
        args = new String[parts.length - 1];
        for (int i = 1; i < parts.length; i++) {
            args[i - 1] = parts[i];
        }
        return true;
    }

    private String[] splitBySpaces(String input) {
        int wordCount = 0;
        boolean inWord = false;

        for (char c : input.toCharArray()) {
            if (c != ' ' && !inWord) { wordCount++; inWord = true; }
            else if (c == ' ') { inWord = false; }
        }

        String[] words = new String[wordCount];
        int index = 0;
        StringBuilder current = new StringBuilder();

        for (char c : input.toCharArray()) {
            if (c != ' ') {
                current.append(c);
            } else {
                if (current.length() > 0) {
                    words[index++] = current.toString();
                    current = new StringBuilder();
                }
            }
        }
        if (current.length() > 0) words[index] = current.toString();
        return words;
    }

    public String getCommandName() { return commandName; }
    public String[] getArgs() { return args; }
}

// ==============================
// Terminal Class
// Simulates a Linux-style command-line interpreter
// ==============================
public class Terminal {
    Parser parser = new Parser();
    File currentDirectory = new File(System.getProperty("user.dir"));

    // pwd: print working directory
    public String pwd() {
        return currentDirectory.getAbsolutePath();
    }

    // cd: change directory
    public void cd(String[] args) {
        if (args.length == 0) {
            currentDirectory = new File(System.getProperty("user.home"));
        } else if (args.length == 1) {
            File newPath;
            if (args[0].equals("..")) {
                newPath = currentDirectory.getParentFile();
            } else {
                File inputPath = new File(args[0]);
                if (args[0].startsWith("/") || args[0].startsWith("\\")) {
                    newPath = inputPath;
                } else {
                    newPath = new File(currentDirectory, args[0]);
                }
            }
            if (newPath != null && newPath.exists() && newPath.isDirectory()) {
                currentDirectory = newPath;
            } else {
                System.out.println("Error: Invalid path");
            }
        } else {
            System.out.println("Error: cd takes zero or one argument only.");
        }
    }

    // ls: list directory contents alphabetically
    public void ls() {
        String[] items = currentDirectory.list();
        if (items != null) {
            sortAlphabetically(items);
            for (String item : items) System.out.println(item);
        } else {
            System.out.println("Error: Unable to list contents.");
        }
    }

    // mkdir: create one or more directories
    public void mkdir(String[] args) {
        if (args.length == 0) {
            System.out.println("Error: mkdir requires at least one argument.");
            return;
        }
        for (String dirName : args) {
            File newDir = dirName.startsWith("/") || dirName.startsWith("\\")
                    ? new File(dirName)
                    : new File(currentDirectory, dirName);
            if (newDir.exists()) {
                System.out.println("Error: Directory already exists - " + newDir.getAbsolutePath());
            } else if (newDir.mkdirs()) {
                System.out.println("Directory created: " + newDir.getAbsolutePath());
            } else {
                System.out.println("Error: Failed to create directory - " + newDir.getAbsolutePath());
            }
        }
    }

    // rmdir: remove empty directory or all empty dirs with *
    public void rmdir(String[] args) {
        if (args.length != 1) {
            System.out.println("Error: rmdir takes exactly one argument.");
            return;
        }
        String target = args[0];
        if (target.equals("*")) {
            File[] items = currentDirectory.listFiles();
            if (items == null || items.length == 0) {
                System.out.println("No directories found.");
                return;
            }
            boolean foundEmpty = false;
            for (File item : items) {
                if (item.isDirectory()) {
                    File[] contents = item.listFiles();
                    if (contents == null || contents.length == 0) {
                        if (item.delete()) {
                            System.out.println("Removed: " + item.getName());
                            foundEmpty = true;
                        }
                    }
                }
            }
            if (!foundEmpty) System.out.println("No empty directories found.");
        } else {
            File targetDir = target.startsWith("/") || target.startsWith("\\")
                    ? new File(target)
                    : new File(currentDirectory, target);
            if (!targetDir.exists()) {
                System.out.println("Error: Directory not found.");
                return;
            }
            if (!targetDir.isDirectory()) {
                System.out.println("Error: Not a directory.");
                return;
            }
            String[] contents = targetDir.list();
            if (contents != null && contents.length > 0) {
                System.out.println("Error: Directory is not empty.");
                return;
            }
            if (targetDir.delete()) {
                System.out.println("Directory removed: " + targetDir.getAbsolutePath());
            } else {
                System.out.println("Error: Failed to remove directory.");
            }
        }
    }

    // touch: create a new empty file
    public void touch(String[] args) {
        if (args.length != 1) {
            System.out.println("Error: touch requires exactly one argument.");
            return;
        }
        File newFile = args[0].startsWith("/") || args[0].startsWith("\\")
                ? new File(args[0])
                : new File(currentDirectory, args[0]);
        try {
            if (newFile.exists()) {
                System.out.println("File already exists: " + newFile.getAbsolutePath());
            } else if (newFile.createNewFile()) {
                System.out.println("File created: " + newFile.getAbsolutePath());
            } else {
                System.out.println("Error: Failed to create file.");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // cp: copy file or directory (-r for recursive)
    public void cp(String[] args) {
        if (args.length == 3 && args[0].equals("-r")) {
            copyDirectoryRecursive(new File(args[1]), new File(args[2]));
            return;
        }
        if (args.length != 2) {
            System.out.println("Error: cp takes exactly 2 arguments.");
            return;
        }
        String sourcePath = args[0].replace("\\", "/");
        String destPath   = args[1].replace("\\", "/");

        File sourceFile = sourcePath.startsWith("/") || sourcePath.contains(":")
                ? new File(sourcePath) : new File(currentDirectory, sourcePath);
        File destFile   = destPath.startsWith("/") || destPath.contains(":")
                ? new File(destPath)   : new File(currentDirectory, destPath);

        if (!sourceFile.exists() || !sourceFile.isFile()) {
            System.out.println("Error: Source file not found.");
            return;
        }
        try (FileInputStream fis = new FileInputStream(sourceFile);
             FileOutputStream fos = new FileOutputStream(destFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) fos.write(buffer, 0, bytesRead);
            System.out.println("Copied: " + sourceFile.getName() + " → " + destFile.getName());
        } catch (Exception e) {
            System.out.println("Error copying file: " + e.getMessage());
        }
    }

    private static void copyDirectoryRecursive(File sourceDir, File destDir) {
        if (!sourceDir.exists()) { System.out.println("Error: Source not found."); return; }
        File newDir = new File(destDir, sourceDir.getName());
        if (!newDir.exists()) newDir.mkdirs();
        File[] files = sourceDir.listFiles();
        if (files != null) {
            for (File file : files) {
                File newFile = new File(newDir, file.getName());
                if (file.isDirectory()) {
                    copyDirectoryRecursive(file, newDir);
                } else {
                    try {
                        Files.copy(file.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        System.out.println("Error copying: " + file.getName());
                    }
                }
            }
        }
        System.out.println("Directory copied successfully.");
    }

    // rm: remove a file
    public void rm(String[] args) {
        if (args.length != 1) { System.out.println("Error: rm takes exactly one argument."); return; }
        String path = args[0].replace("\\", "/");
        File targetFile = path.startsWith("/") || path.contains(":")
                ? new File(path) : new File(currentDirectory, path);
        if (!targetFile.exists()) { System.out.println("Error: File not found."); return; }
        if (targetFile.isDirectory()) { System.out.println("Error: Is a directory, not a file."); return; }
        if (targetFile.delete()) {
            System.out.println("File deleted: " + targetFile.getName());
        } else {
            System.out.println("Error: Could not delete file.");
        }
    }

    // cat: display file contents
    public void cat(String[] args) {
        if (args.length != 1) { System.out.println("Error: cat takes exactly one argument."); return; }
        File targetFile = args[0].startsWith("/") || args[0].startsWith("\\")
                ? new File(args[0]) : new File(currentDirectory, args[0]);
        if (!targetFile.exists()) { System.out.println("Error: File not found."); return; }
        if (targetFile.isDirectory()) { System.out.println("Error: Is a directory."); return; }
        try (Scanner fileReader = new Scanner(targetFile)) {
            while (fileReader.hasNextLine()) System.out.println(fileReader.nextLine());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // wc: count lines, words, characters
    public void wc(String[] args) {
        if (args.length != 1) { System.out.println("Error: wc takes exactly one argument."); return; }
        File targetFile = args[0].startsWith("/") || args[0].startsWith("\\")
                ? new File(args[0]) : new File(currentDirectory, args[0]);
        if (!targetFile.exists()) { System.out.println("Error: File not found."); return; }
        if (targetFile.isDirectory()) { System.out.println("Error: Is a directory."); return; }
        int lineCount = 0, wordCount = 0, charCount = 0;
        try (Scanner fileReader = new Scanner(targetFile)) {
            while (fileReader.hasNextLine()) {
                String line = fileReader.nextLine();
                lineCount++;
                charCount += line.length() + 1;
                if (!line.trim().isEmpty()) wordCount += line.trim().split("\\s+").length;
            }
            System.out.println(lineCount + " " + wordCount + " " + charCount + " " + targetFile.getName());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // echo: print text
    public void echo(String[] args) {
        if (args.length == 0) { System.out.println(); return; }
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            output.append(args[i]);
            if (i < args.length - 1) output.append(" ");
        }
        System.out.println(output.toString());
    }

    // zip: compress files or directories
    public void zip(String[] args) {
        if (args.length < 2) { System.out.println("Usage: zip [-r] archive.zip files..."); return; }
        boolean recursive = args[0].equals("-r");
        int startIndex = recursive ? 2 : 1;
        if (recursive && args.length < 3) { System.out.println("Usage: zip -r archive.zip directory"); return; }
        String zipFileName = args[startIndex - 1];
        if (!zipFileName.endsWith(".zip")) { System.out.println("Archive name must end with .zip"); return; }
        try (FileOutputStream fos = new FileOutputStream(zipFileName);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            for (int i = startIndex; i < args.length; i++) {
                File fileToZip = new File(currentDirectory, args[i]);
                if (!fileToZip.exists()) { System.out.println("Not found: " + args[i]); continue; }
                if (fileToZip.isDirectory() && recursive) zipDirectory(fileToZip, fileToZip.getName(), zos);
                else if (fileToZip.isFile()) zipFile(fileToZip, fileToZip.getName(), zos);
            }
            System.out.println("Created: " + zipFileName);
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void zipFile(File file, String fileName, ZipOutputStream zos) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            zos.putNextEntry(new ZipEntry(fileName));
            byte[] bytes = new byte[1024]; int length;
            while ((length = fis.read(bytes)) >= 0) zos.write(bytes, 0, length);
        }
    }

    private void zipDirectory(File folder, String parentFolder, ZipOutputStream zos) throws IOException {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) zipDirectory(file, parentFolder + "/" + file.getName(), zos);
            else {
                try (FileInputStream fis = new FileInputStream(file)) {
                    zos.putNextEntry(new ZipEntry(parentFolder + "/" + file.getName()));
                    byte[] bytes = new byte[1024]; int length;
                    while ((length = fis.read(bytes)) >= 0) zos.write(bytes, 0, length);
                }
            }
        }
    }

    // unzip: extract a zip archive
    private static void unzip(String[] args) {
        if (args.length == 0) { System.out.println("Error: unzip requires a zip file argument."); return; }
        String zipFilePath = args[0];
        String destDirPath = args.length >= 2 ? args[1] : ".";
        File destDir = new File(destDirPath);
        if (!destDir.exists()) destDir.mkdirs();
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                File newFile = new File(destDir, entry.getName());
                if (entry.isDirectory()) {
                    newFile.mkdirs();
                } else {
                    new File(newFile.getParent()).mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        byte[] buffer = new byte[1024]; int len;
                        while ((len = zis.read(buffer)) > 0) fos.write(buffer, 0, len);
                    }
                }
                zis.closeEntry();
            }
            System.out.println("Extracted: " + zipFilePath + " → " + destDir.getAbsolutePath());
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // Alphabetical bubble sort
    private void sortAlphabetically(String[] arr) {
        for (int i = 0; i < arr.length - 1; i++)
            for (int j = 0; j < arr.length - i - 1; j++)
                if (arr[j].compareToIgnoreCase(arr[j + 1]) > 0) {
                    String temp = arr[j]; arr[j] = arr[j + 1]; arr[j + 1] = temp;
                }
    }

    // Route input to the correct command method
    public void chooseCommandAction(String input) {
        if (!parser.parse(input)) { System.out.println("Invalid input."); return; }

        boolean append = false;
        String[] parts;
        if (input.contains(">>")) { parts = input.split(">>", 2); append = true; }
        else if (input.contains(">")) { parts = input.split(">", 2); }
        else { parts = new String[]{input}; }

        String commandPart = parts[0].trim();
        String outputFile  = parts.length > 1 ? parts[1].trim() : null;

        if (!parser.parse(commandPart)) { System.out.println("Invalid command syntax."); return; }

        String   command = parser.getCommandName();
        String[] args    = parser.getArgs();

        java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
        java.io.PrintStream oldOut = System.out;
        System.setOut(new java.io.PrintStream(outputStream));

        try {
            switch (command) {
                case "pwd":    System.out.println(pwd()); break;
                case "cd":     cd(args);     break;
                case "ls":     ls();         break;
                case "mkdir":  mkdir(args);  break;
                case "rmdir":  rmdir(args);  break;
                case "touch":  touch(args);  break;
                case "cp":     cp(args);     break;
                case "rm":     rm(args);     break;
                case "cat":    cat(args);    break;
                case "wc":     wc(args);     break;
                case "echo":   echo(args);   break;
                case "zip":    zip(args);    break;
                case "unzip":  unzip(args);  break;
                case "exit":
                    System.out.println("Exiting terminal...");
                    System.exit(0);
                default:
                    System.out.println("Unknown command: " + command);
            }
        } finally {
            System.out.flush();
            System.setOut(oldOut);
        }

        if (outputFile != null) {
            try {
                File file = new File(currentDirectory, outputFile);
                try (java.io.FileWriter writer = new java.io.FileWriter(file, append)) {
                    writer.write(outputStream.toString());
                }
                System.out.println("Output redirected to: " + file.getAbsolutePath());
            } catch (Exception e) {
                System.out.println("Error writing to file: " + e.getMessage());
            }
        } else {
            System.out.print(outputStream.toString());
        }
    }

    // Entry point
    public static void main(String[] args) {
        Terminal terminal = new Terminal();
        Scanner scanner   = new Scanner(System.in);
        while (true) {
            System.out.print(">>> ");
            String input = scanner.nextLine();
            terminal.chooseCommandAction(input);
        }
    }
}
