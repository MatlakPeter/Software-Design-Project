package core.processor;

import model.FileData;

import java.io.File;

public interface FileProcessor {
    boolean supports(File file);
    FileData process(File file);
}
