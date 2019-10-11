package com.faust.m.flashcardm.framework

import com.faust.m.core.usecase.AddBooklet
import com.faust.m.core.usecase.DeleteBooklet
import com.faust.m.core.usecase.GetBookletsOutlines
import com.faust.m.core.usecase.GetBooklets

class UseCases(val getBookletsOutlines: GetBookletsOutlines,
               val addBooklet: AddBooklet,
               val deleteBooklet: DeleteBooklet,
               val getBooklets: GetBooklets
)