package com.example.protectioniq.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.protectioniq.engine.FinancialEngine
import com.example.protectioniq.model.PolicyAnalysisResult
import com.example.protectioniq.model.PolicyType
import com.example.protectioniq.model.ProtectionGapResult
import com.example.protectioniq.model.SensitivityItem
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BlueElectric
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseDanger
import com.example.ui.theme.TealPrimary

@Composable
fun KpiHeaderCards(
    gapResult: ProtectionGapResult,
    policies: List<PolicyAnalysisResult>,
    sensitivities: List<SensitivityItem>,
    modifier: Modifier = Modifier
) {
    val bestPolicy = policies.firstOrNull { it.product.type == PolicyType.PURE_TERM } ?: policies.firstOrNull()
    val topSensitivity = sensitivities.firstOrNull()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Card 1: Recommended Cover
            SingleKpiCard(
                modifier = Modifier
                    .weight(1f)
                    .testTag("kpi_card_recommended_cover"),
                icon = Icons.Default.Security,
                iconTint = TealPrimary,
                title = "Recommended Cover",
                value = FinancialEngine.formatIndianCurrency(gapResult.recommendedCover),
                subtitle = "${gapResult.workingYearsLeft} Yrs Earning PV"
            )

            // Card 2: Current Protection Gap
            SingleKpiCard(
                modifier = Modifier
                    .weight(1f)
                    .testTag("kpi_card_current_gap"),
                icon = if (gapResult.isUnderInsured) Icons.Default.Warning else Icons.Default.Security,
                iconTint = if (gapResult.isUnderInsured) RoseDanger else EmeraldSuccess,
                title = if (gapResult.isUnderInsured) "Current Gap" else "Status",
                value = if (gapResult.isUnderInsured) FinancialEngine.formatIndianCurrency(gapResult.netProtectionGap) else "Fully Covered",
                subtitle = if (gapResult.isUnderInsured) "Shortfall to Bridge" else "100%+ Safety Target"
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Card 3: Best Value Option
            SingleKpiCard(
                modifier = Modifier
                    .weight(1f)
                    .testTag("kpi_card_best_value"),
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                iconTint = BlueElectric,
                title = "Best-Value Option",
                value = "Pure Term",
                subtitle = bestPolicy?.let { "${it.coverPerRupee.toInt()}x Cover / ₹" } ?: "Highest leverage"
            )

            // Card 4: Most Sensitive Assumption
            SingleKpiCard(
                modifier = Modifier
                    .weight(1f)
                    .testTag("kpi_card_most_sensitive"),
                icon = Icons.Default.Assessment,
                iconTint = AmberWarning,
                title = "Top Sensitivity",
                value = topSensitivity?.parameterName ?: "Discount Rate",
                subtitle = topSensitivity?.let { "±${FinancialEngine.formatIndianCurrency(it.swingAmount / 2.0)}" } ?: "Key variable"
            )
        }
    }
}

@Composable
private fun SingleKpiCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconTint: Color,
    title: String,
    value: String,
    subtitle: String
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}
