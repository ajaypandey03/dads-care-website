package com.dadscare.backend.forms;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockLineRepository extends JpaRepository<StockLine, Long> {

    List<StockLine> findAllByGodownFormId(Long godownFormId);
}
