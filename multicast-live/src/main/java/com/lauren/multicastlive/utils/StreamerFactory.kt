package com.lauren.multicastlive.utils

import android.Manifest
import android.content.Context
import android.media.MediaFormat
import android.util.Size
import androidx.annotation.RequiresPermission
import io.github.thibaultbee.streampack.data.AudioConfig
import io.github.thibaultbee.streampack.data.VideoConfig
import io.github.thibaultbee.streampack.ext.srt.streamers.CameraSrtLiveStreamer
import io.github.thibaultbee.streampack.internal.muxers.ts.data.TsServiceInfo
import io.github.thibaultbee.streampack.streamers.interfaces.IStreamer

class StreamerFactory(private val context: Context) {private fun createStreamer(context: Context): IStreamer {
        return CameraSrtLiveStreamer(context, enableAudio = true)
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun build(): IStreamer {
        val streamer = createStreamer(context)

        val videoConfig = VideoConfig(
            mimeType = MediaFormat.MIMETYPE_VIDEO_AVC,
            startBitrate = 2000 * 1000, // to b/s
            resolution = Size(640, 480),
            fps = 30
        )

        val audioConfig = AudioConfig(
            mimeType = MediaFormat.MIMETYPE_AUDIO_AAC,
            startBitrate = 128000,
            sampleRate = 48000,
            channelConfig = AudioConfig.getChannelConfig(2),
            byteFormat = 2
        )

        streamer.configure(videoConfig)
        streamer.configure(audioConfig)

        return streamer
    }
}