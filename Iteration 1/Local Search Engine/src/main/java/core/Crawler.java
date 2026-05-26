package core;

import core.processor.FileProcessor;
import core.processor.ImageFileProcessor;
import core.processor.TextFileProcessor;
import model.FileData;
import repository.FileRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.nio.charset.StandardCharsets;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.charset.UnmappableCharacterException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;

public class Crawler {
    private String ignoreExtension;
    private FileRepository repository;
    private Set<String> scannedPaths;

    private int filesScannedCount; // counter for feedback while crawling

    private final List<FileProcessor> processors;


    public Crawler(String ignoreExtension, FileRepository repository) {
        this.ignoreExtension = ignoreExtension;
        this.repository = repository;
        this.scannedPaths = new HashSet<>();

        this.processors = List.of(
                new TextFileProcessor(),
                new ImageFileProcessor()
        );
    }

    public Set<String> getScannedPaths() {
        return scannedPaths;
    }
    public void resetScannedPaths() {
        scannedPaths.clear();
        filesScannedCount = 0;
    }

    public List<FileData> scanDirectory(File directory) {
        List<FileData> results = new ArrayList<>();
        scanDirectoryRecursive(directory, results);
        return results;
    }

    public void scanDirectoryRecursive(File directory, List<FileData> results) {
        File[] files = directory.listFiles();
        if (files == null) { // Error handling for permissions
            System.out.println("Warning: Access denied or not a directory -> " + directory.getAbsolutePath());
            return;
        }

        for (File file : files) {
            filesScannedCount++;
            if (filesScannedCount % 1000 == 0) { // Print feeckack for every 1000th file
                System.out.println("... Still scanning. Files checked: " + filesScannedCount + " (Currently at: " + file.getParent() + ")");
            }

            if (Files.isSymbolicLink(file.toPath())) { return; }

            if (file.isDirectory()) {
                scanDirectoryRecursive(file, results);
            } else if (file.isFile() && !file.getName().endsWith(ignoreExtension)) {
                FileData fileData = processFile(file);
                if (fileData != null) {
                    scannedPaths.add(file.getAbsolutePath());
                    results.add(fileData);
                }
            }
        }
    }

    public void scanDirectoryParallel(File directory, BlockingQueue<FileData> queue, ExecutorService readers) {
        scanDirectoryParallelRecursive(directory, queue, readers);
    }

    private void scanDirectoryParallelRecursive(File directory, BlockingQueue<FileData> queue, ExecutorService readers) {
        File[] files = directory.listFiles();
        if (files == null) { // Error handling for permissions
            System.out.println("Warning: Access denied or not a directory -> " + directory.getAbsolutePath());
            return;
        }

        for (File file : files) {
            filesScannedCount++;
            if (filesScannedCount % 1000 == 0) { // Print feeckack for every 1000th file
                System.out.println("... Still scanning. Files checked: " + filesScannedCount + " (Currently at: " + file.getParent() + ")");
            }

            if (Files.isSymbolicLink(file.toPath())) { continue; }

            if (file.isDirectory()) {
                scanDirectoryParallelRecursive(file, queue, readers);
            } else if (file.isFile() && !file.getName().endsWith(ignoreExtension)) {
                scannedPaths.add(file.getAbsolutePath());

                // Delegate the actual file parsing to a reader thread.
                readers.submit(() -> {
                    FileData fileData = processFile(file);
                    if (fileData == null) return;


                    fileData.setPathScore(PathScorer.score(fileData));

                    try {
                        queue.put(fileData); // blocks if the queue is full
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
        }
    }

    private FileData processFile(File file) {
        for (FileProcessor processor : processors) {
            if (processor.supports(file)) {
                return processor.process(file);
            }
        }
        return null; // unsupported file type -> skip
    }
}