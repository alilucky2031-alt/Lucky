package com.example.protectioniq.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.protectioniq.engine.FinancialEngine
import com.example.protectioniq.model.PolicyAnalysisResult
import com.example.protectioniq.model.PolicyType
import com.example.protectioniq.model.ProtectionGapResult
import com.example.protectioniq.model.SensitivityItem
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BlueElectric
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseDanger
import com.example.ui.theme.TealAccent
import com.example.ui.theme.TealPrimary
import com.example.ui.theme.VioletCategory
import java.util.Locale
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

@Composable
fun ProtectionGauge(
    gapResult: ProtectionGapResult,
    modifier: Modifier = Modifier
) {
    val animatedProgress = remember { Animatable(0f) }
    val targetRatio = (gapResult.coverageRatioPct / 100.0).toFloat().coerceIn(0f, 1.5f)

    LaunchedEffect(targetRatio) {
        animatedProgress.animateTo(
            targetValue = targetRatio,
            animationSpec = tween(durationMillis = 800)
        )
    }

    val arcColor = when {
        gapResult.coverageRatioPct < 50.0 -> RoseDanger
        gapResult.coverageRatioPct < 90.0 -> AmberWarning
        else -> EmeraldSuccess
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .testTag("protection_gauge"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(200.dp, 160.dp)
                .padding(8.dp)
        ) {
            val strokeWidth = 16.dp.toPx()
            val arcSize = Size(size.width - strokeWidth, size.height * 1.5f - strokeWidth)
            val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

            // Background track: 180 degrees arc from 180 to 0
            drawArc(
                color = Color.LightGray.copy(alpha = 0.25f),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Active progress arc:
            val sweep = (animatedProgress.value * 180f).coerceIn(0f, 180f)
            drawArc(
                brush = Brush.horizontalGradient(
                    listOf(arcColor.copy(alpha = 0.8f), arcColor)
                ),
                startAngle = 180f,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        // Center content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 36.dp)
        ) {
            Text(
                text = "${String.format(Locale.US, "%.0f", gapResult.coverageRatioPct)}%",
                style = MaterialTheme.typography.displayMedium,
                color = arcColor,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (gapResult.isUnderInsured) "Under-protected" else "Adequately Covered",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Current: ${FinancialEngine.formatIndianCurrency(gapResult.existingCover)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "•",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Target: ${FinancialEngine.formatIndianCurrency(gapResult.recommendedCover)}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun StackedBreakdownBar(
    gapResult: ProtectionGapResult,
    modifier: Modifier = Modifier
) {
    val total = max(1.0, gapResult.grossProtectionNeed)
    val hlvFrac = (gapResult.hlvGross / total).toFloat().coerceIn(0f, 1f)
    val liabFrac = (gapResult.liabilities / total).toFloat().coerceIn(0f, 1f)
    val goalsFrac = (gapResult.goalsTotal / total).toFloat().coerceIn(0f, 1f)

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Total Protection Need Composition",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Gross Need: ${FinancialEngine.formatIndianCurrency(gapResult.grossProtectionNeed)} (minus ${FinancialEngine.formatIndianCurrency(gapResult.liquidAssetsOffset)} liquid savings)",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Multi-segment horizontal bar:
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(22.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.LightGray.copy(alpha = 0.2f))
        ) {
            if (hlvFrac > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(hlvFrac.coerceAtLeast(0.01f))
                        .background(TealPrimary)
                )
            }
            if (liabFrac > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(liabFrac.coerceAtLeast(0.01f))
                        .background(BlueElectric)
                )
            }
            if (goalsFrac > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(goalsFrac.coerceAtLeast(0.01f))
                        .background(VioletCategory)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Legend row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            BreakdownLegendItem(
                color = TealPrimary,
                label = "Income PV (HLV)",
                value = FinancialEngine.formatIndianCurrency(gapResult.hlvGross)
            )
            BreakdownLegendItem(
                color = BlueElectric,
                label = "Loans & Debt",
                value = FinancialEngine.formatIndianCurrency(gapResult.liabilities)
            )
            BreakdownLegendItem(
                color = VioletCategory,
                label = "Family Goals",
                value = FinancialEngine.formatIndianCurrency(gapResult.goalsTotal)
            )
        }
    }
}

@Composable
private fun BreakdownLegendItem(
    color: Color,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun CashFlowWealthChart(
    policies: List<PolicyAnalysisResult>,
    investmentReturnPct: Double,
    modifier: Modifier = Modifier
) {
    val pureTerm = policies.firstOrNull { it.product.type == PolicyType.PURE_TERM }
    val trop = policies.firstOrNull { it.product.type == PolicyType.TROP }
    val endowment = policies.firstOrNull { it.product.type == PolicyType.ENDOWMENT }

    val termYears = 30
    val returnRate = investmentReturnPct / 100.0

    // Compute trajectory for each over term:
    val btidPoints = remember(policies, investmentReturnPct) {
        val purePrem = pureTerm?.product?.annualPremium ?: 16_500.0
        val endPrem = endowment?.product?.annualPremium ?: 1_20_000.0
        val diff = max(0.0, endPrem - purePrem)
        val points = mutableListOf<Double>()
        var corpus = 0.0
        points.add(0.0)
        for (y in 1..termYears) {
            corpus = (corpus + diff) * (1.0 + returnRate)
            points.add(corpus)
        }
        points
    }

    val endowmentMaturity = endowment?.product?.maturityBenefit ?: 42_00_000.0
    val tropMaturity = trop?.product?.maturityBenefit ?: 11_85_000.0

    val maxVal = max(btidPoints.maxOrNull() ?: 1.0, endowmentMaturity * 1.2)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Wealth Creation Trajectory Over Time",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "30-Year Horizon",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                .padding(12.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Draw horizontal guide lines:
                val steps = 3
                for (s in 1..steps) {
                    val y = h * (s.toFloat() / steps.toFloat())
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.2f),
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // 1. Draw BTID Wealth curve (Teal gradient):
                val btidPath = Path()
                btidPoints.forEachIndexed { idx, value ->
                    val x = (idx.toFloat() / termYears.toFloat()) * w
                    val y = h - ((value / maxVal).toFloat() * h).coerceIn(0f, h)
                    if (idx == 0) btidPath.moveTo(x, y) else btidPath.lineTo(x, y)
                }

                drawPath(
                    path = btidPath,
                    color = TealPrimary,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // 2. Draw Endowment baseline step / slope (Amber):
                val endPath = Path()
                endPath.moveTo(0f, h)
                endPath.lineTo(w * 0.67f, h - ((endowmentMaturity * 0.4 / maxVal).toFloat() * h))
                endPath.lineTo(w, h - ((endowmentMaturity / maxVal).toFloat() * h))
                drawPath(
                    path = endPath,
                    color = AmberWarning,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )

                // 3. Draw TROP line (Rose/Blue):
                val tropPath = Path()
                tropPath.moveTo(0f, h)
                tropPath.lineTo(w, h - ((tropMaturity / maxVal).toFloat() * h))
                drawPath(
                    path = tropPath,
                    color = BlueElectric,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            LegendChip(
                color = TealPrimary,
                label = "BTID Corpus: ${FinancialEngine.formatIndianCurrency(btidPoints.last())}"
            )
            LegendChip(
                color = AmberWarning,
                label = "Endowment: ${FinancialEngine.formatIndianCurrency(endowmentMaturity)}"
            )
            LegendChip(
                color = BlueElectric,
                label = "TROP: ${FinancialEngine.formatIndianCurrency(tropMaturity)}"
            )
        }
    }
}

@Composable
private fun LegendChip(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun TornadoChart(
    sensitivities: List<SensitivityItem>,
    modifier: Modifier = Modifier
) {
    val maxSwing = max(1.0, sensitivities.maxOfOrNull { it.swingAmount } ?: 1.0)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        sensitivities.forEach { item ->
            val ratio = (item.swingAmount / maxSwing).toFloat().coerceIn(0.1f, 1f)

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "#${item.impactRanking}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Text(
                            text = item.parameterName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Text(
                        text = "±${FinancialEngine.formatIndianCurrency(item.swingAmount / 2.0)}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Centered tornado bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left swing (Decrease parameter)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(ratio * 0.9f)
                                .clip(RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp))
                                .background(RoseDanger.copy(alpha = 0.8f))
                        )
                    }

                    // Center axis divider
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(20.dp)
                            .background(MaterialTheme.colorScheme.onSurfaceVariant)
                    )

                    // Right swing (Increase parameter)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(ratio * 0.9f)
                                .clip(RoundedCornerShape(topEnd = 6.dp, bottomEnd = 6.dp))
                                .background(TealPrimary.copy(alpha = 0.85f))
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "-2% (${String.format(Locale.US, "%.1f", item.lowTestPct)}%)",
                        style = MaterialTheme.typography.labelSmall,
                        color = RoseDanger
                    )
                    Text(
                        text = "Base: ${String.format(Locale.US, "%.1f", item.baselinePct)}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "+2% (${String.format(Locale.US, "%.1f", item.highTestPct)}%)",
                        style = MaterialTheme.typography.labelSmall,
                        color = TealPrimary
                    )
                }
            }
        }
    }
}
