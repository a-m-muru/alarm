package ee.ut.cs.alarm.gaming

import android.content.Context
import android.media.MediaPlayer

object AudioPlayer {
    fun playSound(ctx: Context, soundId: Int) {
        val mp = MediaPlayer.create(ctx, soundId)
        mp.setOnCompletionListener { mp -> mp.release() }
        mp.start()
    }
}