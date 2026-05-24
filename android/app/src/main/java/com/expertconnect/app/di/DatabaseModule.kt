package com.expertconnect.app.di

import android.content.Context
import androidx.room.Room
import com.expertconnect.app.data.local.AppDatabase
import com.expertconnect.app.data.local.dao.BookingDao
import com.expertconnect.app.data.local.dao.FavoriteDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing Room database and DAO dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()  // For dev; use proper migrations in production
            .build()

    @Provides
    @Singleton
    fun provideBookingDao(db: AppDatabase): BookingDao = db.bookingDao()

    @Provides
    @Singleton
    fun provideFavoriteDao(db: AppDatabase): FavoriteDao = db.favoriteDao()

    @Provides
    @Singleton
    fun provideExpertProfileDao(db: AppDatabase): com.expertconnect.app.data.local.dao.ExpertProfileDao = db.expertProfileDao()
}
