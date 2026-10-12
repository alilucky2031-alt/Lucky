package com.example.protectioniq.model

import java.util.UUID

data class UserProfile(
    val age: Int = 32,
    val retirementAge: Int = 60,
    val annualIncome: Double = 18_00_000.0, // 18 Lakhs
    val expectedIncomeGrowthPct: Double = 8.0, // 8%
    val annualExpenses: Double = 7_20_000.0, // 7.2 Lakhs
    val personalExpenseRatioPct: Double = 30.0, // 30% of income is personal, remainder is family
    val dependents: Int = 2,
    val outstandingLoans: Double = 45_00_000.0, // 45 Lakhs (Home + Car)
    val goals: List<FinancialGoal> = listOf(
        FinancialGoal(id = "1", name = "Child Higher Education", targetAmount = 30_00_000.0, yearsAway = 14),
        FinancialGoal(id = "2", name = "Child Marriage", targetAmount = 20_00_000.0, yearsAway = 18)
    ),
    val existingLifeCover: Double = 50_00_000.0, // 50 Lakhs
    val existingLiquidSavings: Double = 15_00_000.0 // 15 Lakhs
)

data class FinancialGoal(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val targetAmount: Double,
    val yearsAway: Int
)

enum class PolicyType(val displayName: String, val shortDesc: String) {
    PURE_TERM("Pure Term Plan", "High sum assured, zero maturity benefit, lowest cost"),
    TROP("Term with Return of Premium", "Returns raw premiums paid if policyholder survives term"),
    ENDOWMENT("Endowment / Savings Plan", "Bundled investment & insurance, low cover, low returns")
}

data class PolicyProduct(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: PolicyType,
    val annualPremium: Double,
    val termYears: Int,
    val sumAssured: Double,
    val maturityBenefit: Double
)

data class Assumptions(
    val inflationRatePct: Double = 6.0,
    val discountRatePct: Double = 7.5,
    val incomeGrowthPct: Double = 8.0,
    val investmentReturnPct: Double = 11.0
)

enum class ScenarioPreset(val label: String, val description: String) {
    BASE("Base Case", "Standard RBI long-term assumptions (Inflation 6%, Discount 7.5%, Growth 8%, Inv 11%)"),
    CONSERVATIVE("Conservative", "Higher inflation, lower salary growth & returns (Inflation 7.5%, Discount 6.5%, Growth 6%, Inv 8.5%)"),
    OPTIMISTIC("Optimistic", "Higher salary growth & equity returns (Inflation 5%, Discount 8.5%, Growth 10%, Inv 13%)")
}

data class ProtectionGapResult(
    val hlvGross: Double,
    val liabilities: Double,
    val goalsTotal: Double,
    val grossProtectionNeed: Double,
    val liquidAssetsOffset: Double,
    val recommendedCover: Double,
    val existingCover: Double,
    val netProtectionGap: Double,
    val coverageRatioPct: Double,
    val workingYearsLeft: Int,
    val isUnderInsured: Boolean
)

data class PolicyAnalysisResult(
    val product: PolicyProduct,
    val totalPremiumsPaid: Double,
    val irrPct: Double?,
    val npv: Double,
    val breakEvenYear: Int?,
    val coverPerRupee: Double,
    val isBestValue: Boolean,
    val btidCorpus: Double?,
    val btidGainOverMaturity: Double?
)

data class SensitivityItem(
    val parameterKey: String,
    val parameterName: String,
    val baselinePct: Double,
    val lowTestPct: Double,
    val highTestPct: Double,
    val lowCover: Double,
    val highCover: Double,
    val swingAmount: Double,
    val impactRanking: Int
)

enum class AlertSeverity { DANGER, WARNING, INFO }

data class AlertItem(
    val severity: AlertSeverity,
    val title: String,
    val message: String,
    val metricDelta: String? = null
)

data class AiGlossaryItem(
    val term: String,
    val definition: String
)

data class AiExplanation(
    val headline: String,
    val keyInsights: List<String>,
    val risks: List<String>,
    val suggestedNextStep: String,
    val glossary: List<AiGlossaryItem>,
    val disclaimer: String = "Illustrative calculation tool only. Not SEBI registered or insurance advice.",
    val isAiGenerated: Boolean = true
)
