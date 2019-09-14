package ru.ao.app;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            switch (args[0]) {
                case "-g":
                    if (args.length == 4) {
                        String fileName = args[1];
                        int rowCount = Integer.parseInt(args[2]);
                        int makRowLength = Integer.parseInt(args[3]);

                        new FileGenerator(fileName, rowCount, makRowLength).generate();
                    } else {
                        System.out.println("Incorrect arguments count: " + args.length);
                    }
                    break;
                case "-s":
                    if (args.length == 3) {
                        String fileName = args[1];
                        int makRowLength = Integer.parseInt(args[2]);
                        File input = new File(fileName);
                        File output = new File("SORTED_" + fileName);
                        new FileSorter(input, makRowLength, new FileOutputStream(output)).sort();
                    } else {
                        System.out.println("Incorrect arguments count: " + args.length);
                    }
                    break;
                default:
                    System.out.println("Incorrect operation: " + args[0]);
            }
        } else {
            System.out.println("Incorrect3 arguments count: " + args.length);
        }
    }

}
