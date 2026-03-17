package uk.ac.tees.mad.photowhisper.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import uk.ac.tees.mad.photowhisper.data.local.dao.MemoryDao
import uk.ac.tees.mad.photowhisper.data.local.entity.MemoryEntity

@Database(entities = [MemoryEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun memoryDao(): MemoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "photo_whisper_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}