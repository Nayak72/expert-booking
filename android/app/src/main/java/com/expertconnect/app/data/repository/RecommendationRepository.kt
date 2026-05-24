package com.expertconnect.app.data.repository

import com.expertconnect.app.data.remote.ApiService
import com.expertconnect.app.domain.model.Expert
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Recommendation repository: fetches ML-powered expert recommendations.
 */
@Singleton
class RecommendationRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getRecommendations(topN: Int = 10): Result<List<Expert>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val response = apiService.getRecommendations(topN)
                if (response.isSuccessful) {
                    val dto = response.body()!!
                    dto.recommendations.map { expertDto ->
                        Expert(
                            id = expertDto.id,
                            userId = expertDto.userId,
                            bio = expertDto.bio,
                            expertise = expertDto.expertise,
                            skills = expertDto.skills,
                            experience = expertDto.experience,
                            languages = expertDto.languages,
                            pricing = expertDto.pricing,
                            profileImage = expertDto.profileImage,
                            categories = expertDto.categories,
                            averageRating = expertDto.averageRating,
                            totalReviews = expertDto.totalReviews,
                            totalBookings = expertDto.totalBookings,
                            isAvailable = expertDto.isAvailable,
                            createdAt = expertDto.createdAt,
                            userName = expertDto.userName,
                            similarityScore = expertDto.similarityScore,
                            recommendationScore = expertDto.recommendationScore
                        )
                    }
                } else throw Exception("Failed to load recommendations")
            }
        }
}
