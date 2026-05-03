package com.w2m.virtual.d5scoring.scoring.infrastructure.adapter.input.rest;

import com.w2m.virtual.d5scoring.scoring.application.service.PaymentScoringService;
import com.w2m.virtual.d5scoring.scoring.application.service.ScoringService;
import com.w2m.virtual.d5scoring.scoring.domain.BookingEvent;
import com.w2m.virtual.d5scoring.scoring.infrastructure.adapter.output.inmemory.InMemoryPaymentAggregateRepository;
import com.w2m.virtual.d5scoring.scoring.infrastructure.adapter.output.inmemory.InMemoryScoreRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ScoringRestAdapterTest {

    @Test
    void getBySupplierId_returnsScore() throws Exception {
        InMemoryScoreRepository repo = new InMemoryScoreRepository();
        ScoringService svc = new ScoringService(repo);
        UUID supplierId = UUID.randomUUID();
        svc.applyEvent(new BookingEvent(UUID.randomUUID(), supplierId, "h-1", BookingEvent.CONFIRMED, Instant.now()));
        svc.applyEvent(new BookingEvent(UUID.randomUUID(), supplierId, "h-1", BookingEvent.REJECTED, Instant.now()));

        PaymentScoringService payScv = new PaymentScoringService(new InMemoryPaymentAggregateRepository());
        ScoringRestAdapter adapter = new ScoringRestAdapter(svc, svc, payScv);
        MockMvc mvc = MockMvcBuilders.standaloneSetup(adapter).build();

        mvc.perform(get("/api/scoring/suppliers/" + supplierId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.supplierId").value(supplierId.toString()))
                .andExpect(jsonPath("$.totalAttempts").value(2))
                .andExpect(jsonPath("$.confirmed").value(1))
                .andExpect(jsonPath("$.rejected").value(1));
    }

    @Test
    void list_returnsAll() throws Exception {
        InMemoryScoreRepository repo = new InMemoryScoreRepository();
        ScoringService svc = new ScoringService(repo);
        UUID s1 = UUID.randomUUID();
        UUID s2 = UUID.randomUUID();
        svc.applyEvent(new BookingEvent(UUID.randomUUID(), s1, "h", BookingEvent.CONFIRMED, Instant.now()));
        svc.applyEvent(new BookingEvent(UUID.randomUUID(), s2, "h", BookingEvent.CONFIRMED, Instant.now()));

        PaymentScoringService payScv = new PaymentScoringService(new InMemoryPaymentAggregateRepository());
        ScoringRestAdapter adapter = new ScoringRestAdapter(svc, svc, payScv);
        MockMvc mvc = MockMvcBuilders.standaloneSetup(adapter).build();

        mvc.perform(get("/api/scoring/suppliers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getUnknown_returns404() throws Exception {
        InMemoryScoreRepository repo = new InMemoryScoreRepository();
        ScoringService svc = new ScoringService(repo);
        PaymentScoringService payScv = new PaymentScoringService(new InMemoryPaymentAggregateRepository());
        ScoringRestAdapter adapter = new ScoringRestAdapter(svc, svc, payScv);
        MockMvc mvc = MockMvcBuilders.standaloneSetup(adapter).build();

        mvc.perform(get("/api/scoring/suppliers/" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }
}
