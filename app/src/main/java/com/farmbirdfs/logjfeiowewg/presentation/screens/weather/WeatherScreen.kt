package com.farmbirdfs.logjfeiowewg.presentation.screens.weather

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.farmbirdfs.logjfeiowewg.presentation.components.FarmTopBar
import com.farmbirdfs.logjfeiowewg.presentation.components.SectionHeader
import org.koin.androidx.compose.koinViewModel
import java.util.*
import kotlin.math.sin

data class WeatherData(
    val temperature: Float,
    val humidity: Float,
    val windSpeed: Float,
    val condition: String,
    val icon: ImageVector,
    val precipitationChance: Int,
    val uvIndex: Int
)

data class ForecastDay(val day: String, val high: Float, val low: Float, val condition: String, val icon: ImageVector)

class WeatherViewModel : ViewModel() {
    private val calendar = Calendar.getInstance()
    private val month = calendar.get(Calendar.MONTH)
    private val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
    private val seed = dayOfYear.toLong()

    val currentWeather: WeatherData by lazy {
        val baseTemp = when (month) {
            in 11..11, in 0..1 -> 2f
            in 2..4 -> 12f
            in 5..7 -> 24f
            in 8..10 -> 14f
            else -> 10f
        }
        val temp = baseTemp + (sin(dayOfYear * 0.1) * 4).toFloat() + (dayOfYear % 3 - 1f)
        val hum = 55f + (sin(dayOfYear * 0.2) * 20).toFloat()
        val wind = 8f + (dayOfYear % 7).toFloat()
        val precip = ((dayOfYear % 10) * 6)

        WeatherData(
            temperature = String.format("%.1f", temp).toFloat(),
            humidity = String.format("%.1f", hum).toFloat(),
            windSpeed = String.format("%.1f", wind).toFloat(),
            condition = if (precip > 40) "Cloudy with rain" else if (precip > 20) "Partly cloudy" else "Clear",
            icon = if (precip > 40) Icons.Filled.Thunderstorm else if (precip > 20) Icons.Filled.Cloud else Icons.Filled.WbSunny,
            precipitationChance = precip.coerceIn(0, 90),
            uvIndex = if (month in 5..7) 7 else if (month in 3..8) 4 else 2
        )
    }

    val forecast: List<ForecastDay> by lazy {
        val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val cal = Calendar.getInstance()
        val startDow = cal.get(Calendar.DAY_OF_WEEK)
        (1..7).map { i ->
            val d = dayOfYear + i
            val t = currentWeather.temperature + sin(d * 0.3).toFloat() * 3
            ForecastDay(
                day = dayNames[(startDow + i - 1) % 7],
                high = String.format("%.1f", t + 3).toFloat(),
                low = String.format("%.1f", t - 4).toFloat(),
                condition = if (d % 4 == 0) "Rain" else if (d % 3 == 0) "Cloudy" else "Clear",
                icon = if (d % 4 == 0) Icons.Filled.Thunderstorm else if (d % 3 == 0) Icons.Filled.Cloud else Icons.Filled.WbSunny
            )
        }
    }

    val transportAlert: String? by lazy {
        when {
            currentWeather.temperature < 5f -> "⚠️ Low temperature — extra bedding recommended during transport"
            currentWeather.temperature > 30f -> "⚠️ High temperature — ensure adequate ventilation in transport containers"
            currentWeather.precipitationChance > 70 -> "⚠️ High chance of rain — protect containers during loading"
            currentWeather.windSpeed > 15f -> "⚠️ Strong winds — secure all container hatches"
            else -> null
        }
    }
}

@Composable
fun WeatherScreen(
    navController: NavController,
    viewModel: WeatherViewModel = koinViewModel()
) {
    val weather = viewModel.currentWeather
    val forecast = viewModel.forecast
    val alert = viewModel.transportAlert

    Scaffold(
        topBar = { FarmTopBar(title = "Weather", onBack = { navController.popBackStack() }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(weather.icon, null, modifier = Modifier.size(72.dp), tint = MaterialTheme.colorScheme.primary)
                        Text(
                            "${weather.temperature}°C",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(weather.condition, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(0.8f))
                        Spacer(Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            WeatherMetric(Icons.Filled.WaterDrop, "${weather.humidity}%", "Humidity")
                            WeatherMetric(Icons.Filled.Air, "${weather.windSpeed} km/h", "Wind")
                            WeatherMetric(Icons.Filled.Umbrella, "${weather.precipitationChance}%", "Rain")
                            WeatherMetric(Icons.Filled.WbSunny, "UV ${weather.uvIndex}", "UV Index")
                        }
                    }
                }
            }

            alert?.let {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Warning, null, tint = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(10.dp))
                            Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
            }

            item { SectionHeader("7-Day Forecast") }

            item {
                Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        forecast.forEach { day ->
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text(day.day, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(44.dp), fontWeight = FontWeight.Medium)
                                Icon(day.icon, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.secondary)
                                Spacer(Modifier.width(8.dp))
                                Text(day.condition, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${day.high}° / ${day.low}°", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }

            item {
                SectionHeader("Transport Suitability")
                val score = when {
                    weather.temperature in 10f..25f && weather.humidity < 70f && weather.precipitationChance < 30 -> "Excellent"
                    weather.temperature in 5f..30f && weather.precipitationChance < 60 -> "Good"
                    else -> "Poor"
                }
                val (bgColor, fgColor) = when (score) {
                    "Excellent" -> Pair(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
                    "Good" -> Pair(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
                    else -> Pair(MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer)
                }
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = bgColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Filled.LocalShipping, null, tint = fgColor, modifier = Modifier.size(32.dp))
                        Column {
                            Text("Transport Conditions", style = MaterialTheme.typography.labelMedium, color = fgColor)
                            Text(score, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = fgColor)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeatherMetric(icon: ImageVector, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(2.dp))
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
