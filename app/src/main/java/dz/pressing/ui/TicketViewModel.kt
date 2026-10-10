package dz.pressing.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import dz.pressing.data.PressingDb
import dz.pressing.data.Ticket
import dz.pressing.data.TicketLogic
import dz.pressing.data.TicketStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TicketViewModel(app: Application) : AndroidViewModel(app) {
    private val db = Room.databaseBuilder(app, PressingDb::class.java, "pressingdz.db").build()
    private val dao = db.tickets()

    val tickets: StateFlow<List<Ticket>> =
        dao.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var filter by mutableStateOf<String?>(null)
    var takingsToday by mutableStateOf(0)
        private set

    fun refreshTakings() {
        viewModelScope.launch {
            val (s, e) = TicketLogic.dayBounds(System.currentTimeMillis())
            takingsToday = dao.takingsForDay(s, e)
        }
    }

    fun add(client: String, phone: String, items: String, count: Int, price: Int, readyAt: Long, onError: (String) -> Unit) {
        val errs = TicketLogic.validate(client, items, price)
        if (errs.isNotEmpty()) { onError(errs.first()); return }
        viewModelScope.launch {
            dao.insert(Ticket(clientName = client.trim(), phone = phone.trim(), itemsLabel = items.trim(),
                itemCount = count.coerceAtLeast(1), priceDzd = price, readyAt = readyAt))
            refreshTakings()
        }
    }

    fun advance(t: Ticket) {
        val next = TicketLogic.nextStatus(t.status) ?: return
        viewModelScope.launch {
            dao.setStatus(t.id, next, if (next == TicketStatus.LIVRE) System.currentTimeMillis() else null)
        }
    }

    fun markPaid(t: Ticket) {
        viewModelScope.launch { dao.markPaid(t.id); refreshTakings() }
    }

    fun delete(t: Ticket) {
        viewModelScope.launch { dao.delete(t); refreshTakings() }
    }
}
