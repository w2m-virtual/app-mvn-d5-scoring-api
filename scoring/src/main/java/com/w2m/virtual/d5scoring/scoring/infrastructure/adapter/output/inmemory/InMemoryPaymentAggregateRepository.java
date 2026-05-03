package com.w2m.virtual.d5scoring.scoring.infrastructure.adapter.output.inmemory;

import com.w2m.virtual.d5scoring.scoring.application.port.output.PaymentAggregateRepositoryPort;
import com.w2m.virtual.d5scoring.scoring.domain.PaymentAggregate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.atomic.AtomicReference;

@Repository
public class InMemoryPaymentAggregateRepository implements PaymentAggregateRepositoryPort {

    private final AtomicReference<PaymentAggregate> ref = new AtomicReference<>(PaymentAggregate.empty());

    @Override
    public PaymentAggregate get() {
        return ref.get();
    }

    @Override
    public PaymentAggregate update(PaymentAggregate aggregate) {
        ref.set(aggregate);
        return aggregate;
    }
}
