package com.example.protectioniq.ui.modules

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.protectioniq.engine.FinancialEngine
import com.example.protectioniq.model.Assumptions
import com.example.protectioniq.model.PolicyAnalysisResult
import com.example.protectioniq.model.PolicyProduct
import com.example.protectioniq.model.PolicyType
import com.example.protectioniq.ui.components.CashFlowWealthChart
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BlueElectric
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseDanger
import com.example.ui.theme.TealPrimary
import java.util.Locale

@Composable
fun Module2PolicyAnalyzerView(
    policies: List<PolicyAnalysisResult>,
    assumptions: Assumptions,
    onUpdatePolicy: (String, Double, Int, Double, Double) -> Unit,
    onLoadSamplePolicies: () -> Unit,
    onOpenPolicyExtractor: () -> Unit,
    modifier: Modifier = Modifier
) {
    var editingPolicyId by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Module Header with Quick Actions
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Module 2: Policy Value Analyzer",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Evaluate if a policy is genuine protection or an inefficient savings scheme. Calculate true IRR, NPV, cover leverage, and 'Buy Term + Invest Difference'.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onLoadSamplePolicies,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("load_sample_products_button")
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.size(4.dp))
                        Text("Reset Samples", fontSize = 12.sp)
                    }

                    Button(
                        onClick = onOpenPolicyExtractor,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("open_policy_extractor_button")
                    ) {
                        Icon(imageVector = Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.size(4.dp))
                        Text("Paste / Scan Quote", fontSize = 12.sp)
                    }
                }
            }
        }

        // Comparison Table Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("policy_comparison_table"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Product Comparison Matrix",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Side-by-side financial metrics (Scroll horizontally to view all columns)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Table
                Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    Column {
                        // Table Header
                        Row(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text("Product Type", modifier = Modifier.size(140.dp, 24.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("Annual Premium", modifier = Modifier.size(100.dp, 24.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("Term", modifier = Modifier.size(60.dp, 24.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("Sum Assured", modifier = Modifier.size(90.dp, 24.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("Maturity Return", modifier = Modifier.size(100.dp, 24.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("Real IRR %", modifier = Modifier.size(80.dp, 24.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("NPV", modifier = Modifier.size(90.dp, 24.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("Cover / ₹1", modifier = Modifier.size(80.dp, 24.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("Actions", modifier = Modifier.size(70.dp, 24.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        // Table Rows
                        policies.forEach { res ->
                            val isBest = res.product.type == PolicyType.PURE_TERM
                            val rowBg = if (isBest) TealPrimary.copy(alpha = 0.08f) else Color.Transparent

                            Row(
                                modifier = Modifier
                                    .background(rowBg)
                                    .padding(vertical = 10.dp, horizontal = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Product Name & Badge
                                Column(modifier = Modifier.size(140.dp, 44.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (isBest) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = null,
                                                tint = TealPrimary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.size(2.dp))
                                        }
                                        Text(
                                            text = res.product.type.displayName,
                                            fontWeight = if (isBest) FontWeight.Bold else FontWeight.SemiBold,
                                            fontSize = 12.sp,
                                            color = if (isBest) TealPrimary else MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1
                                        )
                                    }
                                    Text(
                                        text = res.product.name,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }

                                // Annual Premium
                                Text(
                                    text = FinancialEngine.formatIndianCurrency(res.product.annualPremium),
                                    modifier = Modifier.size(100.dp, 30.dp),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                )

                                // Term
                                Text(
                                    text = "${res.product.termYears} yrs",
                                    modifier = Modifier.size(60.dp, 30.dp),
                                    fontSize = 12.sp
                                )

                                // Sum Assured
                                Text(
                                    text = FinancialEngine.formatIndianCurrency(res.product.sumAssured),
                                    modifier = Modifier.size(90.dp, 30.dp),
                                    fontWeight = FontWeight.Bold,
                                    color = if (isBest) TealPrimary else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 12.sp
                                )

                                // Maturity Return
                                Text(
                                    text = if (res.product.maturityBenefit > 0) FinancialEngine.formatIndianCurrency(res.product.maturityBenefit) else "₹0 (Pure Cover)",
                                    modifier = Modifier.size(100.dp, 30.dp),
                                    fontSize = 12.sp
                                )

                                // IRR
                                Text(
                                    text = res.irrPct?.let { "${String.format(Locale.US, "%.1f", it)}%" } ?: "N/A (Cost)",
                                    modifier = Modifier.size(80.dp, 30.dp),
                                    fontWeight = FontWeight.Bold,
                                    color = if (res.irrPct != null && res.irrPct < assumptions.inflationRatePct) RoseDanger else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 12.sp
                                )

                                // NPV
                                Text(
                                    text = FinancialEngine.formatIndianCurrency(res.npv),
                                    modifier = Modifier.size(90.dp, 30.dp),
                                    fontSize = 12.sp
                                )

                                // Cover / Rupee
                                Text(
                                    text = "${res.coverPerRupee.toInt()}x",
                                    modifier = Modifier.size(80.dp, 30.dp),
                                    fontWeight = FontWeight.Bold,
                                    color = if (isBest) TealPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )

                                // Edit Button
                                OutlinedButton(
                                    onClick = { editingPolicyId = res.product.id },
                                    modifier = Modifier.size(70.dp, 32.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(2.dp),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("Edit", fontSize = 11.sp)
                                }
                            }
                            HorizontalDivider()
                        }
                    }
                }
            }
        }

        // BTID (Buy Term + Invest Difference) Strategy Card
        val endowmentRes = policies.firstOrNull { it.product.type == PolicyType.ENDOWMENT }
        if (endowmentRes != null && endowmentRes.btidCorpus != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = TealPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Strategy: Buy Term + Invest the Difference (BTID)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Instead of committing ₹${FinancialEngine.formatIndianGrouping(endowmentRes.product.annualPremium.toLong())}/yr to an endowment plan for just ${FinancialEngine.formatIndianCurrency(endowmentRes.product.sumAssured)} cover, buy Pure Term (₹1 Cr cover for ~₹16,500) and invest the remaining difference each year at ${assumptions.investmentReturnPct}% return.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(AmberWarning.copy(alpha = 0.12f))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text("Endowment Maturity", style = MaterialTheme.typography.labelSmall, color = AmberWarning)
                                Text(
                                    FinancialEngine.formatIndianCurrency(endowmentRes.product.maturityBenefit),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text("With low ₹25L cover", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(TealPrimary.copy(alpha = 0.15f))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text("BTID Corpus Wealth", style = MaterialTheme.typography.labelSmall, color = TealPrimary)
                                Text(
                                    FinancialEngine.formatIndianCurrency(endowmentRes.btidCorpus),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TealPrimary
                                )
                                Text("Plus huge ₹1 Cr cover", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    val extraGain = (endowmentRes.btidCorpus - endowmentRes.product.maturityBenefit)
                    Text(
                        text = "Net Gain from BTID: +${FinancialEngine.formatIndianCurrency(extraGain)} extra wealth for your family while enjoying 4x higher life cover during your earning years!",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = EmeraldSuccess
                    )
                }
            }
        }

        // Cash Flow & Wealth Line Chart
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                CashFlowWealthChart(
                    policies = policies,
                    investmentReturnPct = assumptions.investmentReturnPct
                )
            }
        }

        // Plain Verdict Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Mathematical Verdict & Advisory Takeaway",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "1. Pure Term is mathematically superior for protection: It provides 500x to 800x coverage per rupee spent, freeing up capital to invest in compounding assets.\n" +
                            "2. Return of Premium (TROP) is a psychological trap: You pay 2.5x higher premium only to get back non-inflation-adjusted nominal rupees 30 years later (delivering an effective IRR of < 3.5%).\n" +
                            "3. Traditional Endowment plans mix insurance with low returns: The typical IRR of 5-6% barely keeps pace with medical and lifestyle inflation in India. Always keep life insurance and investments separate.",
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = 19.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    // Edit Policy Dialog
    editingPolicyId?.let { id ->
        val targetPolicy = policies.firstOrNull { it.product.id == id }?.product
        if (targetPolicy != null) {
            var premStr by remember { mutableStateOf(targetPolicy.annualPremium.toLong().toString()) }
            var termStr by remember { mutableStateOf(targetPolicy.termYears.toString()) }
            var saStr by remember { mutableStateOf(targetPolicy.sumAssured.toLong().toString()) }
            var matStr by remember { mutableStateOf(targetPolicy.maturityBenefit.toLong().toString()) }

            androidx.compose.ui.window.Dialog(onDismissRequest = { editingPolicyId = null }) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Edit ${targetPolicy.name}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        OutlinedTextField(
                            value = premStr,
                            onValueChange = { premStr = it },
                            label = { Text("Annual Premium (₹)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = termStr,
                            onValueChange = { termStr = it },
                            label = { Text("Policy Term (Years)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = saStr,
                            onValueChange = { saStr = it },
                            label = { Text("Sum Assured (Life Cover ₹)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = matStr,
                            onValueChange = { matStr = it },
                            label = { Text("Maturity Benefit (₹)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(onClick = { editingPolicyId = null }) {
                                Text("Cancel")
                            }
                            Spacer(modifier = Modifier.size(8.dp))
                            Button(
                                onClick = {
                                    val prem = premStr.toDoubleOrNull() ?: targetPolicy.annualPremium
                                    val term = termStr.toIntOrNull() ?: targetPolicy.termYears
                                    val sa = saStr.toDoubleOrNull() ?: targetPolicy.sumAssured
                                    val mat = matStr.toDoubleOrNull() ?: targetPolicy.maturityBenefit
                                    onUpdatePolicy(id, prem, term, sa, mat)
                                    editingPolicyId = null
                                }
                            ) {
                                Text("Save")
                            }
                        }
                    }
                }
            }
        }
    }
}
