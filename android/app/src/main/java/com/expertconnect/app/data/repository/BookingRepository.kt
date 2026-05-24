package com.expertconnect.app.data.repository

import com.expertconnect.app.data.local.dao.BookingDao
import com.expertconnect.app.data.local.entity.BookingEntity
import com.expertconnect.app.data.remote.ApiService
import com.expertconnect.app.data.remote.dto.BookingCancelRequest
import com.expertconnect.app.data.remote.dto.BookingCreateRequest
import com.expertconnect.app.data.remote.dto.BookingRescheduleRequest
import com.expertconnect.app.data.remote.dto.BookingResponse
import com.expertconnect.app.domain.model.Booking
import com.expertconnect.app.domain.model.PaginatedResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Booking repository: creates, cancels, reschedules, and caches bookings in Room.
 * Implements offline-first pattern: reads from Room cache, writes through to API.
 */
@Singleton
class BookingRepository @Inject constructor(
    private val apiService: ApiService,
    private val bookingDao: BookingDao
) {
    /** Cached upcoming bookings as a reactive Flow. */
    val cachedUpcomingBookings: Flow<List<Booking>> =
        bookingDao.observeUpcomingBookings().map { entities ->
            entities.map { it.toDomain() }
        }

    /** All cached bookings (history). */
    val cachedBookings: Flow<List<Booking>> =
        bookingDao.observeAllBookings().map { entities ->
            entities.map { it.toDomain() }
        }

    suspend fun createBooking(
        expertId: String,
        bookingDate: String,
        slot: String,
        notes: String? = null
    ): Result<Booking> = withContext(Dispatchers.IO) {
        runCatching {
            val response = apiService.createBooking(
                BookingCreateRequest(expertId, bookingDate, slot, notes)
            )
            if (response.isSuccessful) {
                val dto = response.body()!!
                val booking = dto.toDomain()
                bookingDao.upsertBooking(dto.toEntity())
                booking
            } else {
                throw Exception(response.errorBody()?.string() ?: "Booking failed")
            }
        }
    }

    suspend fun cancelBooking(bookingId: String, reason: String? = null): Result<Booking> =
        withContext(Dispatchers.IO) {
            runCatching {
                val response = apiService.cancelBooking(bookingId, BookingCancelRequest(reason))
                if (response.isSuccessful) {
                    val dto = response.body()!!
                    bookingDao.upsertBooking(dto.toEntity())
                    dto.toDomain()
                } else {
                    throw Exception(response.errorBody()?.string() ?: "Cancellation failed")
                }
            }
        }

    suspend fun confirmBooking(bookingId: String): Result<Booking> =
        withContext(Dispatchers.IO) {
            runCatching {
                val response = apiService.confirmBooking(bookingId)
                if (response.isSuccessful) {
                    val dto = response.body()!!
                    bookingDao.upsertBooking(dto.toEntity())
                    dto.toDomain()
                } else {
                    throw Exception(response.errorBody()?.string() ?: "Confirmation failed")
                }
            }
        }

    suspend fun rescheduleBooking(
        bookingId: String,
        newDate: String?,
        newSlot: String?
    ): Result<Booking> = withContext(Dispatchers.IO) {
        runCatching {
            val response = apiService.rescheduleBooking(
                bookingId, BookingRescheduleRequest(newDate, newSlot)
            )
            if (response.isSuccessful) {
                val dto = response.body()!!
                bookingDao.upsertBooking(dto.toEntity())
                dto.toDomain()
            } else {
                throw Exception(response.errorBody()?.string() ?: "Reschedule failed")
            }
        }
    }

    suspend fun getBookingHistory(
        page: Int = 1,
        pageSize: Int = 20,
        status: String? = null
    ): Result<PaginatedResult<Booking>> = withContext(Dispatchers.IO) {
        runCatching {
            val response = apiService.getBookingHistory(page, pageSize, status)
            if (response.isSuccessful) {
                val dto = response.body()!!
                // Update cache
                bookingDao.upsertBookings(dto.items.map { it.toEntity() })
                PaginatedResult(
                    items = dto.items.map { it.toDomain() },
                    total = dto.total,
                    page = dto.page,
                    pageSize = dto.pageSize,
                    totalPages = dto.totalPages
                )
            } else {
                throw Exception("Failed to load history")
            }
        }
    }

    suspend fun getUpcomingBookings(): Result<List<Booking>> = withContext(Dispatchers.IO) {
        runCatching {
            val response = apiService.getUpcomingBookings()
            if (response.isSuccessful) {
                val dtos = response.body()!!
                bookingDao.upsertBookings(dtos.map { it.toEntity() })
                dtos.map { it.toDomain() }
            } else {
                throw Exception("Failed to load upcoming bookings")
            }
        }
    }
}

private fun BookingResponse.toDomain() = Booking(
    id = id, userId = userId, expertId = expertId,
    bookingDate = bookingDate, slot = slot, status = status,
    notes = notes, meetingLink = meetingLink,
    cancellationReason = cancellationReason,
    createdAt = createdAt, updatedAt = updatedAt,
    expertName = expertName, expertExpertise = expertExpertise,
    expertProfileImage = expertProfileImage, userName = userName
)

private fun BookingResponse.toEntity() = BookingEntity(
    id = id, userId = userId, expertId = expertId,
    bookingDate = bookingDate, slot = slot, status = status,
    notes = notes, meetingLink = meetingLink,
    cancellationReason = cancellationReason,
    createdAt = createdAt, updatedAt = updatedAt,
    expertName = expertName, expertExpertise = expertExpertise,
    expertProfileImage = expertProfileImage, userName = userName
)

private fun BookingEntity.toDomain() = Booking(
    id = id, userId = userId, expertId = expertId,
    bookingDate = bookingDate, slot = slot, status = status,
    notes = notes, meetingLink = meetingLink,
    cancellationReason = cancellationReason,
    createdAt = createdAt, updatedAt = updatedAt,
    expertName = expertName, expertExpertise = expertExpertise,
    expertProfileImage = expertProfileImage, userName = userName
)
