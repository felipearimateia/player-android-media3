package br.com.dicasdoari.player

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.ExposedDropdownMenuDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.dicasdoari.player.ui.theme.PlayerAndroidTheme
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

class MainActivity : ComponentActivity() {

    private lateinit var playMediaController: PlayerMediaController
    private var currentEvent = PlayerEvent.STOPPED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PlayerAndroidTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {

                    var isPlaying by remember { mutableStateOf(false) }
                    var currentTime by remember { mutableStateOf(0L) }
                    var totalDuration by remember { mutableStateOf(0L) }

                    LaunchedEffect(Unit) {
                        while (true) {
                            delay(1.seconds)
                            currentTime = playMediaController.getCurrentPosition()
                        }
                    }

                   DisposableEffect(Unit) {
                       playMediaController.callback = { event, currentPosition, duration, playing ->
                           isPlaying = playing
                           currentEvent = event
                           currentTime = currentPosition
                           totalDuration = duration
                       }
                       onDispose { playMediaController.destroy() }
                    }

                    MusicPlayer(
                        isPlaying = isPlaying,
                        currentTime = currentTime,
                        totalDuration = totalDuration,
                        onPlayPause = { playPause() },
                        onStop = { playMediaController.stop() },
                        onSeek = { value -> playMediaController.seekTo(value) },
                        onSpeed = { value -> playMediaController.speed(value) })

                }
            }
        }

        playMediaController = PlayerMediaController(this)
    }

    private fun playPause() {
        when (currentEvent) {
            PlayerEvent.PLAYING -> playMediaController.pause()
            PlayerEvent.PAUSED -> playMediaController.resume()
            PlayerEvent.STOPPED -> play()
            else -> Unit
        }
    }

    private fun play() {
        val mediaAudio = MediaAudio(
            name = "Jazz In Paris",
            uri = "https://storage.googleapis.com/exoplayer-test-media-0/Jazz_In_Paris.mp3",
            artworkUri = Uri.parse("https://i1.sndcdn.com/artworks-000337632603-ug36ao-t500x500.jpg")
        )
        playMediaController.play(mediaAudio)
    }
}

@Composable
fun MusicPlayer(
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onStop: () -> Unit,
    onSeek: (Long) -> Unit,
    onSpeed: (Float) -> Unit,
    modifier: Modifier = Modifier,
    currentTime: Long,
    totalDuration: Long
) {

    Column(modifier = modifier
        .padding(16.dp)
        .fillMaxSize()) {
        PlayerSeekBar(currentPosition = currentTime, duration = totalDuration, onSeek = onSeek)
        Timer(currentTime = currentTime, totalDuration = totalDuration)
        PlayerControls(
            isPlaying = isPlaying,
            onPlayPause = onPlayPause,
            onStop = onStop,
            onSpeed = onSpeed,
            modifier = modifier
        )
    }

}

@Composable
fun Timer(currentTime: Long, totalDuration: Long) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = currentTime.getTimeString())
        Text(text = totalDuration.getTimeString())
    }
}

@Composable
fun PlayerSeekBar(currentPosition: Long, duration: Long, onSeek: (Long) -> Unit) {
    Slider(
        value = currentPosition.toFloat(),
        onValueChange = { value -> onSeek(value.toLong()) },
        valueRange = 0f..duration.toFloat(),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun PlayerControls(
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onStop: () -> Unit,
    onSpeed: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        val iconPlay = if (isPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle
        IconButton(onClick = onStop, modifier = Modifier.size(40.dp)) {
            Icon(Icons.Default.StopCircle, contentDescription = "Stop",  modifier = Modifier.fillMaxSize())
        }
        SpeedDropDown(onSpeed = onSpeed)
        IconButton(onClick = onPlayPause, modifier = Modifier.size(40.dp)) {
            Icon(iconPlay, contentDescription = "Play/Pause", modifier = Modifier.fillMaxSize())
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SpeedDropDown(onSpeed: (Float) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val items = listOf("0.5x", "1.0x", "1.5x", "2.0x")
    var selectedText by remember { mutableStateOf(items[1]) }

    Box(modifier = Modifier.width(100.dp)) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                value = selectedText,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)},
            )

            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false}) {
                items.forEach { value ->
                    DropdownMenuItem(onClick = {
                        expanded = false
                        onSpeed(value.replace("x","").toFloat())
                        selectedText = value
                    }) {
                        Text(text = value)
                    }
                }
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    PlayerAndroidTheme {
        MusicPlayer(
            isPlaying = false,
            onPlayPause = {},
            onStop = {},
            onSeek = {},
            onSpeed = {},
            currentTime = 0,
            totalDuration = 10
        )
    }
}

