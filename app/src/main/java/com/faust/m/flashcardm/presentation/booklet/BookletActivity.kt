package com.faust.m.flashcardm.presentation.booklet

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.faust.m.flashcardm.R
import com.faust.m.flashcardm.presentation.BookletViewModelFactory
import com.faust.m.flashcardm.presentation.LiveDataObserver
import com.faust.m.flashcardm.presentation.fragment_edit_card.CardEditionState
import com.faust.m.flashcardm.presentation.fragment_edit_card.CardEditionState.*
import kotlinx.android.synthetic.main.activity_booklet.*
import org.koin.android.ext.android.getKoin


class BookletActivity: AppCompatActivity(), LiveDataObserver {

    private lateinit var viewModel: BookletViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booklet)

        // Hide fragment add card
        supportFragmentManager.beginTransaction().let { ft ->
            ft.hide(fg_add_card)
            ft.commit()
        }

        viewModel = getKoin().get<BookletViewModelFactory>().createViewModelFrom(this)
        viewModel.cardEditionState.observeData(this, ::onCardEditionStateChanged)
        viewModel.loadData()
    }

    private fun onCardEditionStateChanged(cardEditionState: CardEditionState) =
        when(cardEditionState) {
            EDIT, ADD -> showFragment()
            CLOSED -> hideFragment()
        }

    private fun showFragment() {
        supportFragmentManager.beginTransaction().let { ft ->
            ft.setCustomAnimations(R.anim.enter_from_bottom, R.anim.disappear_after_delay)
            ft.show(fg_add_card)
            ft.hide(fg_card_list)
            ft.commit()
        }
        supportFragmentManager.findFragmentById(R.id.fg_card_list)?.let {
            (it as FragmentCardList).makeFABAnimation(false)
        }
    }

    private fun hideFragment() {
        supportFragmentManager.beginTransaction().let { ft ->
            ft.setCustomAnimations(R.anim.appear_instant, R.anim.exit_to_bottom)
            ft.show(fg_card_list)
            ft.hide(fg_add_card)
            ft.commit()
        }
        supportFragmentManager.findFragmentById(R.id.fg_card_list)?.let {
            (it as FragmentCardList).makeFABAnimation(true)
        }
    }


    override fun onBackPressed() =
        when {
            viewModel.onBackPressed() -> {}
            else -> super.onBackPressed()
        }
}
