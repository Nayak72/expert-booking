package com.expertconnect.app.domain.model

/**
 * Domain models — pure Kotlin data classes used in the business logic layer.
 * These are independent of any framework (Retrofit, Room, etc.).
 */

data class User(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    val profileImage: String?,
    val createdAt: String
)

data class AuthToken(
    val accessToken: String,
    val tokenType: String,
    val role: String,
    val userId: String
)

data class Expert(
    val id: String,
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
    val userEmail: String? = null,
    val availability: Map<String, List<String>> = emptyMap(),
    // Recommendation score (populated by recommendation API)
    val similarityScore: Double? = null,
    val recommendationScore: Double? = null
)

data class Booking(
    val id: String,
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
    val userName: String?
)

data class Review(
    val id: String,
    val userId: String,
    val expertId: String,
    val rating: Double,
    val reviewText: String?,
    val createdAt: String,
    val updatedAt: String,
    val reviewerName: String?,
    val reviewerImage: String?
)

data class Favorite(
    val id: String,
    val userId: String,
    val expertId: String,
    val createdAt: String,
    val expertExpertise: String?,
    val expertPricing: Double?,
    val expertAverageRating: Double?,
    val expertProfileImage: String?,
    val expertName: String?
)

data class RatingSummary(
    val expertId: String,
    val averageRating: Double,
    val totalReviews: Int,
    val ratingDistribution: Map<String, Int>
)

data class SlotUpdate(
    val event: String,        // "slot_booked" | "slot_released"
    val expertId: String,
    val bookingDate: String,
    val slot: String,
    val isAvailable: Boolean
)

data class PaginatedResult<T>(
    val items: List<T>,
    val total: Int,
    val page: Int,
    val pageSize: Int,
    val totalPages: Int
)
