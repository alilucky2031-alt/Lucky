package com.example.protectioniq.ui.modules

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.protectioniq.engine.FinancialEngine
import com.example.protectioniq.model.PolicyAnalysisResult
import com.example.protectioniq.model.ProtectionGapResult
import com.example.protectioniq.model.SensitivityItem
import com.example.protectioniq.ui.AppNavStep
import com.example.protectioniq.ui.components.KpiHeaderCards
import com.example.protectioniq.ui.components.ProtectionGauge
import com.example.ui.theme.BlueElectric
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.Navy900
import com.example.ui.theme.RoseDanger
import com.example.ui.theme.TealAccent
import com.example.ui.theme.TealPrimary
import com.example.ui.theme.VioletCategory

@Composable
fun DashboardOverviewView(
    gapResult: ProtectionGapResult,
    policies: List<PolicyAnalysisResult>,
    sensitivities: List<SensitivityItem>,
    onNavigateStep: (AppNavStep) -> Unit,
    onOpenPrintableReport: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Card with Generated Graphic
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("hero_banner_card"),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.hero_protection_banner_1791815543243),
                    contentDescription = "ProtectionIQ Shield Illustration",
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Crop
                )

                // Dark gradient overlay to keep text hyper-legible
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Navy900.copy(alpha = 0.85f), Navy900)
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(TealAccent)
                        )
                        Text(
                            text = "DETERMINISTIC FINANCIAL ENGINE",
                            style = MaterialTheme.typography.labelSmall,
                            color = TealAccent,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                    Text(
                        text = "Life Insurance Decision Tool",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Human Life Value • Policy IRR & NPV • Live Sensitivity • AI Explainer",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFCBD5E1)
                    )
                }
            }
        }

        // 4 KPI Cards
        KpiHeaderCards(
            gapResult = gapResult,
            policies = policies,
            sensitivities = sensitivities
        )

        // Quick Coverage Meter Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateStep(AppNavStep.GAP_CALCULATOR) }
                .testTag("dashboard_gauge_card"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Coverage Meter",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Open Details",
                            style = MaterialTheme.typography.labelSmall,
                            color = TealPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = TealPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                ProtectionGauge(gapResult = gapResult)

                Text(
                    text = if (gapResult.isUnderInsured) {
                        "Tap to adjust income, retirement, loans, and family milestones."
                    } else {
                        "Your current cover exceeds your calculated actuarial Human Life Value."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Module Navigation Directory
        Text(
            text = "Decision Modules",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        ModuleNavCard(
            stepNumber = "01",
            title = "Protection Gap Calculator",
            description = "Compute actuarial Human Life Value (HLV) PV, outstanding loans & future goal funding.",
            icon = Icons.Default.Calculate,
            accentColor = TealPrimary,
            tag = "Module 1",
            onClick = { onNavigateStep(AppNavStep.GAP_CALCULATOR) },
            testTag = "nav_card_module_1"
        )

        ModuleNavCard(
            stepNumber = "02",
            title = "Policy Value Analyzer",
            description = "Compare Pure Term, Return of Premium (TROP), and Endowment. Calculate IRR, NPV, & BTID.",
            icon = Icons.AutoMirrored.Filled.CompareArrows,
            accentColor = BlueElectric,
            tag = "Module 2",
            onClick = { onNavigateStep(AppNavStep.POLICY_ANALYZER) },
            testTag = "nav_card_module_2"
        )

        ModuleNavCard(
            stepNumber = "03",
            title = "Scenario & Sensitivity Engine",
            description = "Live assumption sliders, Base/Conservative/Optimistic presets, Tornado ranking & variance alerts.",
            icon = Icons.Default.CrisisAlert,
            accentColor = VioletCategory,
            tag = "Module 3",
            onClick = { onNavigateStep(AppNavStep.SENSITIVITY) },
            testTag = "nav_card_module_3"
        )

        ModuleNavCard(
            stepNumber = "04",
            title = "AI Explainer & Executive Summary",
            description = "Structured Gemini briefing with 3 takeaways, 3 risks, next steps, and plain glossary.",
            icon = Icons.Default.AutoAwesome,
            accentColor = TealPrimary,
            tag = "Module 4",
            onClick = { onNavigateStep(AppNavStep.AI_SUMMARY) },
            testTag = "nav_card_module_4"
        )

        // Privacy Footer Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Column {
                    Text(
                        text = "Data stays in your browser session and is not stored.",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "All calculations run deterministically on-device in verified Kotlin code.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ModuleNavCard(
    stepNumber: String,
    title: String,
    description: String,
    icon: ImageVector,
    accentColor: Color,
    tag: String,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(testTag),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = tag,
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Step $stepNumber",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
