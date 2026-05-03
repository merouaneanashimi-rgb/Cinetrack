package com.cinetrack.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.cinetrack.R

class CineTrackWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CineTrackWidget()
}

class CineTrackWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                WidgetContent()
            }
        }
    }
}

@Composable
fun WidgetContent() {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(R.color.widget_background))
            .padding(16.dp)
    ) {
        Text(
            text = "Continue Watching",
            style = TextStyle(
                color = ColorProvider(R.color.primary),
                fontSize = 16.sp
            )
        )
        Spacer(modifier = GlanceModifier.height(8.dp))
        // Show placeholder for widget content
        // In production, this would load actual data from Room
        Text(
            text = "Open CineTrack to see your shows",
            style = TextStyle(
                color = ColorProvider(R.color.on_surface_variant),
                fontSize = 12.sp
            )
        )
    }
}
