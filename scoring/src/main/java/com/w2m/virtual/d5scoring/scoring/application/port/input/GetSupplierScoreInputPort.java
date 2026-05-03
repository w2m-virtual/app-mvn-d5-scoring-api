package com.w2m.virtual.d5scoring.scoring.application.port.input;

import com.w2m.virtual.d5scoring.scoring.domain.SupplierScore;

import java.util.Optional;
import java.util.UUID;

public interface GetSupplierScoreInputPort {
    Optional<SupplierScore> getBySupplierId(UUID supplierId);
}
