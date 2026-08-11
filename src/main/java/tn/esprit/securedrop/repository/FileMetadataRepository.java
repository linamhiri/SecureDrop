package tn.esprit.securedrop.repository;

import tn.esprit.securedrop.model.FileMetadata;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class FileMetadataRepository {

    private final ConcurrentMap<String, FileMetadata> files =
            new ConcurrentHashMap<>();

    public FileMetadata save(FileMetadata fileMetadata) {

        files.put(fileMetadata.getId(), fileMetadata);

        return fileMetadata;
    }

    public Optional<FileMetadata> findById(String id) {
        return Optional.ofNullable(files.get(id));
    }

    public List<FileMetadata> findAll() {

        return files.values()
                .stream()
                .sorted(
                        Comparator
                                .comparing(FileMetadata::getUploadedAt)
                                .reversed()
                )
                .toList();
    }

    public void deleteById(String id) {
        files.remove(id);
    }
}