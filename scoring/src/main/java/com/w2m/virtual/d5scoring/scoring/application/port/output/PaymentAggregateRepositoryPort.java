package com.w2m.virtual.d5scoring.scoring.application.port.output;

import com.w2m.virtual.d5scoring.scoring.domain.PaymentAggregate;

public interface PaymentAggregateRepositoryPort {
    PaymentAggregate get();
    PaymentAggregate update(PaymentAggregate aggregate);
}
