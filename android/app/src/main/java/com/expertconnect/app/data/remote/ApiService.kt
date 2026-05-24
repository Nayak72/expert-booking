package com.expertconnect.app.data.remote

import com.expertconnect.app.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit API service interface.
 * Declares all backend endpoints as Kotlin suspend functions.
 */
interface ApiService {

    // ── Authentication ────────────────────────────────────────────────────────

    @POST("api/auth/signup")
    suspend fun signup(@Body request: SignupRequest): Response<UserResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<TokenResponse>

    @GET("api/auth/me")
    suspend fun getMe(): Response<UserResponse>

    @PATCH("api/auth/me")
    suspend fun updateMe(@Body request: Map<String, @JvmSuppressWildcards Any>): Response<UserResponse>

    @POST("api/auth/fcm-token")
    suspend fun updateFcmToken(@Query("fcm_token") fcmToken: String): Response<Map<String, String>>

    // ── Experts ───────────────────────────────────────────────────────────────

    @GET("api/experts")
    suspend fun getExperts(
        @Query("search") search: String? = null,
        @Query("expertise") expertise: String? = null,
        @Query("category") category: String? = null,
        @Query("min_rating") minRating: Float? = null,
        @Query("max_price") maxPrice: Float? = null,
        @Query("min_experience") minExperience: Int? = null,
        @Query("language") language: String? = null,
        @Query("is_available") isAvailable: Int? = null,
        @Query("sort_by") sortBy: String = "average_rating",
        @Query("sort_order") sortOrder: String = "desc",
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20
    ): Response<ExpertListResponse>

    @GET("api/experts/me")
    suspend fun getMyExpertProfile(): Response<ExpertResponse>

    @GET("api/experts/{expert_id}")
    suspend fun getExpert(@Path("expert_id") expertId: String): Response<ExpertResponse>

    @POST("api/experts")
    suspend fun createExpertProfile(@Body request: Map<String, @JvmSuppressWildcards Any>): Response<ExpertResponse>

    @PATCH("api/experts/{expert_id}")
    suspend fun updateExpertProfile(
        @Path("expert_id") expertId: String,
        @Body request: Map<String, @JvmSuppressWildcards Any>
    ): Response<ExpertResponse>

    @GET("api/experts/{expert_id}/slots")
    suspend fun getBookedSlots(
        @Path("expert_id") expertId: String,
        @Query("date") date: String
    ): Response<SlotAvailabilityResponse>

    // ── Bookings ──────────────────────────────────────────────────────────────

    @POST("api/bookings")
    suspend fun createBooking(@Body request: BookingCreateRequest): Response<BookingResponse>

    @HTTP(method = "DELETE", path = "api/bookings/{booking_id}", hasBody = true)
    suspend fun cancelBooking(
        @Path("booking_id") bookingId: String,
        @Body request: BookingCancelRequest = BookingCancelRequest()
    ): Response<BookingResponse>

    @PATCH("api/bookings/{booking_id}/reschedule")
    suspend fun rescheduleBooking(
        @Path("booking_id") bookingId: String,
        @Body request: BookingRescheduleRequest
    ): Response<BookingResponse>

    @PATCH("api/bookings/{booking_id}/confirm")
    suspend fun confirmBooking(
        @Path("booking_id") bookingId: String
    ): Response<BookingResponse>

    @GET("api/bookings/history")
    suspend fun getBookingHistory(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20,
        @Query("status") status: String? = null
    ): Response<BookingListResponse>

    @GET("api/bookings/upcoming")
    suspend fun getUpcomingBookings(): Response<List<BookingResponse>>

    // ── Reviews ───────────────────────────────────────────────────────────────

    @POST("api/reviews")
    suspend fun createReview(@Body request: ReviewCreateRequest): Response<ReviewResponse>

    @PATCH("api/reviews/{review_id}")
    suspend fun updateReview(
        @Path("review_id") reviewId: String,
        @Body request: ReviewUpdateRequest
    ): Response<ReviewResponse>

    @GET("api/reviews/expert/{expert_id}")
    suspend fun getExpertReviews(
        @Path("expert_id") expertId: String,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20
    ): Response<ReviewListResponse>

    @GET("api/reviews/expert/{expert_id}/summary")
    suspend fun getRatingSummary(
        @Path("expert_id") expertId: String
    ): Response<RatingSummaryResponse>

    // ── Favorites ─────────────────────────────────────────────────────────────

    @POST("api/favorites")
    suspend fun addFavorite(@Body request: FavoriteCreateRequest): Response<FavoriteResponse>

    @DELETE("api/favorites/{expert_id}")
    suspend fun removeFavorite(@Path("expert_id") expertId: String): Response<Void>

    @GET("api/favorites")
    suspend fun getFavorites(): Response<List<FavoriteResponse>>

    @GET("api/favorites/{expert_id}/check")
    suspend fun checkFavorite(@Path("expert_id") expertId: String): Response<Map<String, Boolean>>

    // ── Recommendations ───────────────────────────────────────────────────────

    @GET("api/recommendations")
    suspend fun getRecommendations(
        @Query("top_n") topN: Int = 10
    ): Response<RecommendationResponse>
}
