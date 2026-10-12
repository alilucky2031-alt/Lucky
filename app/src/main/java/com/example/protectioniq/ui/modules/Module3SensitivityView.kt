package com.example.protectioniq.ui.modules

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.protectioniq.engine.FinancialEngine
import com.example.protectioniq.model.AlertItem
import com.example.protectioniq.model.AlertSeverity
import com.example.protectioniq.model.Assumptions
import com.example.protectioniq.model.ProtectionGapResult
import com.example.protectioniq.model.ScenarioPreset
import com.example.protectioniq.model.SensitivityItem
import com.example.protectioniq.ui.components.TornadoChart
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseDanger
import com.example.ui.theme.TealPrimary
import java.util.Locale

@Composable
fun Module3SensitivityView(
    assumptions: Assumptions,
    activePreset: ScenarioPreset,
    gapResult: ProtectionGapResult,
    sensitivities: List<SensitivityItem>,
    alerts: List<AlertItem>,
    onUpdateAssumptions: (Assumptions) -> Unit,
    onApplyPreset: (ScenarioPreset) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Module Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Module 3: Scenario & Sensitivity Engine",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Stress-test your insurance plan against economic shifts. Adjust variables dynamically or apply macro scenarios.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }

        // Preset Scenarios Selector
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Macro Preset Scenarios",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ScenarioPreset.values().forEach { preset ->
                        val isSelected = activePreset == preset
                        if (isSelected) {
                            Button(
                                onClick = { onApplyPreset(preset) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("preset_${preset.name.lowercase()}"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                            ) {
                                Text(preset.label, fontSize = 12.sp, maxLines = 1)
                            }
                        } else {
                            OutlinedButton(
                                onClick = { onApplyPreset(preset) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("preset_${preset.name.lowercase()}"),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(preset.label, fontSize = 12.sp, maxLines = 1)
                            }
                        }
                    }
                }

                Text(
                    text = activePreset.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Live Assumption Sliders
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        text = "Live Assumption Sliders (Illustrative, Not Advice)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // 1. Inflation Rate
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Retail Inflation Rate", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            "${String.format(Locale.US, "%.1f", assumptions.inflationRatePct)}% p.a.",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = assumptions.inflationRatePct.toFloat(),
                        onValueChange = { onUpdateAssumptions(assumptions.copy(inflationRatePct = it.toDouble())) },
                        valueRange = 4.0f..10.0f,
                        modifier = Modifier.testTag("slider_inflation")
                    )
                }

                // 2. Discount Rate
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Discount Rate (Cost of Capital)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            "${String.format(Locale.US, "%.1f", assumptions.discountRatePct)}% p.a.",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = assumptions.discountRatePct.toFloat(),
                        onValueChange = { onUpdateAssumptions(assumptions.copy(discountRatePct = it.toDouble())) },
                        valueRange = 5.0f..12.0f,
                        modifier = Modifier.testTag("slider_discount_rate")
                    )
                }

                // 3. Expected Income Growth %
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Income Growth Rate", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            "${String.format(Locale.US, "%.1f", assumptions.incomeGrowthPct)}% p.a.",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = assumptions.incomeGrowthPct.toFloat(),
                        onValueChange = { onUpdateAssumptions(assumptions.copy(incomeGrowthPct = it.toDouble())) },
                        valueRange = 4.0f..15.0f,
                        modifier = Modifier.testTag("slider_income_growth_rate")
                    )
                }

                // 4. Investment Return %
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Expected Investment Return (Equity/Hybrid)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            "${String.format(Locale.US, "%.1f", assumptions.investmentReturnPct)}% p.a.",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = assumptions.investmentReturnPct.toFloat(),
                        onValueChange = { onUpdateAssumptions(assumptions.copy(investmentReturnPct = it.toDouble())) },
                        valueRange = 6.0f..16.0f,
                        modifier = Modifier.testTag("slider_investment_return")
                    )
                }
            }
        }

        // Tornado Chart Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("tornado_chart_card"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Tornado Chart: Assumption Sensitivity Ranking",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Measures which parameter shifts your recommended cover the most when varied by ±2.0%.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                TornadoChart(sensitivities = sensitivities)
            }
        }

        // Variance Alerts Panel
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("alerts_panel"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CrisisAlert,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Variance Alerts & Risk Flags",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (alerts.isEmpty()) {
                    Text(
                        text = "No severe variances detected. Parameters are balanced.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    alerts.forEach { alert ->
                        val (bgColor, iconColor, icon) = when (alert.severity) {
                            AlertSeverity.DANGER -> Triple(RoseDanger.copy(alpha = 0.12f), RoseDanger, Icons.Default.Warning)
                            AlertSeverity.WARNING -> Triple(AmberWarning.copy(alpha = 0.12f), AmberWarning, Icons.Default.Warning)
                            AlertSeverity.INFO -> Triple(EmeraldSuccess.copy(alpha = 0.12f), EmeraldSuccess, Icons.Default.Info)
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(bgColor)
                                .padding(12.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = iconColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = alert.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = iconColor
                                        )
                                        alert.metricDelta?.let { delta ->
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(iconColor.copy(alpha = 0.2f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = delta,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = iconColor
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = alert.message,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
