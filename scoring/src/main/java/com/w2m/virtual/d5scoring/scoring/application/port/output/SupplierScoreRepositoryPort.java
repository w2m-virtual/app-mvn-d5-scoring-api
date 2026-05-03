package com.w2m.virtual.d5scoring.scoring.application.port.output;

import com.w2m.virtual.d5scoring.scoring.domain.SupplierScore;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SupplierScoreRepositoryPort {
    Optional<SupplierScore> findById(UUID supplierId);

    List<SupplierScore> findAll();

    SupplierScore save(SupplierScore score);
}
