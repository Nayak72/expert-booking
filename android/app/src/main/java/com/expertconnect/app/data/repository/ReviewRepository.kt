package com.expertconnect.app.data.repository

import com.expertconnect.app.data.remote.ApiService
import com.expertconnect.app.data.remote.dto.ReviewCreateRequest
import com.expertconnect.app.data.remote.dto.ReviewResponse
import com.expertconnect.app.data.remote.dto.ReviewUpdateRequest
import com.expertconnect.app.domain.model.PaginatedResult
import com.expertconnect.app.domain.model.RatingSummary
import com.expertconnect.app.domain.model.Review
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewRepository @Inject constructor(private val apiService: ApiService) {

    suspend fun createReview(expertId: String, rating: Double, reviewText: String?): Result<Review> =
        withContext(Dispatchers.IO) {
            runCatching {
                val response = apiService.createReview(ReviewCreateRequest(expertId, rating, reviewText))
                if (response.isSuccessful) response.body()!!.toDomain()
                else throw Exception(response.errorBody()?.string() ?: "Failed to submit review")
            }
        }

    suspend fun updateReview(reviewId: String, rating: Double?, reviewText: String?): Result<Review> =
        withContext(Dispatchers.IO) {
            runCatching {
                val response = apiService.updateReview(reviewId, ReviewUpdateRequest(rating, reviewText))
                if (response.isSuccessful) response.body()!!.toDomain()
                else throw Exception("Failed to update review")
            }
        }

    suspend fun getExpertReviews(expertId: String, page: Int = 1, pageSize: Int = 20): Result<PaginatedResult<Review>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val response = apiService.getExpertReviews(expertId, page, pageSize)
                if (response.isSuccessful) {
                    val dto = response.body()!!
                    PaginatedResult(dto.items.map { it.toDomain() }, dto.total, dto.page, dto.pageSize, dto.totalPages)
                } else throw Exception("Failed to load reviews")
            }
        }

    suspend fun getRatingSummary(expertId: String): Result<RatingSummary> =
        withContext(Dispatchers.IO) {
            runCatching {
                val response = apiService.getRatingSummary(expertId)
                if (response.isSuccessful) {
                    val dto = response.body()!!
                    RatingSummary(dto.expertId, dto.averageRating, dto.totalReviews, dto.ratingDistribution)
                } else throw Exception("Failed to load rating summary")
            }
        }
}

private fun ReviewResponse.toDomain() = Review(
    id = id, userId = userId, expertId = expertId, rating = rating,
    reviewText = reviewText, createdAt = createdAt, updatedAt = updatedAt,
    reviewerName = reviewerName, reviewerImage = reviewerImage
)
