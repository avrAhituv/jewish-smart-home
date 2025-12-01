package com.jewishhome.app.di

import android.content.Context
import com.jewishhome.app.data.local.preferences.UserPreferences
import com.jewishhome.app.data.repository.CalendarRepositoryImpl
import com.jewishhome.app.data.repository.ContactsRepositoryImpl
import com.jewishhome.app.data.repository.MusicRepositoryImpl
import com.jewishhome.app.data.repository.TextsRepositoryImpl
import com.jewishhome.app.data.repository.ZmanimRepositoryImpl
import com.jewishhome.app.domain.repository.CalendarRepository
import com.jewishhome.app.domain.repository.ContactsRepository
import com.jewishhome.app.domain.repository.MusicRepository
import com.jewishhome.app.domain.repository.TextsRepository
import com.jewishhome.app.domain.repository.ZmanimRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context = context

    @Provides
    @Singleton
    fun provideUserPreferences(@ApplicationContext context: Context): UserPreferences {
        return UserPreferences(context)
    }

    @Provides
    @Singleton
    fun provideZmanimRepository(userPreferences: UserPreferences): ZmanimRepository {
        return ZmanimRepositoryImpl(userPreferences)
    }

    @Provides
    @Singleton
    fun provideMusicRepository(
        @ApplicationContext context: Context,
        userPreferences: UserPreferences
    ): MusicRepository {
        return MusicRepositoryImpl(context, userPreferences)
    }

    @Provides
    @Singleton
    fun provideContactsRepository(
        @ApplicationContext context: Context,
        userPreferences: UserPreferences
    ): ContactsRepository {
        return ContactsRepositoryImpl(context, userPreferences)
    }

    @Provides
    @Singleton
    fun provideCalendarRepository(
        @ApplicationContext context: Context,
        userPreferences: UserPreferences
    ): CalendarRepository {
        return CalendarRepositoryImpl(context, userPreferences)
    }

    @Provides
    @Singleton
    fun provideTextsRepository(
        userPreferences: UserPreferences
    ): TextsRepository {
        return TextsRepositoryImpl(userPreferences)
    }
}
