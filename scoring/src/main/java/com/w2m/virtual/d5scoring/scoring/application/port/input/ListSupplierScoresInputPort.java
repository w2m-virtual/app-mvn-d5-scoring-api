package com.w2m.virtual.d5scoring.scoring.application.port.input;

import com.w2m.virtual.d5scoring.scoring.domain.SupplierScore;

import java.util.List;

public interface ListSupplierScoresInputPort {
    List<SupplierScore> listAll();
}
