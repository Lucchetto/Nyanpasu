package com.zhenxiang.nyaa.preference

import android.content.Context
import android.media.MediaPlayer
import android.util.AttributeSet
import android.view.View
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.zhenxiang.nyaa.R

class FooterPreference: Preference {
    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int): super(context, attrs, defStyleAttr, defStyleRes)

    private var mediaPlayer: MediaPlayer? = null

    init {
        layoutResource = R.layout.footer_preference
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        holder.itemView.findViewById<View>(R.id.renge).setOnClickListener { _ ->
            playNyanpasu()
        }
    }

    private fun playNyanpasu() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
                it.start()
            }
        } ?: run {
            val newPlayer = MediaPlayer.create(context, R.raw.nyanpasu)
            newPlayer.setOnCompletionListener {
                it.release()
                mediaPlayer = null
            }
            newPlayer.start()
            mediaPlayer = newPlayer
        }
    }
}