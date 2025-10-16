package com.pwnagotchi.pwnagotchiandroid.widgets

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.currentState
import androidx.glance.layout.Column
import androidx.glance.layout.padding
import androidx.glance.text.Text
import com.pwnagotchi.pwnagotchiandroid.R

class StatusWidget : GlanceAppWidget() {
    @Composable
    override fun Content() {
        val state = currentState<WidgetState>()

        Column(modifier = GlanceModifier.padding(8.dp)) {
            Image(
                provider = ImageProvider(
                    when (state.face) {
                        "(•‿•)", "(•́‿•̀)", "(^‿^)" -> R.drawable.pwnagotchi_happy
                        "(☓‿‿☓)", "(#__#)", "(-__-)" -> R.drawable.pwnagotchi_sad
                        else -> R.drawable.pwnagotchi_neutral
                    }
                ),
                contentDescription = "Pwnagotchi Face"
            )
            Text(text = state.message)
        }
    }
}

class StatusWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = StatusWidget()
}
