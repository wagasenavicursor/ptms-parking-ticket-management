package com.ptms.service; import org.junit.jupiter.api.Test; import java.util.*; import static org.assertj.core.api.Assertions.*;
class TicketCombinationServiceTest{@Test void minimumCombination(){var r=new TicketCombinationService().combinationsFor(14);assertThat(r).contains(List.of(12,2));assertThat(r).allSatisfy(x->assertThat(x).hasSize(2));}}
