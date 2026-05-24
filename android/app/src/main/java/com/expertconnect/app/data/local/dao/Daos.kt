package com.expertconnect.app.data.local.dao

import androidx.room.*
import com.expertconnect.app.data.local.entity.BookingEntity
import com.expertconnect.app.data.local.entity.FavoriteEntity
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO for cached bookings.
 * Exposes Flow-based queries for reactive UI updates.
 */
@Dao
interface BookingDao {

    @Query("SELECT * FROM cached_bookings ORDER BY bookingDate DESC")
    fun observeAllBookings(): Flow<List<BookingEntity>>

    @Query("SELECT * FROM cached_bookings WHERE status IN ('pending', 'confirmed') ORDER BY bookingDate ASC")
    fun observeUpcomingBookings(): Flow<List<BookingEntity>>

    @Query("SELECT * FROM cached_bookings WHERE id = :id")
    suspend fun getBookingById(id: String): BookingEntity?

    @Upsert
    suspend fun upsertBookings(bookings: List<BookingEntity>)

    @Upsert
    suspend fun upsertBooking(booking: BookingEntity)

    @Query("DELETE FROM cached_bookings WHERE id = :id")
    suspend fun deleteBooking(id: String)

    @Query("DELETE FROM cached_bookings")
    suspend fun clearAll()

    @Query("DELETE FROM cached_bookings WHERE cachedAt < :threshold")
    suspend fun evictOldCache(threshold: Long)
}

/**
 * Room DAO for cached favorites.
 */
@Dao
interface FavoriteDao {

    @Query("SELECT * FROM cached_favorites ORDER BY createdAt DESC")
    fun observeFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM cached_favorites WHERE expertId = :expertId LIMIT 1")
    suspend fun getFavoriteByExpertId(expertId: String): FavoriteEntity?

    @Upsert
    suspend fun upsertFavorites(favorites: List<FavoriteEntity>)

    @Upsert
    suspend fun upsertFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM cached_favorites WHERE expertId = :expertId")
    suspend fun deleteFavoriteByExpertId(expertId: String)

    @Query("DELETE FROM cached_favorites")
    suspend fun clearAll()
}

/**
 * Room DAO for cached expert profile and availability.
 */
@Dao
interface ExpertProfileDao {
    @Query("SELECT * FROM cached_expert_profile WHERE userId = :userId LIMIT 1")
    suspend fun getMyExpertProfile(userId: String): com.expertconnect.app.data.local.entity.ExpertProfileEntity?

    @Upsert
    suspend fun upsertExpertProfile(profile: com.expertconnect.app.data.local.entity.ExpertProfileEntity)
    
    @Query("DELETE FROM cached_expert_profile")
    suspend fun clearAll()
}
