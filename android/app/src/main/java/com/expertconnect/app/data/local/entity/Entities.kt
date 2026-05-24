package com.expertconnect.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for caching bookings locally for offline access.
 */
@Entity(tableName = "cached_bookings")
data class BookingEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val expertId: String,
    val bookingDate: String,
    val slot: String,
    val status: String,
    val notes: String?,
    val meetingLink: String?,
    val cancellationReason: String?,
    val createdAt: String,
    val updatedAt: String,
    val expertName: String?,
    val expertExpertise: String?,
    val expertProfileImage: String?,
    val userName: String?,
    val cachedAt: Long = System.currentTimeMillis()
)

/**
 * Room entity for caching favorited experts locally.
 */
@Entity(tableName = "cached_favorites")
data class FavoriteEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val expertId: String,
    val createdAt: String,
    val expertExpertise: String?,
    val expertPricing: Double?,
    val expertAverageRating: Double?,
    val expertProfileImage: String?,
    val expertName: String?,
    val cachedAt: Long = System.currentTimeMillis()
)

/**
 * Room entity for caching the expert's own profile and availability.
 */
@Entity(tableName = "cached_expert_profile")
data class ExpertProfileEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val bio: String?,
    val expertise: String,
    val skills: String?,
    val experience: Int,
    val languages: String,
    val pricing: Double,
    val profileImage: String?,
    val categories: String?,
    val averageRating: Double,
    val totalReviews: Int,
    val totalBookings: Int,
    val isAvailable: Int,
    val createdAt: String,
    val userName: String?,
    val userEmail: String?,
    val availabilityJson: String, // Cached availability slots
    val cachedAt: Long = System.currentTimeMillis()
)
