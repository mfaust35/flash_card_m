package com.faust.m.flashcardm.framework

import com.faust.m.core.data.BookletDataSource
import com.faust.m.core.data.BookletRepository
import com.faust.m.core.data.CardDataSource
import com.faust.m.core.data.CardRepository
import com.faust.m.core.usecase.*
import com.faust.m.flashcardm.framework.db.room.BookletRoomDataSource
import com.faust.m.flashcardm.framework.db.room.CardRoomDataSource
import com.faust.m.flashcardm.framework.db.room.definition.FlashRoomDatabase
import com.faust.m.flashcardm.presentation.BaseViewModelFactory
import com.faust.m.flashcardm.presentation.BookletViewModelFactory
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
        CardRoomDataSource(get())
    } bind CardDataSource::class
}

val viewModelModule = module {

    factory { BaseViewModelFactory() }
    factory { BookletViewModelFactory() }
    single { FlashViewModel(androidApplication())}

}

val useCases = module {

    single { GetBookletsOutlines(get()) }
    single { AddBooklet(get()) }
    single { DeleteBooklet(get()) }
    single { GetBooklets(get()) }
    single { UpdateCard(get()) }
    single { RenameBooklet(get()) }
    single { GetCardsToReviewForBooklet(get()) }
    single { GetCardsForBooklet(get()) }
    single { AddCard(get()) }
    single { GetBooklet(get()) }
    single { UpdateCardContent(get()) }
    single { UseCases(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }

}