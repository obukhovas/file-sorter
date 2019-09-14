package ru.ao.app;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class FileSorterTest {

    private String fileName;
    private String expected;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"test-file-1.txt",
                        "a\n" +
                                "b\n" +
                                "c\n" +
                                "d\n" +
                                "e\n" +
                                "f\n" +
                                "j\n"},
                {"test-file-2.txt",
                        "a\n" +
                                "b\n" +
                                "c\n" +
                                "d\n" +
                                "e\n" +
                                "f\n" +
                                "j\n" +
                                "j\n"},
                {"test-file-3.txt", ""},
                {"test-file-4.txt",
                        "a\n" +
                                "a\n" +
                                "a\n" +
                                "a\n" +
                                "a\n" +
                                "a\n" +
                                "a\n"},
                {"test-file-5.txt",
                        "1\n" +
                                "2\n" +
                                "3\n" +
                                "a\n" +
                                "b\n" +
                                "c\n"},
                {"test-file-6.txt",
                        "\n" +
                                "\n" +
                                "a\n" +
                                "b\n" +
                                "c\n" +
                                "d\n" +
                                "e\n" +
                                "f\n"}
        });
    }

    public FileSorterTest(String fileName, String expected) {
        this.fileName = fileName;
        this.expected = expected;
    }

    @Test
    public void test() throws Exception {
        File file = new File(this.getClass().getClassLoader().getResource(fileName).toURI());
        ByteArrayOutputStream actual = new ByteArrayOutputStream();

        new FileSorter(file, 1, actual)
                .setMaxChunkSize(5)
                .sort();

        Assert.assertEquals(expected, new String(actual.toByteArray(), Charset.forName("UTF-8")));
    }

}