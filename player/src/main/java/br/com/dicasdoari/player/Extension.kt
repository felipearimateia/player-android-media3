package br.com.dicasdoari.player

fun Long.getTimeString(): String {
    val buf = StringBuffer()

    val minutes = (this % (1000 * 60 * 60) / (1000 * 60)).toInt()
    val seconds = (this % (1000 * 60 * 60) % (1000 * 60) / 1000).toInt()

    buf
        .append(String.format("%02d", minutes))
        .append(":")
        .append(String.format("%02d", seconds))

    return buf.toString()
}