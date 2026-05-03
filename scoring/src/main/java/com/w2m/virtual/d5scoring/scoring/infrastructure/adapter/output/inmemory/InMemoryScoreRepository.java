package com.w2m.virtual.d5scoring.scoring.infrastructure.adapter.output.inmemory;

import com.w2m.virtual.d5scoring.scoring.application.port.output.SupplierScoreRepositoryPort;
import com.w2m.virtual.d5scoring.scoring.domain.SupplierScore;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryScoreRepository implements SupplierScoreRepositoryPort {

    private final ConcurrentHashMap<UUID, SupplierScore> store = new ConcurrentHashMap<>();

    @Override
    public Optional<SupplierScore> findById(UUID supplierId) {
        return Optional.ofNullable(store.get(supplierId));
    }

    @Override
    public List<SupplierScore> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public SupplierScore save(SupplierScore score) {
        store.put(score.supplierId(), score);
        return score;
    }
}
