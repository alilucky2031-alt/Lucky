package com.example

import com.example.protectioniq.engine.FinancialEngine
import com.example.protectioniq.model.Assumptions
import com.example.protectioniq.model.PolicyProduct
import com.example.protectioniq.model.PolicyType
import com.example.protectioniq.model.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FinancialEngineTest {

    @Test
    fun testIndianCurrencyFormatting() {
        assertEquals("₹1.50 Cr", FinancialEngine.formatIndianCurrency(1_50_00_000.0))
        assertEquals("₹25.00 L", FinancialEngine.formatIndianCurrency(25_00_000.0))
        assertEquals("₹50.0 K", FinancialEngine.formatIndianCurrency(50_000.0))
        assertEquals("15,00,000", FinancialEngine.formatIndianGrouping(1500000L))
        assertEquals("1,00,00,000", FinancialEngine.formatIndianGrouping(10000000L))
    }

    @Test
    fun testHlvCalculation_DeterministicAndPositive() {
        val profile = UserProfile(
            age = 30,
            retirementAge = 60,
            annualIncome = 15_00_000.0,
            personalExpenseRatioPct = 30.0
        )
        val assumptions = Assumptions(
            inflationRatePct = 6.0,
            discountRatePct = 7.5,
            incomeGrowthPct = 8.0
        )
        val hlv = FinancialEngine.calculateHlv(profile, assumptions)
        // With 30 years left, ~10.5L surplus growing at 8% discounted at 7.5%, HLV should be > 3 Crores:
        assertTrue("HLV should be greater than 3 Crores", hlv > 3_00_00_000.0)
    }

    @Test
    fun testProtectionGapCalculation() {
        val profile = UserProfile(
            age = 30,
            retirementAge = 60,
            annualIncome = 12_00_000.0,
            outstandingLoans = 40_00_000.0,
            existingLifeCover = 50_00_000.0,
            existingLiquidSavings = 10_00_000.0
        )
        val assumptions = Assumptions()
        val gap = FinancialEngine.calculateProtectionGap(profile, assumptions)

        assertTrue(gap.hlvGross > 0)
        assertTrue(gap.recommendedCover > gap.existingCover)
        assertTrue(gap.netProtectionGap > 0)
        assertTrue(gap.isUnderInsured)
    }

    @Test
    fun testIrrCalculation_EndowmentCashFlow() {
        // 20-year endowment: pay 100k for 20 years, receive 40L at maturity:
        val cashFlows = DoubleArray(21)
        for (i in 0 until 20) {
            cashFlows[i] = -100_000.0
        }
        cashFlows[20] = 40_00_000.0

        val irr = FinancialEngine.calculateIrr(cashFlows)
        assertNotNull(irr)
        // Expected IRR for traditional endowment in India is typically around 5% to 6.5%:
        assertTrue("IRR should be between 3% and 8%", irr!! in 3.0..8.0)
    }

    @Test
    fun testNpvCalculation() {
        val cashFlows = doubleArrayOf(-1000.0, 500.0, 700.0)
        val npv = FinancialEngine.calculateNpv(cashFlows, 0.10)
        // PV = -1000 + 500/1.1 + 700/1.21 = -1000 + 454.55 + 578.51 = 33.06
        assertEquals(33.05, npv, 0.5)
    }

    @Test
    fun testPolicyAnalyzerAndBtid() {
        val policies = FinancialEngine.getSamplePolicies()
        val assumptions = Assumptions(investmentReturnPct = 11.0)
        val results = FinancialEngine.analyzePolicies(policies, assumptions)

        assertEquals(3, results.size)
        val pureTerm = results.first { it.product.type == PolicyType.PURE_TERM }
        val endowment = results.first { it.product.type == PolicyType.ENDOWMENT }

        // Pure term cover per rupee must be dramatically higher than endowment:
        assertTrue(pureTerm.coverPerRupee > endowment.coverPerRupee * 10)
        // BTID gain for endowment must be massive:
        assertNotNull(endowment.btidCorpus)
        assertTrue(endowment.btidCorpus!! > endowment.product.maturityBenefit)
    }

    @Test
    fun testSensitivityTornadoRanking() {
        val profile = UserProfile()
        val assumptions = Assumptions()
        val sensitivity = FinancialEngine.calculateSensitivity(profile, assumptions)

        assertEquals(4, sensitivity.size)
        // Check that ranking is sorted descending by swingAmount:
        assertTrue(sensitivity[0].swingAmount >= sensitivity[1].swingAmount)
        assertTrue(sensitivity[1].swingAmount >= sensitivity[2].swingAmount)
    }
}
