package com.example.protectioniq.ui.modules

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.protectioniq.engine.FinancialEngine
import com.example.protectioniq.model.Assumptions
import com.example.protectioniq.model.FinancialGoal
import com.example.protectioniq.model.ProtectionGapResult
import com.example.protectioniq.model.UserProfile
import com.example.protectioniq.ui.components.ProtectionGauge
import com.example.protectioniq.ui.components.StackedBreakdownBar
import com.example.ui.theme.BlueElectric
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseDanger
import com.example.ui.theme.TealPrimary
import java.util.Locale

@Composable
fun Module1GapCalculatorView(
    profile: UserProfile,
    assumptions: Assumptions,
    gapResult: ProtectionGapResult,
    showFormulaExpander: Boolean,
    onToggleFormulaExpander: () -> Unit,
    onUpdateProfile: (UserProfile) -> Unit,
    onAddGoal: (String, Double, Int) -> Unit,
    onRemoveGoal: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isAddingGoalDialog by remember { mutableStateOf(false) }
    var newGoalName by remember { mutableStateOf("Child Higher Education") }
    var newGoalAmountStr by remember { mutableStateOf("2500000") }
    var newGoalYearsStr by remember { mutableStateOf("12") }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Module 1: Protection Gap Calculator",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Calculate your family's exact financial security need using the actuarial Human Life Value (HLV) method.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }

        // Live Diagnosis Card with Gauge
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("protection_gap_card"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Life Insurance Coverage Diagnosis",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                ProtectionGauge(gapResult = gapResult)

                Spacer(modifier = Modifier.height(12.dp))

                // Diagnostic Banner
                val bannerBg = if (gapResult.isUnderInsured) RoseDanger.copy(alpha = 0.12f) else EmeraldSuccess.copy(alpha = 0.12f)
                val bannerColor = if (gapResult.isUnderInsured) RoseDanger else EmeraldSuccess
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(bannerBg)
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = if (gapResult.isUnderInsured) "Underinsurance Gap: ${FinancialEngine.formatIndianCurrency(gapResult.netProtectionGap)}" else "Cover Meets Recommendation",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = bannerColor
                        )
                        Text(
                            text = if (gapResult.isUnderInsured) {
                                "Your family requires ${FinancialEngine.formatIndianCurrency(gapResult.recommendedCover)} total cover, but you currently hold ${FinancialEngine.formatIndianCurrency(gapResult.existingCover)}. Bridge this ${FinancialEngine.formatIndianCurrency(gapResult.netProtectionGap)} gap with pure term insurance."
                            } else {
                                "Your existing cover of ${FinancialEngine.formatIndianCurrency(gapResult.existingCover)} safely covers your family's Human Life Value, debt and upcoming milestones."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                // Stacked Breakdown of need
                StackedBreakdownBar(gapResult = gapResult)

                Spacer(modifier = Modifier.height(12.dp))

                // Formula Expander Button
                OutlinedButton(
                    onClick = onToggleFormulaExpander,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("toggle_formula_button"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = if (showFormulaExpander) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = if (showFormulaExpander) "Hide Formula & Assumptions" else "Show Formula & Assumptions"
                    )
                }

                AnimatedVisibility(
                    visible = showFormulaExpander,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Functions,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Actuarial Human Life Value (HLV) Formula",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "HLV = \\sum_{t=1}^{N} \\frac{(Income_t - PersonalExpenses_t)}{(1 + r)^t}",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "• Working Years (N): ${gapResult.workingYearsLeft} yrs (${profile.age} to ${profile.retirementAge})\n" +
                                    "• Annual Income: ${FinancialEngine.formatIndianCurrency(profile.annualIncome)} (growing at ${profile.expectedIncomeGrowthPct}%)\n" +
                                    "• Personal Expenses Deducted: ${profile.personalExpenseRatioPct}% (${FinancialEngine.formatIndianCurrency(profile.annualIncome * profile.personalExpenseRatioPct / 100.0)})\n" +
                                    "• Discount Rate (r): ${assumptions.discountRatePct}% p.a.\n" +
                                    "• Total HLV PV: ${FinancialEngine.formatIndianCurrency(gapResult.hlvGross)}\n" +
                                    "• Recommended Cover = HLV (${FinancialEngine.formatIndianCurrency(gapResult.hlvGross)}) + Debt (${FinancialEngine.formatIndianCurrency(gapResult.liabilities)}) + Goals (${FinancialEngine.formatIndianCurrency(gapResult.goalsTotal)}) - Savings (${FinancialEngine.formatIndianCurrency(gapResult.liquidAssetsOffset)}) = ${FinancialEngine.formatIndianCurrency(gapResult.recommendedCover)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // Profile Inputs Card
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
                Text(
                    text = "Personal & Financial Profile",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // Age and Retirement Age
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Current Age: ${profile.age} yrs",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Slider(
                            value = profile.age.toFloat(),
                            onValueChange = { onUpdateProfile(profile.copy(age = it.toInt())) },
                            valueRange = 21f..65f,
                            modifier = Modifier.testTag("slider_age")
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Retirement: ${profile.retirementAge} yrs",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Slider(
                            value = profile.retirementAge.toFloat(),
                            onValueChange = { onUpdateProfile(profile.copy(retirementAge = it.toInt())) },
                            valueRange = 50f..75f,
                            modifier = Modifier.testTag("slider_retirement_age")
                        )
                    }
                }

                // Annual Income
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Annual Income",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = FinancialEngine.formatIndianCurrency(profile.annualIncome),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = (profile.annualIncome / 1_00_000.0).toFloat(),
                        onValueChange = { onUpdateProfile(profile.copy(annualIncome = it.toDouble() * 1_00_000.0)) },
                        valueRange = 3f..100f,
                        modifier = Modifier.testTag("slider_income")
                    )
                }

                // Expected Income Growth %
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Expected Income Growth: ${String.format(Locale.US, "%.1f", profile.expectedIncomeGrowthPct)}%",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Slider(
                            value = profile.expectedIncomeGrowthPct.toFloat(),
                            onValueChange = { onUpdateProfile(profile.copy(expectedIncomeGrowthPct = it.toDouble())) },
                            valueRange = 3f..20f,
                            modifier = Modifier.testTag("slider_income_growth")
                        )
                    }
                }

                // Personal Expense Ratio
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Personal Expense Share (deducted from HLV)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${String.format(Locale.US, "%.0f", profile.personalExpenseRatioPct)}%",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Slider(
                        value = profile.personalExpenseRatioPct.toFloat(),
                        onValueChange = { onUpdateProfile(profile.copy(personalExpenseRatioPct = it.toDouble())) },
                        valueRange = 15f..50f,
                        modifier = Modifier.testTag("slider_personal_expense_ratio")
                    )
                }

                // Dependents
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Number of Dependents: ${profile.dependents}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { if (profile.dependents > 0) onUpdateProfile(profile.copy(dependents = profile.dependents - 1)) },
                            shape = RoundedCornerShape(8.dp)
                        ) { Text("-") }
                        OutlinedButton(
                            onClick = { onUpdateProfile(profile.copy(dependents = profile.dependents + 1)) },
                            shape = RoundedCornerShape(8.dp)
                        ) { Text("+") }
                    }
                }

                HorizontalDivider()

                // Liabilities & Loans
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Outstanding Loans (Home / Car / Personal Debt)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = FinancialEngine.formatIndianCurrency(profile.outstandingLoans),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = RoseDanger
                        )
                    }
                    Slider(
                        value = (profile.outstandingLoans / 1_00_000.0).toFloat(),
                        onValueChange = { onUpdateProfile(profile.copy(outstandingLoans = it.toDouble() * 1_00_000.0)) },
                        valueRange = 0f..200f,
                        modifier = Modifier.testTag("slider_loans")
                    )
                }

                // Existing Life Cover
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Existing Life Cover Held",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = FinancialEngine.formatIndianCurrency(profile.existingLifeCover),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = BlueElectric
                        )
                    }
                    Slider(
                        value = (profile.existingLifeCover / 1_00_000.0).toFloat(),
                        onValueChange = { onUpdateProfile(profile.copy(existingLifeCover = it.toDouble() * 1_00_000.0)) },
                        valueRange = 0f..300f,
                        modifier = Modifier.testTag("slider_existing_cover")
                    )
                }

                // Existing Liquid Savings/Investments
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Existing Liquid Savings / Mutual Funds",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = FinancialEngine.formatIndianCurrency(profile.existingLiquidSavings),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldSuccess
                        )
                    }
                    Slider(
                        value = (profile.existingLiquidSavings / 1_00_000.0).toFloat(),
                        onValueChange = { onUpdateProfile(profile.copy(existingLiquidSavings = it.toDouble() * 1_00_000.0)) },
                        valueRange = 0f..100f,
                        modifier = Modifier.testTag("slider_existing_savings")
                    )
                }
            }
        }

        // Financial Goals Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Future Financial Goals",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = { isAddingGoalDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("add_goal_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.size(4.dp))
                        Text("Add Goal")
                    }
                }

                profile.goals.forEach { goal ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = goal.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${FinancialEngine.formatIndianCurrency(goal.targetAmount)} in ${goal.yearsAway} years",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { onRemoveGoal(goal.id) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Goal",
                                tint = RoseDanger
                            )
                        }
                    }
                }
            }
        }
    }

    if (isAddingGoalDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { isAddingGoalDialog = false }) {
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
                        text = "Add Family Milestone Goal",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = newGoalName,
                        onValueChange = { newGoalName = it },
                        label = { Text("Goal Name (e.g. Child Education)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newGoalAmountStr,
                        onValueChange = { newGoalAmountStr = it },
                        label = { Text("Target Amount (₹)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newGoalYearsStr,
                        onValueChange = { newGoalYearsStr = it },
                        label = { Text("Years Away") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(onClick = { isAddingGoalDialog = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.size(8.dp))
                        Button(
                            onClick = {
                                val amount = newGoalAmountStr.toDoubleOrNull() ?: 20_00_000.0
                                val years = newGoalYearsStr.toIntOrNull() ?: 10
                                onAddGoal(newGoalName, amount, years)
                                isAddingGoalDialog = false
                            }
                        ) {
                            Text("Add")
                        }
                    }
                }
            }
        }
    }
}
