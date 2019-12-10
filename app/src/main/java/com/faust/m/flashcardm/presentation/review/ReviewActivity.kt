package com.faust.m.flashcardm.presentation.review

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.faust.m.flashcardm.R
import com.faust.m.flashcardm.presentation.BookletViewModelFactory
import com.faust.m.flashcardm.presentation.LiveDataObserver
import com.faust.m.flashcardm.presentation.fragment_edit_card.CardEditionState
import com.faust.m.flashcardm.presentation.fragment_edit_card.CardEditionState.*
import kotlinx.android.synthetic.main.activity_booklet.fg_add_card
import kotlinx.android.synthetic.main.activity_review.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.warn
import org.koin.android.ext.android.getKoin
import java.util.*

class ReviewActivity: AppCompatActivity(), LiveDataObserver, AnkoLogger {

    private lateinit var viewModel: ReviewViewModel
    private var textToSpeech: TextToSpeech? = null


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
        viewModel.textToSpeak.observeData(this, ::onNewTextToSpeak)
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

    private fun onNewTextToSpeak(text: String) {
        when (textToSpeech) {
            null -> initializeTextToSpeech()
            else -> speak(text)
        }
    }

    private fun initializeTextToSpeech() {
        warnUserForDelayDuringInitialization()

        /*
        I am using applicationContext here instead of the activity. Using the activity is causing
        a leak, even though I do release properly the TextToSpeech by calling :
        - textToSpeech.shutdown()
        - textToSpeech = null // to release the reference, just in case
        Still the service seems to keep a reference to my activity, according to LeakCanary ar least
        More information :
        https://developer.android.com/reference/android/speech/tts/TextToSpeech.html#shutdown%28%29
        https://stackoverflow.com/questions/7298731/when-to-call-activity-context-or-application-context (answer from CommonsWare)
         */
        textToSpeech = TextToSpeech(applicationContext) { status ->
            when (status) {
                TextToSpeech.SUCCESS -> afterInitialized()
                else -> resetTextToSpeechAfterErrorStatus(status)
            }
        }
    }

    private fun warnUserForDelayDuringInitialization() =
        Toast.makeText(this, R.string.initialize_text_to_speech, Toast.LENGTH_SHORT).show()

    private fun afterInitialized() {
        textToSpeech?.language = Locale.US
        viewModel.onTextToSpeechInitialized()
    }

    private fun resetTextToSpeechAfterErrorStatus(status: Int) {
        warn { "Text to speech initialization failed with status $status" }
        textToSpeech?.shutdown()
        textToSpeech = null
    }

    private fun speak(text: String) {
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "Card")
    }

    override fun onBackPressed() =
        when {
            viewModel.onBackPressed() -> {}
            else -> super.onBackPressed()
        }

    override fun onDestroy() {
        textToSpeech?.run {
            stop()
            shutdown()
        }
        textToSpeech = null
        super.onDestroy()

    }
}
