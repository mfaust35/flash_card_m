package com.faust.m.flashcardm.presentation.review

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.faust.m.flashcardm.R
import com.faust.m.flashcardm.presentation.BookletViewModelFactory
import com.faust.m.flashcardm.presentation.LiveDataObserver
import com.faust.m.flashcardm.presentation.fragment_edit_card.CardEditionState
import com.faust.m.flashcardm.presentation.fragment_edit_card.CardEditionState.*
import kotlinx.android.synthetic.main.activity_booklet.fg_add_card
import kotlinx.android.synthetic.main.activity_review.*
import org.koin.android.ext.android.getKoin

class ReviewActivity: AppCompatActivity(), LiveDataObserver {

    private lateinit var viewModel: ReviewViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review)

        // Hide fragment add card
        supportFragmentManager.beginTransaction().let { ft ->
            ft.hide(fg_add_card)
            ft.commit()
        }

        viewModel =
            getKoin().get<BookletViewModelFactory>().createViewModelFrom(this)
        viewModel.reviewCard.observeData(this, ::onCurrentCardChanged)
        viewModel.cardEditionState.observeData(this, ::onCardEditionStateChanged)
    }

    private fun onCurrentCardChanged(reviewCard: ReviewCard) {
        if (reviewCard == ReviewCard.EMPTY) {
            finish()
        }
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
            ft.hide(fg_card_content)
            ft.hide(fg_actions)
            ft.commit()
        }
    }

    private fun hideFragment() {
        supportFragmentManager.beginTransaction().let { ft ->
            ft.setCustomAnimations(R.anim.appear_instant, R.anim.exit_to_bottom)
            ft.show(fg_actions)
            ft.show(fg_card_content)
            ft.hide(fg_add_card)
            ft.commit()
        }
    }

    override fun onBackPressed() =
        when {
            viewModel.onBackPressed() -> {}
            else -> super.onBackPressed()
        }
}
