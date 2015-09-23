package de.maxvogler.learningspaces.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import de.maxvogler.learningspaces.R
import org.jetbrains.anko.browse

public class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
    }

    @Suppress("UNUSED_PARAMETER")
    public fun openGithubWebsite(v: View?) {
        browse("https://github.com/mr-max/learningspaces")
    }

    @Suppress("UNUSED_PARAMETER")
    public fun openImageSource(v: View?) {
        browse("https://www.flickr.com/photos/kit-bibliothek/4923442932/")
    }

}
