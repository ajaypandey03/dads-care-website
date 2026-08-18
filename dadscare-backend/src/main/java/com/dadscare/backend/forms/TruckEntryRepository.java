package com.dadscare.backend.forms;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TruckEntryRepository extends JpaRepository<TruckEntry, Long> {

    List<TruckEntry> findAllByGodownFormId(Long godownFormId);
}
