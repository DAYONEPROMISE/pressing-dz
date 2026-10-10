package dz.pressing.data

/** Pure business rules — fully unit-tested on JVM, no Android deps. */
object TicketLogic {

    fun validate(client: String, items: String, priceDzd: Int): List<String> {
        val errs = mutableListOf<String>()
        if (client.isBlank()) errs += "client_required"
        if (items.isBlank()) errs += "items_required"
        if (priceDzd <= 0) errs += "price_positive"
        if (priceDzd > 10_000_000) errs += "price_too_large"
        return errs
    }

    /** Forward-only transitions: recu->pret->livre. */
    fun canTransition(from: String, to: String): Boolean = when (from) {
        TicketStatus.RECU -> to == TicketStatus.PRET
        TicketStatus.PRET -> to == TicketStatus.LIVRE
        TicketStatus.LIVRE -> false
        else -> false
    }

    fun nextStatus(from: String): String? = when (from) {
        TicketStatus.RECU -> TicketStatus.PRET
        TicketStatus.PRET -> TicketStatus.LIVRE
        else -> null
    }

    fun dayBounds(dayMillis: Long): Pair<Long, Long> {
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = dayMillis
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        return start to start + 86_400_000L
    }

    fun formatDzd(amount: Int): String =
        java.text.NumberFormat.getIntegerInstance(java.util.Locale.ROOT).format(amount).replace(',', ' ') + " DA"
}
