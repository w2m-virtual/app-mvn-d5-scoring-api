package com.w2m.virtual.d5scoring.scoring.infrastructure.adapter.input.rest;

import com.w2m.virtual.d5scoring.scoring.application.port.input.GetPaymentAggregateInputPort;
import com.w2m.virtual.d5scoring.scoring.application.port.input.GetSupplierScoreInputPort;
import com.w2m.virtual.d5scoring.scoring.application.port.input.ListSupplierScoresInputPort;
import com.w2m.virtual.d5scoring.scoring.infrastructure.adapter.input.rest.data.ScoringDtos.PaymentAggregateResponse;
import com.w2m.virtual.d5scoring.scoring.infrastructure.adapter.input.rest.data.ScoringDtos.SupplierScoreResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/scoring")
public class ScoringRestAdapter {

    private final GetSupplierScoreInputPort getPort;
    private final ListSupplierScoresInputPort listPort;
    private final GetPaymentAggregateInputPort paymentsPort;

    public ScoringRestAdapter(GetSupplierScoreInputPort getPort,
                              ListSupplierScoresInputPort listPort,
                              GetPaymentAggregateInputPort paymentsPort) {
        this.getPort = getPort;
        this.listPort = listPort;
        this.paymentsPort = paymentsPort;
    }

    @GetMapping("/suppliers")
    public List<SupplierScoreResponse> list() {
        return listPort.listAll().stream().map(SupplierScoreResponse::from).toList();
    }

    @GetMapping("/suppliers/{supplierId}")
    public ResponseEntity<SupplierScoreResponse> get(@PathVariable UUID supplierId) {
        return getPort.getBySupplierId(supplierId)
                .map(SupplierScoreResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/payments")
    public PaymentAggregateResponse payments() {
        return PaymentAggregateResponse.from(paymentsPort.get());
    }
}
