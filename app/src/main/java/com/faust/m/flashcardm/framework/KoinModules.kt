package com.faust.m.flashcardm.framework

import com.faust.m.core.data.BookletDataSource
import com.faust.m.core.data.BookletRepository
import com.faust.m.core.data.CardRepository
import com.faust.m.flashcardm.framework.db.room.BookletRoomDataSource
import com.faust.m.flashcardm.framework.db.room.CardRoomDataSource
import com.faust.m.flashcardm.framework.db.room.definition.FlashRoomDatabase
import com.faust.m.flashcardm.presentation.ViewModelFactory
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.bind
import org.koin.dsl.module

val roomDatabaseModule = module {

    single {
        FlashRoomDatabase.getInstance(androidApplication())
    }

    single { BookletRepository(get()) }
    single {
        BookletRoomDataSource(get<FlashRoomDatabase>().bookletDao())
    } bind BookletDataSource::class

    single { CardRepository(get()) }
    single {
        CardRoomDataSource(
            get<FlashRoomDatabase>().cardDao(),
            get<FlashRoomDatabase>().cardContentDao()
        )
    }
}

val viewModelModule = module {

    single { ViewModelFactory() }

}