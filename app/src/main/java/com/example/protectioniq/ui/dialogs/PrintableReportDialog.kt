package com.example.protectioniq.ui.dialogs

import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.protectioniq.engine.FinancialEngine
import com.example.protectioniq.model.AiExplanation
import com.example.protectioniq.model.Assumptions
import com.example.protectioniq.model.PolicyAnalysisResult
import com.example.protectioniq.model.PolicyType
import com.example.protectioniq.model.ProtectionGapResult
import com.example.protectioniq.model.UserProfile
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseDanger
import com.example.ui.theme.TealPrimary
import java.util.Locale

@Composable
fun PrintableReportDialog(
    profile: UserProfile,
    gapResult: ProtectionGapResult,
    policies: List<PolicyAnalysisResult>,
    assumptions: Assumptions,
    explanation: AiExplanation?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    val reportText = buildReportText(profile, gapResult, policies, assumptions, explanation)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header with actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = TealPrimary)
                        Text(
                            text = "ProtectionIQ Executive Report",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Privacy Badge
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(TealPrimary.copy(alpha = 0.1f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(14.dp))
                        Text(
                            text = "Privacy Note: Data stays in your browser session and is not stored.",
                            style = MaterialTheme.typography.labelSmall,
                            color = TealPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Summary Paper Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "1. Financial Profile & Protection Need",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "• Age: ${profile.age} | Retirement Age: ${profile.retirementAge} (${gapResult.workingYearsLeft} working years)\n" +
                                    "• Annual Income: ${FinancialEngine.formatIndianCurrency(profile.annualIncome)} (Growth: ${profile.expectedIncomeGrowthPct}%)\n" +
                                    "• Calculated Human Life Value (HLV): ${FinancialEngine.formatIndianCurrency(gapResult.hlvGross)}\n" +
                                    "• Total Liabilities & Debt: ${FinancialEngine.formatIndianCurrency(gapResult.liabilities)}\n" +
                                    "• Upcoming Family Goals: ${FinancialEngine.formatIndianCurrency(gapResult.goalsTotal)}\n" +
                                    "• Liquid Savings Offset: -${FinancialEngine.formatIndianCurrency(gapResult.liquidAssetsOffset)}\n" +
                                    "• Recommended Total Cover: ${FinancialEngine.formatIndianCurrency(gapResult.recommendedCover)}\n" +
                                    "• Existing Cover: ${FinancialEngine.formatIndianCurrency(gapResult.existingCover)}\n" +
                                    "• Net Protection Gap: ${FinancialEngine.formatIndianCurrency(gapResult.netProtectionGap)} (${String.format(Locale.US, "%.0f", gapResult.coverageRatioPct)}% covered)",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 18.sp
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        Text(
                            text = "2. Policy Comparison & BTID Assessment",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        policies.forEach { res ->
                            Text(
                                text = "• ${res.product.name} (${res.product.type.displayName}):\n" +
                                        "  Premium: ${FinancialEngine.formatIndianCurrency(res.product.annualPremium)}/yr | Cover: ${FinancialEngine.formatIndianCurrency(res.product.sumAssured)} | Maturity: ${FinancialEngine.formatIndianCurrency(res.product.maturityBenefit)}\n" +
                                        "  Leverage: ${res.coverPerRupee.toInt()}x cover per ₹1 | IRR: ${res.irrPct?.let { "${String.format(Locale.US, "%.1f", it)}%" } ?: "N/A"}",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 18.sp
                            )
                        }

                        val endowment = policies.firstOrNull { it.product.type == PolicyType.ENDOWMENT }
                        if (endowment?.btidCorpus != null) {
                            Text(
                                text = "• Buy Term + Invest Difference (BTID) Result:\n" +
                                        "  Investing the premium difference at ${assumptions.investmentReturnPct}% creates ${FinancialEngine.formatIndianCurrency(endowment.btidCorpus)} in wealth vs ${FinancialEngine.formatIndianCurrency(endowment.product.maturityBenefit)} in endowment maturity (+${FinancialEngine.formatIndianCurrency(endowment.btidCorpus - endowment.product.maturityBenefit)} extra).",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = EmeraldSuccess,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (explanation != null) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            Text(
                                text = "3. AI Executive Verdict",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = explanation.headline,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            explanation.keyInsights.forEach {
                                Text("✓ $it", style = MaterialTheme.typography.bodySmall)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Immediate Action: ${explanation.suggestedNextStep}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = TealPrimary
                            )
                        }
                    }
                }

                // Action Buttons: Share / Print
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Close")
                    }

                    Button(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "ProtectionIQ Financial Decision Report")
                                putExtra(Intent.EXTRA_TEXT, reportText)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share or Save ProtectionIQ Report"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("share_report_button")
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.size(6.dp))
                        Text("Share / Save")
                    }
                }
            }
        }
    }
}

private fun buildReportText(
    profile: UserProfile,
    gapResult: ProtectionGapResult,
    policies: List<PolicyAnalysisResult>,
    assumptions: Assumptions,
    explanation: AiExplanation?
): String {
    val sb = StringBuilder()
    sb.appendLine("==========================================")
    sb.appendLine(" PROTECTIONIQ EXECUTIVE FINANCIAL REPORT ")
    sb.appendLine("==========================================")
    sb.appendLine()
    sb.appendLine("1. DIAGNOSIS & PROTECTION NEED")
    sb.appendLine("• Current Age: ${profile.age} | Retirement Age: ${profile.retirementAge}")
    sb.appendLine("• Annual Income: ${FinancialEngine.formatIndianCurrency(profile.annualIncome)}")
    sb.appendLine("• Human Life Value (HLV): ${FinancialEngine.formatIndianCurrency(gapResult.hlvGross)}")
    sb.appendLine("• Debt & Liabilities: ${FinancialEngine.formatIndianCurrency(gapResult.liabilities)}")
    sb.appendLine("• Future Milestone Goals: ${FinancialEngine.formatIndianCurrency(gapResult.goalsTotal)}")
    sb.appendLine("• Liquid Savings Offset: -${FinancialEngine.formatIndianCurrency(gapResult.liquidAssetsOffset)}")
    sb.appendLine("------------------------------------------")
    sb.appendLine("• RECOMMENDED COVER: ${FinancialEngine.formatIndianCurrency(gapResult.recommendedCover)}")
    sb.appendLine("• CURRENT COVER HELD: ${FinancialEngine.formatIndianCurrency(gapResult.existingCover)}")
    sb.appendLine("• NET PROTECTION GAP: ${FinancialEngine.formatIndianCurrency(gapResult.netProtectionGap)} (${String.format(Locale.US, "%.0f", gapResult.coverageRatioPct)}% covered)")
    sb.appendLine()
    sb.appendLine("2. POLICY VALUE MATRIX")
    policies.forEach { p ->
        sb.appendLine("• ${p.product.name} [${p.product.type.displayName}]:")
        sb.appendLine("  Premium: ${FinancialEngine.formatIndianCurrency(p.product.annualPremium)}/yr | Cover: ${FinancialEngine.formatIndianCurrency(p.product.sumAssured)}")
        sb.appendLine("  Maturity: ${FinancialEngine.formatIndianCurrency(p.product.maturityBenefit)} | IRR: ${p.irrPct?.let { "${String.format(Locale.US, "%.1f", it)}%" } ?: "N/A"}")
        sb.appendLine("  Cover per Rupee: ${p.coverPerRupee.toInt()}x")
    }
    sb.appendLine()
    sb.appendLine("3. ASSUMPTIONS")
    sb.appendLine("• Inflation: ${assumptions.inflationRatePct}% | Discount Rate: ${assumptions.discountRatePct}%")
    sb.appendLine("• Salary Growth: ${assumptions.incomeGrowthPct}% | Investment Return: ${assumptions.investmentReturnPct}%")
    sb.appendLine()
    if (explanation != null) {
        sb.appendLine("4. TAKEAWAYS & NEXT STEP")
        sb.appendLine("• ${explanation.headline}")
        explanation.keyInsights.forEach { sb.appendLine("  - $it") }
        sb.appendLine("• Next Step: ${explanation.suggestedNextStep}")
        sb.appendLine()
    }
    sb.appendLine("------------------------------------------")
    sb.appendLine("Disclaimer: Illustrative financial decision tool only. Data stays in your browser session and is not stored.")
    return sb.toString()
}
