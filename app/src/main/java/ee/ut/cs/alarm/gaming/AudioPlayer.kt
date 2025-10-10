package ee.ut.cs.alarm.gaming

import android.content.Context
import android.media.MediaPlayer
import android.media.PlaybackParams

object AudioPlayer {
    fun playSound(ctx: Context, soundId: Int, params: PlaybackParams = PlaybackParams()) {
        val mp = MediaPlayer.create(ctx, soundId)
        mp.setOnCompletionListener { mp -> mp.release() }
        mp.playbackParams = params
        mp.start()
    }
}