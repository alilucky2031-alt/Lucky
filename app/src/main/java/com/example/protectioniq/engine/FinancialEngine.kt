package com.example.protectioniq.engine

import com.example.protectioniq.model.AlertItem
import com.example.protectioniq.model.AlertSeverity
import com.example.protectioniq.model.Assumptions
import com.example.protectioniq.model.FinancialGoal
import com.example.protectioniq.model.PolicyAnalysisResult
import com.example.protectioniq.model.PolicyProduct
import com.example.protectioniq.model.PolicyType
import com.example.protectioniq.model.ProtectionGapResult
import com.example.protectioniq.model.SensitivityItem
import com.example.protectioniq.model.UserProfile
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

object FinancialEngine {

    /**
     * Formats amounts in Indian numbering system (Crores, Lakhs, Thousands) with Rupee symbol.
     */
    fun formatIndianCurrency(amount: Double, compact: Boolean = true): String {
        val sign = if (amount < 0) "-" else ""
        val absVal = abs(amount)

        if (compact) {
            return when {
                absVal >= 1_00_00_000.0 -> {
                    val cr = absVal / 1_00_00_000.0
                    "${sign}₹${String.format(Locale.US, "%.2f", cr)} Cr"
                }
                absVal >= 1_00_000.0 -> {
                    val l = absVal / 1_00_000.0
                    "${sign}₹${String.format(Locale.US, "%.2f", l)} L"
                }
                absVal >= 1_000.0 -> {
                    val k = absVal / 1_000.0
                    "${sign}₹${String.format(Locale.US, "%.1f", k)} K"
                }
                else -> "${sign}₹${String.format(Locale.US, "%.0f", absVal)}"
            }
        } else {
            return "${sign}₹${formatIndianGrouping(absVal.toLong())}"
        }
    }

    /**
     * Formats integer with Indian commas: e.g. 15,00,000
     */
    fun formatIndianGrouping(value: Long): String {
        val s = value.toString()
        if (s.length <= 3) return s
        val lastThree = s.substring(s.length - 3)
        val remaining = s.substring(0, s.length - 3)
        val sb = StringBuilder()
        var count = 0
        for (i in remaining.length - 1 downTo 0) {
            sb.append(remaining[i])
            count++
            if (count % 2 == 0 && i > 0) {
                sb.append(',')
            }
        }
        return sb.reverse().toString() + "," + lastThree
    }

    /**
     * Human Life Value (HLV) calculation:
     * PV of future (income - personal expenses) until retirement, discounted at discount rate.
     * Surplus = Income_t - PersonalExpenses_t.
     * Discounted back to present value using discount rate.
     */
    fun calculateHlv(profile: UserProfile, assumptions: Assumptions): Double {
        val workingYears = max(0, profile.retirementAge - profile.age)
        if (workingYears <= 0) return 0.0

        val discountRate = assumptions.discountRatePct / 100.0
        val incomeGrowthRate = assumptions.incomeGrowthPct / 100.0
        val inflationRate = assumptions.inflationRatePct / 100.0

        // Personal expense portion of income:
        val personalExpensesCurrent = profile.annualIncome * (profile.personalExpenseRatioPct / 100.0)

        var totalPv = 0.0
        for (t in 1..workingYears) {
            val incomeT = profile.annualIncome * (1.0 + incomeGrowthRate).pow(t)
            val personalExpenseT = personalExpensesCurrent * (1.0 + inflationRate).pow(t)
            val netSurplusT = max(0.0, incomeT - personalExpenseT)

            val discountFactor = (1.0 + discountRate).pow(t)
            totalPv += (netSurplusT / discountFactor)
        }

        return totalPv
    }

    /**
     * Protection Gap calculation:
     * Recommended cover = HLV + liabilities + goals - existing cover - savings.
     */
    fun calculateProtectionGap(profile: UserProfile, assumptions: Assumptions): ProtectionGapResult {
        val workingYears = max(0, profile.retirementAge - profile.age)
        val hlv = calculateHlv(profile, assumptions)
        val liabilities = max(0.0, profile.outstandingLoans)

        // Calculate goals present value discounted at inflation or discount rate:
        val discountRate = assumptions.discountRatePct / 100.0
        val inflationRate = assumptions.inflationRatePct / 100.0

        var goalsTotal = 0.0
        profile.goals.forEach { goal ->
            // Goal inflated into future and discounted back, or direct goal target:
            // PV = Target * (1 + inflation)^years / (1 + discount)^years
            val years = max(1, goal.yearsAway)
            val futureCost = goal.targetAmount * (1.0 + inflationRate).pow(years)
            val pvGoal = futureCost / (1.0 + discountRate).pow(years)
            goalsTotal += pvGoal
        }

        val grossNeed = hlv + liabilities + goalsTotal
        val liquidSavings = max(0.0, profile.existingLiquidSavings)
        val recommendedCover = max(0.0, grossNeed - liquidSavings)

        val existingCover = max(0.0, profile.existingLifeCover)
        val netGap = max(0.0, recommendedCover - existingCover)

        val coverageRatio = if (recommendedCover > 0) {
            (existingCover / recommendedCover) * 100.0
        } else 100.0

        return ProtectionGapResult(
            hlvGross = hlv,
            liabilities = liabilities,
            goalsTotal = goalsTotal,
            grossProtectionNeed = grossNeed,
            liquidAssetsOffset = liquidSavings,
            recommendedCover = recommendedCover,
            existingCover = existingCover,
            netProtectionGap = netGap,
            coverageRatioPct = coverageRatio,
            workingYearsLeft = workingYears,
            isUnderInsured = netGap > 0
        )
    }

    /**
     * Net Present Value (NPV) calculation.
     * Cash flow at index t corresponds to year t.
     */
    fun calculateNpv(cashFlows: DoubleArray, rateFraction: Double): Double {
        var npv = 0.0
        for (t in cashFlows.indices) {
            npv += cashFlows[t] / (1.0 + rateFraction).pow(t)
        }
        return npv
    }

    /**
     * Internal Rate of Return (IRR) calculation using robust Bisection algorithm.
     * Cash flows: negative for outflows (premiums), positive for inflows (maturity).
     */
    fun calculateIrr(cashFlows: DoubleArray, maxIterations: Int = 1000): Double? {
        val hasNegative = cashFlows.any { it < 0 }
        val hasPositive = cashFlows.any { it > 0 }
        if (!hasNegative || !hasPositive) return null

        var lowRate = -0.50 // -50%
        var highRate = 1.00 // 100%

        var npvLow = calculateNpv(cashFlows, lowRate)
        var npvHigh = calculateNpv(cashFlows, highRate)

        // Expand bounds if needed
        var attempts = 0
        while (npvLow * npvHigh > 0 && attempts < 20) {
            highRate += 0.50
            npvHigh = calculateNpv(cashFlows, highRate)
            attempts++
        }

        if (npvLow * npvHigh > 0) return null

        for (i in 0 until maxIterations) {
            val midRate = (lowRate + highRate) / 2.0
            val npvMid = calculateNpv(cashFlows, midRate)

            if (abs(npvMid) < 1e-4 || (highRate - lowRate) < 1e-6) {
                return midRate * 100.0 // Return percentage
            }

            if (npvLow * npvMid < 0) {
                highRate = midRate
                npvHigh = npvMid
            } else {
                lowRate = midRate
                npvLow = npvMid
            }
        }

        return ((lowRate + highRate) / 2.0) * 100.0
    }

    /**
     * Analyzes insurance policies and the "Buy Term + Invest the Difference" (BTID) strategy.
     */
    fun analyzePolicies(
        policies: List<PolicyProduct>,
        assumptions: Assumptions
    ): List<PolicyAnalysisResult> {
        val discountRate = assumptions.discountRatePct / 100.0
        val investmentReturn = assumptions.investmentReturnPct / 100.0

        // Find pure term annual premium baseline for BTID comparison:
        val pureTermProduct = policies.firstOrNull { it.type == PolicyType.PURE_TERM }
        val pureTermPremium = pureTermProduct?.annualPremium ?: 18_000.0

        return policies.map { policy ->
            val term = policy.termYears
            val totalPremiums = policy.annualPremium * term

            // Construct cash flows: year 0 to term-1: -annualPremium
            // year term: -annualPremium (if payable at start of year) or at year end
            // Let's model annual premium paid at beginning of each year t=0..term-1
            // and maturity received at t=term.
            val cashFlows = DoubleArray(term + 1)
            for (t in 0 until term) {
                cashFlows[t] = -policy.annualPremium
            }
            cashFlows[term] = policy.maturityBenefit

            val irr = if (policy.maturityBenefit > 0) {
                calculateIrr(cashFlows)
            } else null

            val npv = calculateNpv(cashFlows, discountRate)

            // Cover per rupee spent per year:
            val coverPerRupee = if (policy.annualPremium > 0) {
                policy.sumAssured / policy.annualPremium
            } else 0.0

            // Break even year: when cumulative maturity/guaranteed value exceeds premiums paid
            val breakEvenYear = if (policy.maturityBenefit >= totalPremiums && totalPremiums > 0) {
                // Approximate break-even year in traditional endowment policies is near maturity:
                min(term, max(1, (term * (totalPremiums / policy.maturityBenefit)).toInt()))
            } else null

            // BTID corpus calculation if this policy is more expensive than pure term:
            val btidCorpus = if (policy.type != PolicyType.PURE_TERM && policy.annualPremium > pureTermPremium) {
                val premiumDiff = policy.annualPremium - pureTermPremium
                // Future value of annual difference invested at investmentReturn compounded over term:
                // FV = sum_{t=1..term} diff * (1 + r)^(term - t + 1)
                var accumulated = 0.0
                for (t in 1..term) {
                    accumulated += premiumDiff * (1.0 + investmentReturn).pow(term - t + 1)
                }
                accumulated
            } else null

            val btidGain = if (btidCorpus != null) {
                btidCorpus - policy.maturityBenefit
            } else null

            PolicyAnalysisResult(
                product = policy,
                totalPremiumsPaid = totalPremiums,
                irrPct = irr,
                npv = npv,
                breakEvenYear = breakEvenYear,
                coverPerRupee = coverPerRupee,
                isBestValue = policy.type == PolicyType.PURE_TERM,
                btidCorpus = btidCorpus,
                btidGainOverMaturity = btidGain
            )
        }
    }

    /**
     * Tornado chart sensitivity analysis:
     * Calculates recommended cover sensitivity by testing +/- 2% swings on each assumption.
     */
    fun calculateSensitivity(
        profile: UserProfile,
        baseAssumptions: Assumptions
    ): List<SensitivityItem> {
        val baseCover = calculateProtectionGap(profile, baseAssumptions).recommendedCover

        val testCases = listOf(
            Triple("discountRate", "Discount Rate", baseAssumptions.discountRatePct),
            Triple("incomeGrowth", "Income Growth %", baseAssumptions.incomeGrowthPct),
            Triple("inflation", "Inflation Rate", baseAssumptions.inflationRatePct),
            Triple("investmentReturn", "Investment Return", baseAssumptions.investmentReturnPct)
        )

        val results = testCases.map { (key, name, basePct) ->
            val lowPct = max(1.0, basePct - 2.0)
            val highPct = basePct + 2.0

            val lowAssumptions = when (key) {
                "discountRate" -> baseAssumptions.copy(discountRatePct = lowPct)
                "incomeGrowth" -> baseAssumptions.copy(incomeGrowthPct = lowPct)
                "inflation" -> baseAssumptions.copy(inflationRatePct = lowPct)
                else -> baseAssumptions.copy(investmentReturnPct = lowPct)
            }

            val highAssumptions = when (key) {
                "discountRate" -> baseAssumptions.copy(discountRatePct = highPct)
                "incomeGrowth" -> baseAssumptions.copy(incomeGrowthPct = highPct)
                "inflation" -> baseAssumptions.copy(inflationRatePct = highPct)
                else -> baseAssumptions.copy(investmentReturnPct = highPct)
            }

            val lowCover = calculateProtectionGap(profile, lowAssumptions).recommendedCover
            val highCover = calculateProtectionGap(profile, highAssumptions).recommendedCover
            val swing = abs(highCover - lowCover)

            SensitivityItem(
                parameterKey = key,
                parameterName = name,
                baselinePct = basePct,
                lowTestPct = lowPct,
                highTestPct = highPct,
                lowCover = lowCover,
                highCover = highCover,
                swingAmount = swing,
                impactRanking = 0
            )
        }.sortedByDescending { it.swingAmount }

        // Assign impact ranking
        return results.mapIndexed { index, item ->
            item.copy(impactRanking = index + 1)
        }
    }

    /**
     * Generates variance and safety alerts:
     * - Flag when existing cover is < 50% of recommended.
     * - Flag when current scenario recommended cover deviates by > 15% from base case.
     * - Flag when endowment plan IRR is lower than long-term inflation.
     */
    fun generateAlerts(
        gapResult: ProtectionGapResult,
        baseRecommendedCover: Double,
        policies: List<PolicyAnalysisResult> = emptyList(),
        inflationRate: Double = 6.0
    ): List<AlertItem> {
        val alerts = mutableListOf<AlertItem>()

        // 1. Severe underinsurance alert:
        if (gapResult.recommendedCover > 0) {
            val ratio = gapResult.coverageRatioPct
            if (ratio < 50.0) {
                alerts.add(
                    AlertItem(
                        severity = AlertSeverity.DANGER,
                        title = "Severe Underinsurance Alert",
                        message = "Your current life cover (${formatIndianCurrency(gapResult.existingCover)}) covers only ${String.format(Locale.US, "%.0f", ratio)}% of your family's financial need. You have a gap of ${formatIndianCurrency(gapResult.netProtectionGap)}.",
                        metricDelta = "${String.format(Locale.US, "%.0f", ratio)}% of Need"
                    )
                )
            } else if (ratio < 80.0) {
                alerts.add(
                    AlertItem(
                        severity = AlertSeverity.WARNING,
                        title = "Moderate Protection Gap",
                        message = "Your cover is below the recommended safety threshold of 100%. Consider bridging the ${formatIndianCurrency(gapResult.netProtectionGap)} shortfall.",
                        metricDelta = "${String.format(Locale.US, "%.0f", ratio)}% Covered"
                    )
                )
            } else {
                alerts.add(
                    AlertItem(
                        severity = AlertSeverity.INFO,
                        title = "Adequate Protection",
                        message = "Congratulations! Your current life cover meets or exceeds the calculated Human Life Value need.",
                        metricDelta = "100%+ Safe"
                    )
                )
            }
        }

        // 2. Scenario variance alert (> 15% variance vs base case)
        if (baseRecommendedCover > 0) {
            val variancePct = ((gapResult.recommendedCover - baseRecommendedCover) / baseRecommendedCover) * 100.0
            if (abs(variancePct) >= 15.0) {
                val dir = if (variancePct > 0) "higher" else "lower"
                alerts.add(
                    AlertItem(
                        severity = AlertSeverity.WARNING,
                        title = "Assumption Sensitivity Triggered (>15% Shift)",
                        message = "Your current economic assumptions shifted your required cover by ${String.format(Locale.US, "%.1f", abs(variancePct))}% ($dir) compared to the base benchmark.",
                        metricDelta = "${if (variancePct > 0) "+" else ""}${String.format(Locale.US, "%.1f", variancePct)}%"
                    )
                )
            }
        }

        // 3. Negative real return / low IRR alert for endowment products:
        policies.firstOrNull { it.product.type == PolicyType.ENDOWMENT }?.let { endRes ->
            endRes.irrPct?.let { irr ->
                if (irr < inflationRate) {
                    alerts.add(
                        AlertItem(
                            severity = AlertSeverity.WARNING,
                            title = "Sub-Inflation Policy Return",
                            message = "The endowment plan delivers an internal return of ${String.format(Locale.US, "%.1f", irr)}%, which is below long-term inflation (${String.format(Locale.US, "%.1f", inflationRate)}%). Your purchasing power is eroding over time.",
                            metricDelta = "${String.format(Locale.US, "%.1f", irr)}% IRR"
                        )
                    )
                }
            }
        }

        return alerts
    }

    /**
     * Default realistic sample Indian insurance policies.
     */
    fun getSamplePolicies(): List<PolicyProduct> {
        return listOf(
            PolicyProduct(
                id = "pure_term_1",
                name = "HDFC Life / ICICI Pure Term Plan",
                type = PolicyType.PURE_TERM,
                annualPremium = 16_500.0,
                termYears = 30,
                sumAssured = 1_00_00_000.0, // ₹1 Crore
                maturityBenefit = 0.0
            ),
            PolicyProduct(
                id = "trop_1",
                name = "Max Life Smart TROP (Return of Premium)",
                type = PolicyType.TROP,
                annualPremium = 39_500.0,
                termYears = 30,
                sumAssured = 1_00_00_000.0, // ₹1 Crore
                maturityBenefit = 39_500.0 * 30 // ₹11.85 Lakhs (100% premiums returned)
            ),
            PolicyProduct(
                id = "endowment_1",
                name = "Traditional Endowment / Guaranteed Savings Plan",
                type = PolicyType.ENDOWMENT,
                annualPremium = 1_20_000.0,
                termYears = 20,
                sumAssured = 25_00_000.0, // ₹25 Lakhs
                maturityBenefit = 42_00_000.0 // ₹42 Lakhs maturity
            )
        )
    }
}
