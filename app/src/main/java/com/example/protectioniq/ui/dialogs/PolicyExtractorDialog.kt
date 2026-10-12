package com.example.protectioniq.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.protectioniq.engine.FinancialEngine
import com.example.protectioniq.model.PolicyType
import com.example.protectioniq.service.ProtectionAiService
import com.example.ui.theme.TealPrimary

@Composable
fun PolicyExtractorDialog(
    onDismiss: () -> Unit,
    onApplyPolicy: (PolicyType, String) -> Unit
) {
    var rawText by remember { mutableStateOf("") }
    var selectedTargetType by remember { mutableStateOf(PolicyType.ENDOWMENT) }

    val sampleQuotes = listOf(
        "LIC Jeevan Labh: Policy Term 25 years. Sum Assured: ₹25,00,000. Annual Premium: ₹1,15,400. Guaranteed Maturity Benefit: ₹45,50,000 at age 55.",
        "HDFC Life Click 2 Protect: Term 35 years. Life Cover Sum Assured: ₹1.5 Crore. Annual Premium: ₹18,450. Maturity Benefit: ₹0 (Pure Term).",
        "ICICI Pru iProtect TROP: Policy Term 30 yrs. Sum Assured: ₹1 Crore. Annual Premium: ₹42,500. 100% Return of Premium Maturity: ₹12,75,000."
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Title & Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Description, contentDescription = null, tint = TealPrimary)
                        Text(
                            text = "Extract Policy Quotation",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Text(
                    text = "Paste text from your insurance proposal, email quote, or policy PDF notice to automatically extract premium, term, and sum assured.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Quick Sample Fill Buttons
                Text(
                    text = "Or load sample quotation text:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = {
                            rawText = sampleQuotes[0]
                            selectedTargetType = PolicyType.ENDOWMENT
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp)
                    ) {
                        Text("Endowment", fontSize = 11.sp, maxLines = 1)
                    }

                    Button(
                        onClick = {
                            rawText = sampleQuotes[1]
                            selectedTargetType = PolicyType.PURE_TERM
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp)
                    ) {
                        Text("Pure Term", fontSize = 11.sp, maxLines = 1)
                    }

                    Button(
                        onClick = {
                            rawText = sampleQuotes[2]
                            selectedTargetType = PolicyType.TROP
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp)
                    ) {
                        Text("TROP", fontSize = 11.sp, maxLines = 1)
                    }
                }

                // Text Input Area
                OutlinedTextField(
                    value = rawText,
                    onValueChange = { rawText = it },
                    label = { Text("Paste policy quote text here") },
                    minLines = 4,
                    maxLines = 6,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("policy_text_input")
                )

                // Live Preview of Extracted Data
                if (rawText.isNotBlank()) {
                    val preview = ProtectionAiService.extractPolicyFromText(rawText)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.size(4.dp))
                                Text("Parsed Key Figures", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Text("• Premium: ${FinancialEngine.formatIndianCurrency(preview.premium)} / yr", fontSize = 12.sp)
                            Text("• Sum Assured: ${FinancialEngine.formatIndianCurrency(preview.sumAssured)}", fontSize = 12.sp)
                            Text("• Policy Term: ${preview.termYears} years", fontSize = 12.sp)
                            if (preview.maturityBenefit > 0) {
                                Text("• Maturity Benefit: ${FinancialEngine.formatIndianCurrency(preview.maturityBenefit)}", fontSize = 12.sp)
                            }
                        }
                    }
                }

                HorizontalDivider()

                // Target Product Selector
                Text(
                    text = "Apply extracted numbers to slot:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PolicyType.values().forEach { type ->
                        val isSelected = selectedTargetType == type
                        if (isSelected) {
                            Button(
                                onClick = { selectedTargetType = type },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp)
                            ) {
                                Text(type.displayName.take(9), fontSize = 11.sp, maxLines = 1)
                            }
                        } else {
                            OutlinedButton(
                                onClick = { selectedTargetType = type },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp)
                            ) {
                                Text(type.displayName.take(9), fontSize = 11.sp, maxLines = 1)
                            }
                        }
                    }
                }

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                    Button(
                        onClick = {
                            if (rawText.isNotBlank()) {
                                onApplyPolicy(selectedTargetType, rawText)
                            }
                        },
                        enabled = rawText.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                        modifier = Modifier.testTag("apply_extracted_policy_button")
                    ) {
                        Text("Apply to Analyzer")
                    }
                }
            }
        }
    }
}
