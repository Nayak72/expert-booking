package com.expertconnect.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.expertconnect.app.data.local.dao.BookingDao
import com.expertconnect.app.data.local.dao.FavoriteDao
import com.expertconnect.app.data.local.dao.ExpertProfileDao
import com.expertconnect.app.data.local.entity.BookingEntity
import com.expertconnect.app.data.local.entity.ExpertProfileEntity
import com.expertconnect.app.data.local.entity.FavoriteEntity

/**
 * Room database definition for ExpertConnect.
 * Contains tables for offline caching of bookings and favorites.
 *
 * Built via DatabaseModule (Hilt DI).
 */
@Database(
    entities = [
        BookingEntity::class,
        FavoriteEntity::class,
        ExpertProfileEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookingDao(): BookingDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun expertProfileDao(): ExpertProfileDao

    companion object {
        const val DATABASE_NAME = "expertconnect_db"
    }
}
