package com.organistaapp.di

import android.content.Context
import androidx.room.Room
import com.organistaapp.data.local.AppDatabase
import com.organistaapp.data.local.EscalaDao
import com.organistaapp.data.local.EventoDao
import com.organistaapp.data.local.OrganistaDao
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
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "organista_db"
        ).build()
    }

    @Provides
    fun provideOrganistaDao(db: AppDatabase): OrganistaDao = db.organistaDao()

    @Provides
    fun provideEscalaDao(db: AppDatabase): EscalaDao = db.escalaDao()

    @Provides
    fun provideEventoDao(db: AppDatabase): EventoDao = db.eventoDao()

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context = context
}
