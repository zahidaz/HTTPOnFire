package com.azzahid.hof.features.http.routing.routes.builtin

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.content.ContextCompat
import com.azzahid.hof.domain.model.Failure
import com.azzahid.hof.domain.model.Route
import com.azzahid.hof.features.http.androidContext
import io.github.smiley4.ktoropenapi.get
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytesWriter
import io.ktor.utils.io.writeFully
import java.nio.ByteBuffer
import java.nio.ByteOrder

private const val SAMPLE_RATE = 16000
private const val CHANNELS: Short = 1
private const val BITS_PER_SAMPLE: Short = 16

internal fun io.ktor.server.routing.Route.addMicrophoneRoute(route: Route) {
    get(route.path, {
        description = route.description
    }) {
        val context = call.application.androidContext

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            call.respond(
                HttpStatusCode.Forbidden,
                Failure(error = "Microphone permission required. Grant RECORD_AUDIO permission in device settings.")
            )
            return@get
        }

        val minBufferSize = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        if (minBufferSize == AudioRecord.ERROR_BAD_VALUE || minBufferSize == AudioRecord.ERROR) {
            call.respond(
                HttpStatusCode.InternalServerError,
                Failure(error = "Audio format not supported on this device")
            )
            return@get
        }

        val bufferSize = maxOf(minBufferSize, 4096)

        @Suppress("MissingPermission")
        val audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )

        if (audioRecord.state != AudioRecord.STATE_INITIALIZED) {
            audioRecord.release()
            call.respond(
                HttpStatusCode.InternalServerError,
                Failure(error = "Failed to initialize microphone")
            )
            return@get
        }

        val wavHeader = buildWavHeader()

        audioRecord.startRecording()
        try {
            call.respondBytesWriter(contentType = ContentType("audio", "wav")) {
                writeFully(wavHeader)
                flush()
                val buffer = ByteArray(bufferSize)
                while (true) {
                    val read = audioRecord.read(buffer, 0, buffer.size)
                    if (read > 0) {
                        writeFully(buffer, 0, read)
                        flush()
                    }
                }
            }
        } finally {
            audioRecord.stop()
            audioRecord.release()
        }
    }
}

private fun buildWavHeader(): ByteArray =
    ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN).apply {
        put("RIFF".toByteArray())
        putInt(Int.MAX_VALUE)
        put("WAVE".toByteArray())
        put("fmt ".toByteArray())
        putInt(16)
        putShort(1)
        putShort(CHANNELS)
        putInt(SAMPLE_RATE)
        putInt(SAMPLE_RATE * CHANNELS * BITS_PER_SAMPLE / 8)
        putShort((CHANNELS * BITS_PER_SAMPLE / 8).toShort())
        putShort(BITS_PER_SAMPLE)
        put("data".toByteArray())
        putInt(Int.MAX_VALUE)
    }.array()
