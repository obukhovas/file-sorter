package ru.ao.app;

import java.io.*;
import java.util.*;

import static java.util.Objects.isNull;

class FileSorter {

    private InputStream inputStream;
    private OutputStream outputStream;
    private Comparator<String> lineComparator;

    private long maxChunkSize;
    private List<File> tmpFiles = new ArrayList<>();

    FileSorter(File file, int maxLineLength,
               OutputStream outputStream) throws FileNotFoundException {
        this(new FileInputStream(file), file.length(), maxLineLength, outputStream, null);
    }

    FileSorter(File file, int maxLineLength, OutputStream outputStream,
               Comparator<String> lineComparator) throws FileNotFoundException {
        this(new FileInputStream(file), file.length(), maxLineLength, outputStream, null);
    }

    private FileSorter(InputStream inputStream, long fileSize,
                       int maxLineLength, OutputStream outputStream,
                       Comparator<String> lineComparator) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.lineComparator = lineComparator;
        computeMaxChunkSize(maxLineLength, fileSize);
    }

    private void computeMaxChunkSize(long maxLineLength, long fileSize) {
        long availableMemory = getAvailableMemory();
        long chunkSize = availableMemory / maxLineLength;
        setMaxChunkSize(chunkSize);
    }

    private long getAvailableMemory() {
        System.gc();
        Runtime r = Runtime.getRuntime();
        long allocatedMemory = r.totalMemory() - r.freeMemory();
        return r.maxMemory() - allocatedMemory;
    }

    FileSorter setMaxChunkSize(long maxChunkSize) {
        this.maxChunkSize = maxChunkSize;
        return this;
    }

    void sort() {
        sortAndSaveChunks();
        mergeChunksToOutputFile();
    }

    private void sortAndSaveChunks() {
        tmpFiles.clear();
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            long currentChunkSize = 0;
            while ((line = br.readLine()) != null) {
                lines.add(line);
                currentChunkSize += line.length() + 1;
                if (currentChunkSize >= maxChunkSize) {
                    currentChunkSize = 0;
                    sortAndWriteToTmpFile(lines);
                }
            }
            // last chunk
            sortAndWriteToTmpFile(lines);
        } catch (IOException e) {
            throw new RuntimeException("Error while sorting chunks", e);
        }
    }

    private void sortAndWriteToTmpFile(List<String> lines) throws IOException {
        lines.sort(lineComparator);
        File tmp = new File(System.currentTimeMillis() + ".tmp");
        tmpFiles.add(tmp);
        writeToFile(tmp, lines);
        lines.clear();
    }

    private void writeToFile(File file, List<String> lines) throws IOException {
        try (BufferedWriterWrapper bw = new BufferedWriterWrapper(new OutputStreamWriter(new FileOutputStream(file)))) {
            for (String line : lines) {
                bw.writeLine(line);
            }
        }
    }

    private void mergeChunksToOutputFile() {
        Map<StringWrapper, BufferedReader> lineReaderMap = new HashMap<>();
        List<BufferedReader> readers = new LinkedList<>();
        try (BufferedWriterWrapper bw = new BufferedWriterWrapper(new OutputStreamWriter(outputStream))) {
            /* collect readers */
            for (File chunk : tmpFiles) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(chunk)));
                readers.add(reader);
                String line = reader.readLine();
                if (!isNull(line)) {
                    lineReaderMap.put(new StringWrapper(line), reader);
                }
            }
            /* write to output file */
            List<StringWrapper> sorted = new LinkedList<>(lineReaderMap.keySet());
            while (!lineReaderMap.isEmpty()) {
                sorted.sort(lineComparator == null ? null : (o1, o2) -> lineComparator.compare(o1.string, o2.string));
                StringWrapper line = sorted.remove(0);
                bw.writeLine(line.string);
                BufferedReader reader = lineReaderMap.remove(line);
                String nextLine = reader.readLine();
                if (nextLine != null) {
                    StringWrapper sw = new StringWrapper(nextLine);
                    lineReaderMap.put(sw, reader);
                    sorted.add(sw);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error while merge chunks", e);
        } finally {
            for (BufferedReader reader : readers) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                }
            }
            for (File tmpFile : tmpFiles) {
                tmpFile.delete();
            }
        }
    }

    /* simple wrapper to write new line */
    private static class BufferedWriterWrapper extends BufferedWriter {

        BufferedWriterWrapper(Writer out) {
            super(out);
        }

        void writeLine(String str) throws IOException {
            super.write(str);
            super.write("\n");
        }
    }

    /* simple wrapper to avoid duplicates line */
    private class StringWrapper implements Comparable<StringWrapper> {
        private final String string;

        StringWrapper(String line) {
            this.string = line;
        }

        @Override
        public int compareTo(StringWrapper o) {
            return string.compareTo(o.string);
        }
    }
}
