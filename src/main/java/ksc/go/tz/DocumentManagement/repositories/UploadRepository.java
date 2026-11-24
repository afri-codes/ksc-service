package ksc.go.tz.DocumentManagement.repositories;

import ksc.go.tz.DocumentManagement.entities.Upload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UploadRepository extends JpaRepository<Upload, UUID> {
    Optional<Upload> findByFileName(String fileName);
}
