package dz.pressing

import dz.pressing.data.TicketLogic
import dz.pressing.data.TicketStatus
import org.junit.Assert.*
import org.junit.Test

class TicketLogicTest {

    @Test fun `validate rejects blank client`() {
        assertTrue(TicketLogic.validate("", "2 chemises", 500).contains("client_required"))
    }

    @Test fun `validate rejects blank items`() {
        assertTrue(TicketLogic.validate("Ahmed", "", 500).contains("items_required"))
    }

    @Test fun `validate rejects zero price`() {
        assertTrue(TicketLogic.validate("Ahmed", "1 costume", 0).contains("price_positive"))
    }

    @Test fun `validate accepts good ticket`() {
        assertTrue(TicketLogic.validate("Ahmed", "3 chemises", 600).isEmpty())
    }

    @Test fun `status flows forward only`() {
        assertTrue(TicketLogic.canTransition(TicketStatus.RECU, TicketStatus.PRET))
        assertTrue(TicketLogic.canTransition(TicketStatus.PRET, TicketStatus.LIVRE))
        assertFalse(TicketLogic.canTransition(TicketStatus.RECU, TicketStatus.LIVRE))
        assertFalse(TicketLogic.canTransition(TicketStatus.PRET, TicketStatus.RECU))
        assertFalse(TicketLogic.canTransition(TicketStatus.LIVRE, TicketStatus.RECU))
    }

    @Test fun `next status chain`() {
        assertEquals(TicketStatus.PRET, TicketLogic.nextStatus(TicketStatus.RECU))
        assertEquals(TicketStatus.LIVRE, TicketLogic.nextStatus(TicketStatus.PRET))
        assertNull(TicketLogic.nextStatus(TicketStatus.LIVRE))
    }

    @Test fun `day bounds span 24h`() {
        val (s, e) = TicketLogic.dayBounds(System.currentTimeMillis())
        assertEquals(86_400_000L, e - s)
    }

    @Test fun `format DZD`() {
        assertEquals("1 500 DA", TicketLogic.formatDzd(1500))
        assertEquals("1 000 000 DA", TicketLogic.formatDzd(1_000_000))
    }
}
