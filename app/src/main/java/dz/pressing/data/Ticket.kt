package dz.pressing.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** Ticket status: RECU -> PRET -> LIVRE (forward only). */
object TicketStatus {
    const val RECU = "recu"
    const val PRET = "pret"
    const val LIVRE = "livre"
    val ALL = listOf(RECU, PRET, LIVRE)
}

@Entity(tableName = "tickets")
data class Ticket(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientName: String,
    val phone: String = "",
    val itemsLabel: String,          // e.g. "3 chemises, 1 costume"
    val itemCount: Int = 1,
    val priceDzd: Int,               // total price in DZD
    val status: String = TicketStatus.RECU,
    val createdAt: Long = System.currentTimeMillis(),
    val readyAt: Long = System.currentTimeMillis(),
    val deliveredAt: Long? = null,
    val paid: Boolean = false,
    val notes: String = ""
)

@Dao
interface TicketDao {
    @Insert suspend fun insert(t: Ticket): Long
    @Delete suspend fun delete(t: Ticket)

    @Query("SELECT * FROM tickets ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<Ticket>>

    @Query("SELECT * FROM tickets WHERE status = :status ORDER BY createdAt DESC")
    fun observeByStatus(status: String): Flow<List<Ticket>>

    @Query("SELECT * FROM tickets WHERE id = :id")
    suspend fun getById(id: Long): Ticket?

    @Query("UPDATE tickets SET status = :status, deliveredAt = :deliveredAt WHERE id = :id")
    suspend fun setStatus(id: Long, status: String, deliveredAt: Long?)

    @Query("UPDATE tickets SET paid = 1 WHERE id = :id")
    suspend fun markPaid(id: Long)

    @Query("SELECT COALESCE(SUM(priceDzd),0) FROM tickets WHERE paid = 1 AND createdAt >= :dayStart AND createdAt < :dayEnd")
    suspend fun takingsForDay(dayStart: Long, dayEnd: Long): Int
}
