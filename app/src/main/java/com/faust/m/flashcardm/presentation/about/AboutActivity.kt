package com.faust.m.flashcardm.presentation.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.faust.m.flashcardm.R
import mehdi.sakout.aboutpage.AboutPage
import mehdi.sakout.aboutpage.Element

class AboutActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val aboutPage: View = AboutPage(this)
            .setDescription("FlashCardM is a demonstration application used for tests purposes." +
                    " It contains only the very basics features of a flash card application. If" +
                    " you are looking for a great free flash card application, you can check" +
                    " AnkiDroid")
            .addItem(ankiDroidElement())
            .addItem(wikipediaElement())
            .addItem(icon8Element())
            .addEmail("melodie.faust@gmail.com")
            .addGitHub("mfaust35")
            .addItem(versionElement())
            .create()
        setContentView(aboutPage)
    }

    private fun ankiDroidElement(): Element = Element().apply {
        title = getString(R.string.about_anki_droid)
        iconDrawable = R.drawable.about_icon_google_play

        intent = Intent(Intent.ACTION_VIEW).apply {
            val appPkgName = "com.ichi2.anki"
            data = Uri.parse("https://play.google.com/store/apps/details?id=$appPkgName")
        }
    }

    private fun wikipediaElement(): Element = Element().apply {
        title = getString(R.string.about_flash_card_wikipedia)
        iconDrawable = R.drawable.icons8_wikipedia_48

        intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://www.en.wikipedia.org/wiki/Flashcard")
        }
    }

    private fun icon8Element(): Element = Element().apply {
        title = getString(R.string.about_icons)
        iconDrawable = R.drawable.icons8_icon_48

        intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://www.icons8.com")
        }
    }

    private fun versionElement(): Element = Element().apply {
        val versionName = packageManager.getPackageInfo(packageName, 0).versionName
        title = "Version $versionName"
    }

    /*
    PackageInfo pInfo = context.getPackageManager().getPackageInfo(getPackageName(), 0);
    String version = pInfo.versionName;
     */
}