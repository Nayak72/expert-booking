package com.expertconnect.app.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Data Transfer Objects (DTOs) for Retrofit API responses.
 * These map directly from JSON to Kotlin objects.
 */

// ── Auth ─────────────────────────────────────────────────────────────────────

data class TokenResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("role") val role: String,
    @SerializedName("user_id") val userId: String
)

data class UserResponse(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("role") val role: String,
    @SerializedName("profile_image") val profileImage: String?,
    @SerializedName("created_at") val createdAt: String
)

// ── Experts ───────────────────────────────────────────────────────────────────

data class ExpertResponse(
    @SerializedName("id") val id: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("bio") val bio: String?,
    @SerializedName("expertise") val expertise: String,
    @SerializedName("skills") val skills: String?,
    @SerializedName("experience") val experience: Int,
    @SerializedName("languages") val languages: String,
    @SerializedName("pricing") val pricing: Double,
    @SerializedName("profile_image") val profileImage: String?,
    @SerializedName("categories") val categories: String?,
    @SerializedName("average_rating") val averageRating: Double,
    @SerializedName("total_reviews") val totalReviews: Int,
    @SerializedName("total_bookings") val totalBookings: Int,
    @SerializedName("is_available") val isAvailable: Int,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("user_name") val userName: String?,
    @SerializedName("user_email") val userEmail: String?,
    @SerializedName("availability") val availability: Map<String, List<String>>?,
    // Recommendation fields
    @SerializedName("similarity_score") val similarityScore: Double?,
    @SerializedName("recommendation_score") val recommendationScore: Double?
)

data class ExpertListResponse(
    @SerializedName("items") val items: List<ExpertResponse>,
    @SerializedName("total") val total: Int,
    @SerializedName("page") val page: Int,
    @SerializedName("page_size") val pageSize: Int,
    @SerializedName("total_pages") val totalPages: Int
)

// ── Bookings ──────────────────────────────────────────────────────────────────

data class BookingResponse(
    @SerializedName("id") val id: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("expert_id") val expertId: String,
    @SerializedName("booking_date") val bookingDate: String,
    @SerializedName("slot") val slot: String,
    @SerializedName("status") val status: String,
    @SerializedName("notes") val notes: String?,
    @SerializedName("meeting_link") val meetingLink: String?,
    @SerializedName("cancellation_reason") val cancellationReason: String?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    @SerializedName("expert_name") val expertName: String?,
    @SerializedName("expert_expertise") val expertExpertise: String?,
    @SerializedName("expert_profile_image") val expertProfileImage: String?,
    @SerializedName("user_name") val userName: String?
)

data class BookingListResponse(
    @SerializedName("items") val items: List<BookingResponse>,
    @SerializedName("total") val total: Int,
    @SerializedName("page") val page: Int,
    @SerializedName("page_size") val pageSize: Int,
    @SerializedName("total_pages") val totalPages: Int
)

// ── Reviews ───────────────────────────────────────────────────────────────────

data class ReviewResponse(
    @SerializedName("id") val id: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("expert_id") val expertId: String,
    @SerializedName("rating") val rating: Double,
    @SerializedName("review_text") val reviewText: String?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    @SerializedName("reviewer_name") val reviewerName: String?,
    @SerializedName("reviewer_image") val reviewerImage: String?
)

data class ReviewListResponse(
    @SerializedName("items") val items: List<ReviewResponse>,
    @SerializedName("total") val total: Int,
    @SerializedName("page") val page: Int,
    @SerializedName("page_size") val pageSize: Int,
    @SerializedName("total_pages") val totalPages: Int
)

data class RatingSummaryResponse(
    @SerializedName("expert_id") val expertId: String,
    @SerializedName("average_rating") val averageRating: Double,
    @SerializedName("total_reviews") val totalReviews: Int,
    @SerializedName("rating_distribution") val ratingDistribution: Map<String, Int>
)

// ── Favorites ─────────────────────────────────────────────────────────────────

data class FavoriteResponse(
    @SerializedName("id") val id: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("expert_id") val expertId: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("expert_expertise") val expertExpertise: String?,
    @SerializedName("expert_pricing") val expertPricing: Double?,
    @SerializedName("expert_average_rating") val expertAverageRating: Double?,
    @SerializedName("expert_profile_image") val expertProfileImage: String?,
    @SerializedName("expert_name") val expertName: String?
)

// ── Recommendations ───────────────────────────────────────────────────────────

data class RecommendationResponse(
    @SerializedName("user_id") val userId: String,
    @SerializedName("strategy") val strategy: String,
    @SerializedName("total") val total: Int,
    @SerializedName("recommendations") val recommendations: List<ExpertResponse>
)

// ── Slots ─────────────────────────────────────────────────────────────────────

data class SlotAvailabilityResponse(
    @SerializedName("expert_id") val expertId: String,
    @SerializedName("date") val date: String,
    @SerializedName("booked_slots") val bookedSlots: List<String>
)

// ── WebSocket ─────────────────────────────────────────────────────────────────

data class SlotUpdateMessage(
    @SerializedName("event") val event: String,
    @SerializedName("expert_id") val expertId: String,
    @SerializedName("booking_date") val bookingDate: String,
    @SerializedName("slot") val slot: String,
    @SerializedName("is_available") val isAvailable: Boolean
)

// ── Request bodies ────────────────────────────────────────────────────────────

data class SignupRequest(
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("role") val role: String = "user"
)

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class BookingCreateRequest(
    @SerializedName("expert_id") val expertId: String,
    @SerializedName("booking_date") val bookingDate: String,
    @SerializedName("slot") val slot: String,
    @SerializedName("notes") val notes: String? = null
)

data class BookingCancelRequest(
    @SerializedName("cancellation_reason") val cancellationReason: String? = null
)

data class BookingRescheduleRequest(
    @SerializedName("booking_date") val bookingDate: String?,
    @SerializedName("slot") val slot: String?
)

data class ReviewCreateRequest(
    @SerializedName("expert_id") val expertId: String,
    @SerializedName("rating") val rating: Double,
    @SerializedName("review_text") val reviewText: String? = null
)

data class ReviewUpdateRequest(
    @SerializedName("rating") val rating: Double?,
    @SerializedName("review_text") val reviewText: String?
)

data class FavoriteCreateRequest(
    @SerializedName("expert_id") val expertId: String
)
