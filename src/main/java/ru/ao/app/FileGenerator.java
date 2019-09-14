package ru.ao.app;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

class FileGenerator {

    private String fileName;
    private int rowCount;
    private int makRowLength;

    FileGenerator(String fileName, int rowCount, int makRowLength) {
        this.fileName = fileName;
        this.rowCount = rowCount;
        this.makRowLength = makRowLength;
    }

    void generate() throws IOException {
        Path path = new File(".", fileName).toPath();
        try (BufferedWriter bw = Files.newBufferedWriter(path, Charset.forName("UTF-8"))) {
            for (int i = 0; i < rowCount; ++i) {
                bw.write(getRandomString() + "\n");
            }
        }
    }

    private String getRandomString() {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        Random random = new Random();
        int length = random.nextInt(makRowLength - 1) + 1;
        StringBuilder buffer = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomLimitedInt = leftLimit + (int)
                    (random.nextFloat() * (rightLimit - leftLimit + 1));
            buffer.append((char) randomLimitedInt);
        }
        return buffer.toString();
    }
}
