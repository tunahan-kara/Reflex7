package com.tunahankara.reflex7.audio

import android.media.AudioManager
import android.media.ToneGenerator
import android.os.SystemClock

enum class SoundCue { START, SUCCESS, FAILURE, LEVEL, URGENCY, MODIFIER, RULE, BEST, SIGNAL }

class RetroAudio {
    private var toneGenerator: ToneGenerator? = null
    private var enabled = true
    private var lastPlayedAt = 0L

    fun setEnabled(value: Boolean) {
        enabled = value
        if (!value) stop()
    }

    fun play(cue: SoundCue) {
        if (!enabled) return
        val now = SystemClock.elapsedRealtime()
        val cooldown = if (cue == SoundCue.URGENCY) 420 else 65
        if (now - lastPlayedAt < cooldown) return
        lastPlayedAt = now
        if (toneGenerator == null) toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 28)
        val tone = when (cue) {
            SoundCue.START -> ToneGenerator.TONE_PROP_ACK
            SoundCue.SUCCESS -> ToneGenerator.TONE_PROP_BEEP
            SoundCue.FAILURE -> ToneGenerator.TONE_PROP_NACK
            SoundCue.LEVEL -> ToneGenerator.TONE_DTMF_5
            SoundCue.URGENCY -> ToneGenerator.TONE_DTMF_1
            SoundCue.MODIFIER -> ToneGenerator.TONE_DTMF_6
            SoundCue.RULE -> ToneGenerator.TONE_DTMF_3
            SoundCue.BEST -> ToneGenerator.TONE_DTMF_9
            SoundCue.SIGNAL -> ToneGenerator.TONE_PROP_PROMPT
        }
        val duration = when (cue) {
            SoundCue.FAILURE, SoundCue.BEST -> 110
            SoundCue.START, SoundCue.RULE -> 80
            else -> 45
        }
        toneGenerator?.startTone(tone, duration)
    }

    fun stop() { toneGenerator?.stopTone() }
    fun release() { toneGenerator?.release(); toneGenerator = null }
}
