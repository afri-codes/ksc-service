package ksc.go.tz.masterData.repository;

import ksc.go.tz.masterData.entities.Slot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SlotRepository extends JpaRepository<Slot, UUID> {
}
