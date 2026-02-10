package com.renderson.cashflowapp.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.renderson.cashflowapp.classifier.TransactionCategoryClassifier
import com.renderson.cashflowapp.data.ClashFlowDatabase
import com.renderson.cashflowapp.data.preferences.SettingsDataStore
import com.renderson.cashflowapp.usecase.SuggestTransactionCategoryUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE transactions ADD COLUMN category TEXT NOT NULL DEFAULT 'OUTROS'")
    }
}

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): ClashFlowDatabase {
        return Room.databaseBuilder(context, ClashFlowDatabase::class.java, "cashFlow.db")
            .addMigrations(MIGRATION_2_3)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Singleton
    @Provides
    fun provideTransactionCategoryClassifier(): TransactionCategoryClassifier =
        TransactionCategoryClassifier()

    @Singleton
    @Provides
    fun provideSuggestTransactionCategoryUseCase(
        classifier: TransactionCategoryClassifier
    ): SuggestTransactionCategoryUseCase =
        SuggestTransactionCategoryUseCase(classifier)

    @Singleton
    @Provides
    fun provideSettingsDataStore(@ApplicationContext context: Context): SettingsDataStore =
        SettingsDataStore(context)
}