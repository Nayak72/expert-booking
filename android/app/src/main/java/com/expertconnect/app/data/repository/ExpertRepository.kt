package com.expertconnect.app.data.repository

import com.expertconnect.app.data.remote.ApiService
import com.expertconnect.app.domain.model.Expert
import com.expertconnect.app.domain.model.PaginatedResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Expert repository: discovery, search, profile management.
 * Maps DTOs to domain models.
 */
@Singleton
class ExpertRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getExperts(
        search: String? = null,
        expertise: String? = null,
        category: String? = null,
        minRating: Float? = null,
        maxPrice: Float? = null,
        minExperience: Int? = null,
        language: String? = null,
        isAvailable: Int? = null,
        sortBy: String = "average_rating",
        sortOrder: String = "desc",
        page: Int = 1,
        pageSize: Int = 20
    ): Result<PaginatedResult<Expert>> = withContext(Dispatchers.IO) {
        runCatching {
            val response = apiService.getExperts(
                search, expertise, category, minRating, maxPrice,
                minExperience, language, isAvailable, sortBy, sortOrder, page, pageSize
            )
            if (response.isSuccessful) {
                val dto = response.body()!!
                PaginatedResult(
                    items = dto.items.map { it.toDomain() },
                    total = dto.total,
                    page = dto.page,
                    pageSize = dto.pageSize,
                    totalPages = dto.totalPages
                )
            } else {
                throw Exception(response.errorBody()?.string() ?: "Failed to load experts")
            }
        }
    }

    suspend fun getExpert(expertId: String): Result<Expert> = withContext(Dispatchers.IO) {
        runCatching {
            val response = apiService.getExpert(expertId)
            if (response.isSuccessful) {
                response.body()!!.toDomain()
            } else {
                throw Exception("Expert not found")
            }
        }
    }

    suspend fun getBookedSlots(expertId: String, date: String): Result<List<String>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val response = apiService.getBookedSlots(expertId, date)
                if (response.isSuccessful) {
                    response.body()!!.bookedSlots
                } else {
                    emptyList()
                }
            }
        }

    suspend fun getMyExpertProfile(): Result<Expert> = withContext(Dispatchers.IO) {

        runCatching {
            val response = apiService.getMyExpertProfile()
            if (response.isSuccessful) {
                response.body()!!.toDomain()
            } else {
                throw Exception("Failed to load expert profile: ${response.code()}")
            }
        }
    }

    suspend fun updateExpertProfile(
        expertId: String,
        updateData: Map<String, Any>
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val response = apiService.updateExpertProfile(expertId, updateData)
            if (!response.isSuccessful) {
                throw Exception(response.errorBody()?.string() ?: "Update failed")
            }
        }
    }

    suspend fun createExpertProfile(
        createData: Map<String, Any>
    ): Result<Expert> = withContext(Dispatchers.IO) {
        runCatching {
            val response = apiService.createExpertProfile(createData)
            if (response.isSuccessful) {
                response.body()!!.toDomain()
            } else {
                throw Exception(response.errorBody()?.string() ?: "Create failed")
            }
        }
    }

    suspend fun updateExpertAvailability(
        expertId: String,
        availability: Map<String, List<String>>
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val updateData: Map<String, @JvmSuppressWildcards Any> = mapOf("availability" to availability)
            val response = apiService.updateExpertProfile(expertId, updateData)
            if (!response.isSuccessful) {
                throw Exception(response.errorBody()?.string() ?: "Failed to save availability")
            }
        }
    }
}


private fun com.expertconnect.app.data.remote.dto.ExpertResponse.toDomain() = Expert(
    id = id,
    userId = userId,
    bio = bio,
    expertise = expertise,
    skills = skills,
    experience = experience,
    languages = languages,
    pricing = pricing,
    profileImage = profileImage,
    categories = categories,
    averageRating = averageRating,
    totalReviews = totalReviews,
    totalBookings = totalBookings,
    isAvailable = isAvailable,
    createdAt = createdAt,
    userName = userName,
    userEmail = userEmail,
    availability = availability ?: emptyMap(),
    similarityScore = similarityScore,
    recommendationScore = recommendationScore
)
