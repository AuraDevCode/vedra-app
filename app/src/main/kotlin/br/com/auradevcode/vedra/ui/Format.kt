package br.com.auradevcode.vedra.ui

import android.content.Context
import br.com.auradevcode.vedra.R

fun formatCountdown(ms: Long): String {
    val total = (ms + 999) / 1000
    return "%d:%02d".format(total / 60, total % 60)
}

fun formatDuration(context: Context, ms: Long): String =
    if (ms < 60_000) context.getString(R.string.duration_seconds, ms / 1000)
    else context.getString(R.string.duration_minutes, ms / 60_000)

fun formatMicros(micros: Long): String =
    if (micros < 1000) "$micros µs" else "%.1f ms".format(micros / 1000.0)
