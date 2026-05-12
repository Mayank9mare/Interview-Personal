package com.uber.terminal;

import java.util.Scanner;

public class Terminal {

    public static void main(String[] args) {
        FileSystem fs = new FileSystem();

        System.out.println("=== Demo ===");
        run(fs, "mkdir /home/user/docs");
        run(fs, "mkdir /home/user/downloads");
        run(fs, "mkdir /var/log/app");
        run(fs, "ls /");
        run(fs, "cd /home/user");
        run(fs, "pwd");
        run(fs, "ls");
        run(fs, "search user");
        run(fs, "cd /");
        run(fs, "search log.*");
        run(fs, "cd /nope");
        run(fs, "search [bad");

        System.out.println("\n=== Terminal (type 'exit' to quit) ===");
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print(fs.pwd() + " $ ");
            if (!scanner.hasNextLine()) break;
            String line = scanner.nextLine().trim();
            if (line.equals("exit")) break;
            if (line.isEmpty()) continue;
            String result = dispatch(fs, line);
            if (!result.isEmpty()) System.out.println(result);
        }
        scanner.close();
    }

    static String dispatch(FileSystem fs, String line) {
        String[] parts = line.split("\\s+", 2);
        String cmd = parts[0];
        String arg = parts.length > 1 ? parts[1] : null;
        switch (cmd) {
            case "mkdir":  return arg != null ? fs.mkdir(arg)  : "Usage: mkdir <path>";
            case "cd":     return arg != null ? fs.cd(arg)     : "Usage: cd <path>";
            case "ls":     return fs.ls(arg);
            case "pwd":    return fs.pwd();
            case "search": return arg != null ? fs.search(arg) : "Usage: search <regex>";
            default:       return "Unknown command: " + cmd;
        }
    }

    private static void run(FileSystem fs, String cmd) {
        System.out.println(fs.pwd() + " $ " + cmd);
        String result = dispatch(fs, cmd);
        if (!result.isEmpty()) System.out.println(result);
    }
}
