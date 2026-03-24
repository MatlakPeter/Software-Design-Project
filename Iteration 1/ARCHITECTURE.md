# Architecture of Iteration 1 (C4 Model)

## Level 1: System Context
The system is a local file search engine that allows users to search for files on their local machine based on filename (and content ??).

### Actors:
- **User**: performs searches for files on their local machine
### External Systems:
- **File System**: provides access to files on the local machine
- **Database**: stores data about files


## Level 2: Container
The system consists of the following containers:

### 1. **Search Application**
- Written in Java
- Handles file directory parsing, database interactions, and querying

### 2. **Database**
- Written in PostgreSQL
- Stores information about the text files (filename, content, metadata)


## Level 3: Components
### Crawler
- Parses recursively through directories
- Finds text files and collects file data (filename, content, metadata)

### Indexer
- Gets file data from the *Crawler*
- Transforms file data into a format suitable for storage in the database
- Stores the transformed data in the database
- Updates the database when files are added, modified, or deleted



### Indexer
- Gets file data from the Crawler
- Transforms file data into a format suitable for storage in the database
- Stores/updates/deletes file data via *FileRepository*

### Query Processor
- Receives search queries from the *UI component*
- Retrieves results via *FileRepository*
- sends results to the *Preview Generator*

### FileRepository
- Provides a single place for database operations
- Methods for:
    - storing files
    - updating files
    - deleting files
    - retrieving files for queries

### Preview Generator
- Extracts relevant snippets from file content thus preparing user-friendly previews for the results

### UI Component
- **IDK if this is necessary for iteration 1, but if not it can just be excluded**
- Provides a user interface for users 
- Accepts search queries
- Displays search results with previews

### Interactions:
- `Crawler` -> `Indexer` -> `FileRepository` -> `Database`
- `User` -> `UI Component` -> `Query Processor` ->  `FileRepository` -> `Database` ->  `FileRepository` ->`Query Processor` -> `Preview Generator` -> `UI Component` -> `User`


## Level 4: Classes
### Crawler
- scanDirectory(String path): void
- parseFile(File file): FileData

### FileData
- filename: String
- content: String
- ...

### Indexer
- storeFile() -> uses FileRepository
- updateFile() -> uses FileRepository
- deleteFile() -> uses FileRepository

### Query Processor
- getQuery()
- executeQuery(String query) -> uses FileRepository
- sendResultsToPreviewGenerator()

### FileRepository
- saveFile(FileData fileData)
- updateFile(FileData fileData)
- deleteFile(String filename)
- queryFiles(String query)

### Preview Generator
- generatePreview()
- sendPreviewToUIComponent()

### UI Component
- getQuery()
- displayResults()