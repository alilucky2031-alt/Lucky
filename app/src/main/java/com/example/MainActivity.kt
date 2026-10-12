package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.protectioniq.ui.AppNavStep
import com.example.protectioniq.ui.ProtectionViewModel
import com.example.protectioniq.ui.dialogs.PolicyExtractorDialog
import com.example.protectioniq.ui.dialogs.PrintableReportDialog
import com.example.protectioniq.ui.modules.DashboardOverviewView
import com.example.protectioniq.ui.modules.Module1GapCalculatorView
import com.example.protectioniq.ui.modules.Module2PolicyAnalyzerView
import com.example.protectioniq.ui.modules.Module3SensitivityView
import com.example.protectioniq.ui.modules.Module4AiExplainerView
import com.example.ui.theme.ProtectionIQTheme
import com.example.ui.theme.TealAccent
import com.example.ui.theme.TealPrimary

class MainActivity : ComponentActivity() {

    private val viewModel: ProtectionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProtectionIQTheme {
                ProtectionIQApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProtectionIQApp(viewModel: ProtectionViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Back handler to navigate back to Dashboard when in sub-screens
    BackHandler(enabled = uiState.currentStep != AppNavStep.DASHBOARD) {
        viewModel.setNavStep(AppNavStep.DASHBOARD)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(TealPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "ProtectionIQ Shield",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "ProtectionIQ",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.size(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(TealAccent.copy(alpha = 0.2f))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "INDIA",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = TealPrimary,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                            Text(
                                text = "Life Insurance Decision Tool",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.setShowReportModal(true) },
                        modifier = Modifier.testTag("top_bar_report_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "View Executive Report",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("bottom_nav_bar"),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                NavigationBarItem(
                    selected = uiState.currentStep == AppNavStep.DASHBOARD,
                    onClick = { viewModel.setNavStep(AppNavStep.DASHBOARD) },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Overview", fontSize = 10.sp) },
                    modifier = Modifier.testTag("nav_item_dashboard"),
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = TealPrimary, indicatorColor = TealPrimary.copy(alpha = 0.15f))
                )
                NavigationBarItem(
                    selected = uiState.currentStep == AppNavStep.GAP_CALCULATOR,
                    onClick = { viewModel.setNavStep(AppNavStep.GAP_CALCULATOR) },
                    icon = { Icon(Icons.Default.Calculate, contentDescription = "Gap") },
                    label = { Text("1. Gap", fontSize = 10.sp) },
                    modifier = Modifier.testTag("nav_item_gap"),
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = TealPrimary, indicatorColor = TealPrimary.copy(alpha = 0.15f))
                )
                NavigationBarItem(
                    selected = uiState.currentStep == AppNavStep.POLICY_ANALYZER,
                    onClick = { viewModel.setNavStep(AppNavStep.POLICY_ANALYZER) },
                    icon = { Icon(Icons.AutoMirrored.Filled.CompareArrows, contentDescription = "Compare") },
                    label = { Text("2. Policies", fontSize = 10.sp) },
                    modifier = Modifier.testTag("nav_item_policies"),
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = TealPrimary, indicatorColor = TealPrimary.copy(alpha = 0.15f))
                )
                NavigationBarItem(
                    selected = uiState.currentStep == AppNavStep.SENSITIVITY,
                    onClick = { viewModel.setNavStep(AppNavStep.SENSITIVITY) },
                    icon = { Icon(Icons.Default.CrisisAlert, contentDescription = "Scenarios") },
                    label = { Text("3. Scenarios", fontSize = 10.sp) },
                    modifier = Modifier.testTag("nav_item_scenarios"),
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = TealPrimary, indicatorColor = TealPrimary.copy(alpha = 0.15f))
                )
                NavigationBarItem(
                    selected = uiState.currentStep == AppNavStep.AI_SUMMARY,
                    onClick = { viewModel.setNavStep(AppNavStep.AI_SUMMARY) },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI Explainer") },
                    label = { Text("4. AI Brief", fontSize = 10.sp) },
                    modifier = Modifier.testTag("nav_item_ai_brief"),
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = TealPrimary, indicatorColor = TealPrimary.copy(alpha = 0.15f))
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                AnimatedContent(
                    targetState = uiState.currentStep,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "step_transition"
                ) { step ->
                    when (step) {
                        AppNavStep.DASHBOARD -> {
                            DashboardOverviewView(
                                gapResult = uiState.gapResult,
                                policies = uiState.policyAnalysisResults,
                                sensitivities = uiState.sensitivityItems,
                                onNavigateStep = { viewModel.setNavStep(it) },
                                onOpenPrintableReport = { viewModel.setShowReportModal(true) }
                            )
                        }
                        AppNavStep.GAP_CALCULATOR -> {
                            Module1GapCalculatorView(
                                profile = uiState.profile,
                                assumptions = uiState.assumptions,
                                gapResult = uiState.gapResult,
                                showFormulaExpander = uiState.showFormulaExpander,
                                onToggleFormulaExpander = { viewModel.toggleFormulaExpander() },
                                onUpdateProfile = { viewModel.updateProfile(it) },
                                onAddGoal = { name, amt, yrs -> viewModel.addGoal(name, amt, yrs) },
                                onRemoveGoal = { viewModel.removeGoal(it) }
                            )
                        }
                        AppNavStep.POLICY_ANALYZER -> {
                            Module2PolicyAnalyzerView(
                                policies = uiState.policyAnalysisResults,
                                assumptions = uiState.assumptions,
                                onUpdatePolicy = { id, prem, term, sa, mat ->
                                    viewModel.updatePolicy(id, prem, term, sa, mat)
                                },
                                onLoadSamplePolicies = { viewModel.loadSamplePolicies() },
                                onOpenPolicyExtractor = { viewModel.setShowPolicyExtractorDialog(true) }
                            )
                        }
                        AppNavStep.SENSITIVITY -> {
                            Module3SensitivityView(
                                assumptions = uiState.assumptions,
                                activePreset = uiState.activePreset,
                                gapResult = uiState.gapResult,
                                sensitivities = uiState.sensitivityItems,
                                alerts = uiState.alerts,
                                onUpdateAssumptions = { viewModel.updateAssumptions(it) },
                                onApplyPreset = { viewModel.applyPreset(it) }
                            )
                        }
                        AppNavStep.AI_SUMMARY -> {
                            Module4AiExplainerView(
                                explanation = uiState.aiExplanation,
                                isLoading = uiState.isAiLoading,
                                onGenerateExplanation = { viewModel.generateAiExplanation() },
                                onOpenPrintableReport = { viewModel.setShowReportModal(true) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal dialogs
    if (uiState.showReportModal) {
        PrintableReportDialog(
            profile = uiState.profile,
            gapResult = uiState.gapResult,
            policies = uiState.policyAnalysisResults,
            assumptions = uiState.assumptions,
            explanation = uiState.aiExplanation,
            onDismiss = { viewModel.setShowReportModal(false) }
        )
    }

    if (uiState.showPolicyExtractorDialog) {
        PolicyExtractorDialog(
            onDismiss = { viewModel.setShowPolicyExtractorDialog(false) },
            onApplyPolicy = { type, text ->
                viewModel.applyExtractedPolicy(type, text)
            }
        )
    }
}
