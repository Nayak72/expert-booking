package com.expertconnect.app.data.repository

import com.expertconnect.app.data.local.dao.FavoriteDao
import com.expertconnect.app.data.local.entity.FavoriteEntity
import com.expertconnect.app.data.remote.ApiService
import com.expertconnect.app.data.remote.dto.FavoriteCreateRequest
import com.expertconnect.app.data.remote.dto.FavoriteResponse
import com.expertconnect.app.domain.model.Favorite
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteRepository @Inject constructor(
    private val apiService: ApiService,
    private val favoriteDao: FavoriteDao
) {
    /** Reactive cached favorites list. */
    val cachedFavorites: Flow<List<Favorite>> =
        favoriteDao.observeFavorites().map { it.map { e -> e.toDomain() } }

    suspend fun getFavorites(): Result<List<Favorite>> = withContext(Dispatchers.IO) {
        runCatching {
            val response = apiService.getFavorites()
            if (response.isSuccessful) {
                val dtos = response.body()!!
                favoriteDao.upsertFavorites(dtos.map { it.toEntity() })
                dtos.map { it.toDomain() }
            } else throw Exception("Failed to load favorites")
        }
    }

    suspend fun addFavorite(expertId: String): Result<Favorite> = withContext(Dispatchers.IO) {
        runCatching {
            val response = apiService.addFavorite(FavoriteCreateRequest(expertId))
            if (response.isSuccessful) {
                val dto = response.body()!!
                favoriteDao.upsertFavorite(dto.toEntity())
                dto.toDomain()
            } else throw Exception(response.errorBody()?.string() ?: "Failed to add favorite")
        }
    }

    suspend fun removeFavorite(expertId: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            apiService.removeFavorite(expertId)
            favoriteDao.deleteFavoriteByExpertId(expertId)
        }
    }

    suspend fun isFavorited(expertId: String): Boolean {
        return favoriteDao.getFavoriteByExpertId(expertId) != null
    }
}

private fun FavoriteResponse.toDomain() = Favorite(
    id = id, userId = userId, expertId = expertId, createdAt = createdAt,
    expertExpertise = expertExpertise, expertPricing = expertPricing,
    expertAverageRating = expertAverageRating, expertProfileImage = expertProfileImage,
    expertName = expertName
)

private fun FavoriteResponse.toEntity() = FavoriteEntity(
    id = id, userId = userId, expertId = expertId, createdAt = createdAt,
    expertExpertise = expertExpertise, expertPricing = expertPricing,
    expertAverageRating = expertAverageRating, expertProfileImage = expertProfileImage,
    expertName = expertName
)

private fun FavoriteEntity.toDomain() = Favorite(
    id = id, userId = userId, expertId = expertId, createdAt = createdAt,
    expertExpertise = expertExpertise, expertPricing = expertPricing,
    expertAverageRating = expertAverageRating, expertProfileImage = expertProfileImage,
    expertName = expertName
)
