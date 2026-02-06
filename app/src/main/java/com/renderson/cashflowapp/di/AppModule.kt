package com.renderson.cashflowapp.di

import android.content.Context
import androidx.room.Room
import com.renderson.cashflowapp.data.ClashFlowDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): ClashFlowDatabase {
        return Room.databaseBuilder(context, ClashFlowDatabase::class.java, "cashFlow.db")
            .fallbackToDestructiveMigration()
            .build()
    }
}