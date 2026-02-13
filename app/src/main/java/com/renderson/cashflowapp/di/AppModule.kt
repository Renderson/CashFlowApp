package com.renderson.cashflowapp.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.renderson.cashflowapp.classifier.TransactionCategoryClassifier
import com.renderson.cashflowapp.data.ClashFlowDatabase
import com.renderson.cashflowapp.data.preferences.SessionDataStore
import com.renderson.cashflowapp.data.preferences.SettingsDataStore
import com.renderson.cashflowapp.data.repository.AuthRepository
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

private val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS recurring_transactions (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                description TEXT NOT NULL,
                type TEXT NOT NULL,
                category TEXT NOT NULL,
                amount REAL NOT NULL,
                start_date TEXT NOT NULL,
                end_date TEXT,
                frequency TEXT NOT NULL,
                next_occurrence TEXT NOT NULL,
                is_active INTEGER NOT NULL DEFAULT 1
            )
            """.trimIndent()
        )
    }
}

private val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE years ADD COLUMN userId TEXT DEFAULT ''")
        db.execSQL("ALTER TABLE recurring_transactions ADD COLUMN userId TEXT DEFAULT ''")
        db.execSQL("UPDATE years SET userId = '' WHERE userId IS NULL")
        db.execSQL("UPDATE recurring_transactions SET userId = '' WHERE userId IS NULL")
    }
}

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): ClashFlowDatabase {
        return Room.databaseBuilder(context, ClashFlowDatabase::class.java, "cashFlow.db")
            .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
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

    @Singleton
    @Provides
    fun provideSessionDataStore(@ApplicationContext context: Context): SessionDataStore =
        SessionDataStore(context)

    @Singleton
    @Provides
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Singleton
    @Provides
    fun provideAuthRepository(firebaseAuth: FirebaseAuth): AuthRepository =
        AuthRepository(firebaseAuth)

    @Singleton
    @Provides
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()
}