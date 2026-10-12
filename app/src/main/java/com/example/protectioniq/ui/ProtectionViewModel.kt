package com.example.protectioniq.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.protectioniq.engine.FinancialEngine
import com.example.protectioniq.model.AiExplanation
import com.example.protectioniq.model.AlertItem
import com.example.protectioniq.model.Assumptions
import com.example.protectioniq.model.FinancialGoal
import com.example.protectioniq.model.PolicyAnalysisResult
import com.example.protectioniq.model.PolicyProduct
import com.example.protectioniq.model.PolicyType
import com.example.protectioniq.model.ProtectionGapResult
import com.example.protectioniq.model.ScenarioPreset
import com.example.protectioniq.model.SensitivityItem
import com.example.protectioniq.model.UserProfile
import com.example.protectioniq.service.ProtectionAiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AppNavStep(val title: String, val stepNumber: Int) {
    DASHBOARD("Dashboard", 0),
    GAP_CALCULATOR("Protection Gap", 1),
    POLICY_ANALYZER("Policy Analyzer", 2),
    SENSITIVITY("Scenarios", 3),
    AI_SUMMARY("AI Explainer", 4)
}

data class ProtectionUiState(
    val currentStep: AppNavStep = AppNavStep.DASHBOARD,
    val profile: UserProfile = UserProfile(),
    val assumptions: Assumptions = Assumptions(),
    val activePreset: ScenarioPreset = ScenarioPreset.BASE,
    val policies: List<PolicyProduct> = FinancialEngine.getSamplePolicies(),
    val gapResult: ProtectionGapResult = FinancialEngine.calculateProtectionGap(UserProfile(), Assumptions()),
    val baseRecommendedCover: Double = FinancialEngine.calculateProtectionGap(UserProfile(), Assumptions()).recommendedCover,
    val policyAnalysisResults: List<PolicyAnalysisResult> = FinancialEngine.analyzePolicies(FinancialEngine.getSamplePolicies(), Assumptions()),
    val sensitivityItems: List<SensitivityItem> = FinancialEngine.calculateSensitivity(UserProfile(), Assumptions()),
    val alerts: List<AlertItem> = emptyList(),
    val aiExplanation: AiExplanation? = null,
    val isAiLoading: Boolean = false,
    val showFormulaExpander: Boolean = false,
    val showReportModal: Boolean = false,
    val showPolicyExtractorDialog: Boolean = false,
    val extractedPolicyText: String = ""
)

class ProtectionViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ProtectionUiState())
    val uiState: StateFlow<ProtectionUiState> = _uiState.asStateFlow()

    init {
        recomputeAll()
    }

    fun setNavStep(step: AppNavStep) {
        _uiState.update { it.copy(currentStep = step) }
    }

    fun updateProfile(newProfile: UserProfile) {
        _uiState.update { it.copy(profile = newProfile) }
        recomputeAll()
    }

    fun addGoal(name: String, amount: Double, years: Int) {
        val updatedGoals = _uiState.value.profile.goals + FinancialGoal(
            name = name,
            targetAmount = amount,
            yearsAway = years
        )
        updateProfile(_uiState.value.profile.copy(goals = updatedGoals))
    }

    fun removeGoal(goalId: String) {
        val updatedGoals = _uiState.value.profile.goals.filterNot { it.id == goalId }
        updateProfile(_uiState.value.profile.copy(goals = updatedGoals))
    }

    fun updateAssumptions(newAssumptions: Assumptions) {
        _uiState.update {
            it.copy(
                assumptions = newAssumptions,
                activePreset = ScenarioPreset.BASE
            )
        }
        recomputeAll()
    }

    fun applyPreset(preset: ScenarioPreset) {
        val newAssumptions = when (preset) {
            ScenarioPreset.BASE -> Assumptions(
                inflationRatePct = 6.0,
                discountRatePct = 7.5,
                incomeGrowthPct = 8.0,
                investmentReturnPct = 11.0
            )
            ScenarioPreset.CONSERVATIVE -> Assumptions(
                inflationRatePct = 7.5,
                discountRatePct = 6.5,
                incomeGrowthPct = 6.0,
                investmentReturnPct = 8.5
            )
            ScenarioPreset.OPTIMISTIC -> Assumptions(
                inflationRatePct = 5.0,
                discountRatePct = 8.5,
                incomeGrowthPct = 10.0,
                investmentReturnPct = 13.0
            )
        }
        _uiState.update {
            it.copy(
                assumptions = newAssumptions,
                activePreset = preset
            )
        }
        recomputeAll()
    }

    fun updatePolicy(policyId: String, premium: Double, term: Int, sumAssured: Double, maturity: Double) {
        val updated = _uiState.value.policies.map {
            if (it.id == policyId) {
                it.copy(
                    annualPremium = premium,
                    termYears = term,
                    sumAssured = sumAssured,
                    maturityBenefit = maturity
                )
            } else it
        }
        _uiState.update { it.copy(policies = updated) }
        recomputeAll()
    }

    fun loadSamplePolicies() {
        _uiState.update { it.copy(policies = FinancialEngine.getSamplePolicies()) }
        recomputeAll()
    }

    fun toggleFormulaExpander() {
        _uiState.update { it.copy(showFormulaExpander = !it.showFormulaExpander) }
    }

    fun setShowReportModal(show: Boolean) {
        _uiState.update { it.copy(showReportModal = show) }
    }

    fun setShowPolicyExtractorDialog(show: Boolean) {
        _uiState.update { it.copy(showPolicyExtractorDialog = show) }
    }

    fun applyExtractedPolicy(targetType: PolicyType, extractedText: String) {
        val extracted = ProtectionAiService.extractPolicyFromText(extractedText)
        val updated = _uiState.value.policies.map { policy ->
            if (policy.type == targetType) {
                policy.copy(
                    annualPremium = extracted.premium,
                    termYears = extracted.termYears,
                    sumAssured = extracted.sumAssured,
                    maturityBenefit = if (targetType == PolicyType.PURE_TERM) 0.0 else extracted.maturityBenefit
                )
            } else policy
        }
        _uiState.update {
            it.copy(
                policies = updated,
                showPolicyExtractorDialog = false
            )
        }
        recomputeAll()
    }

    fun generateAiExplanation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isAiLoading = true) }
            val current = _uiState.value
            val explanation = ProtectionAiService.explainProtectionPlan(
                gapResult = current.gapResult,
                policies = current.policyAnalysisResults,
                userAge = current.profile.age,
                retirementAge = current.profile.retirementAge
            )
            _uiState.update {
                it.copy(
                    aiExplanation = explanation,
                    isAiLoading = false
                )
            }
        }
    }

    private fun recomputeAll() {
        val state = _uiState.value
        val gap = FinancialEngine.calculateProtectionGap(state.profile, state.assumptions)
        val baseAssumptions = Assumptions(inflationRatePct = 6.0, discountRatePct = 7.5, incomeGrowthPct = 8.0, investmentReturnPct = 11.0)
        val baseCover = FinancialEngine.calculateProtectionGap(state.profile, baseAssumptions).recommendedCover
        val policyResults = FinancialEngine.analyzePolicies(state.policies, state.assumptions)
        val sensitivities = FinancialEngine.calculateSensitivity(state.profile, state.assumptions)
        val alertsList = FinancialEngine.generateAlerts(
            gapResult = gap,
            baseRecommendedCover = baseCover,
            policies = policyResults,
            inflationRate = state.assumptions.inflationRatePct
        )

        _uiState.update {
            it.copy(
                gapResult = gap,
                baseRecommendedCover = baseCover,
                policyAnalysisResults = policyResults,
                sensitivityItems = sensitivities,
                alerts = alertsList
            )
        }
    }
}
