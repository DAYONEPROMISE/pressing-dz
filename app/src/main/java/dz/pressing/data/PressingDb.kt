package dz.pressing.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Ticket::class], version = 1, exportSchema = false)
abstract class PressingDb : RoomDatabase() {
    abstract fun tickets(): TicketDao
}
