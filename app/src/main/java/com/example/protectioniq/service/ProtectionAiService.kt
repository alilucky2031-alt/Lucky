package com.example.protectioniq.service

import com.example.BuildConfig
import com.example.protectioniq.engine.FinancialEngine
import com.example.protectioniq.model.AiExplanation
import com.example.protectioniq.model.AiGlossaryItem
import com.example.protectioniq.model.PolicyAnalysisResult
import com.example.protectioniq.model.PolicyType
import com.example.protectioniq.model.ProtectionGapResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object ProtectionAiService {

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    /**
     * Calls Gemini API to explain the computed numbers, or falls back to deterministic local insights.
     */
    suspend fun explainProtectionPlan(
        gapResult: ProtectionGapResult,
        policies: List<PolicyAnalysisResult>,
        userAge: Int,
        retirementAge: Int
    ): AiExplanation = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        // Construct raw numbers payload:
        val payloadJson = buildFinancialPayload(gapResult, policies, userAge, retirementAge)

        if (apiKey.isNullOrBlank() || apiKey == "MY_GEMINI_API_KEY") {
            // Provide crisp deterministic financial explanation
            return@withContext generateDeterministicExplanation(gapResult, policies)
        }

        try {
            val url = "$BASE_URL/$MODEL_NAME:generateContent?key=$apiKey"
            val promptText = """
                Analyze the following user protection numbers and policy comparisons:
                $payloadJson

                Return ONLY valid JSON matching this schema:
                {
                  "headline": "Short crisp takeaway headline",
                  "keyInsights": ["Insight 1", "Insight 2", "Insight 3"],
                  "risks": ["Risk 1", "Risk 2", "Risk 3"],
                  "suggestedNextStep": "One actionable recommended next step",
                  "glossary": [
                    {"term": "Term Name", "definition": "Plain-English 1-line definition"}
                  ],
                  "disclaimer": "This analysis is illustrative only and does not constitute formal legal, tax or financial advice."
                }
            """.trimIndent()

            val requestBodyJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", promptText)
                            })
                        })
                    })
                })
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", "You are a careful insurance explainer. Use ONLY the numbers provided. Never recompute or invent figures. Use simple language. Do not give legal, tax or financial advice. End with a one-line disclaimer.")
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.2)
                })
            }

            val request = Request.Builder()
                .url(url)
                .post(requestBodyJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseString = response.body?.string() ?: ""

            if (!response.isSuccessful || responseString.isBlank()) {
                return@withContext generateDeterministicExplanation(gapResult, policies)
            }

            // Parse response:
            val root = JSONObject(responseString)
            val candidates = root.optJSONArray("candidates")
            val candidate = candidates?.optJSONObject(0)
            val content = candidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val rawText = parts?.optJSONObject(0)?.optString("text") ?: ""

            if (rawText.isBlank()) {
                return@withContext generateDeterministicExplanation(gapResult, policies)
            }

            parseExplanationJson(rawText, gapResult, policies)
        } catch (e: Exception) {
            generateDeterministicExplanation(gapResult, policies)
        }
    }

    private fun parseExplanationJson(
        jsonString: String,
        gapResult: ProtectionGapResult,
        policies: List<PolicyAnalysisResult>
    ): AiExplanation {
        return try {
            val obj = JSONObject(jsonString)
            val headline = obj.optString("headline", "ProtectionIQ Financial Assessment")
            val insightsList = mutableListOf<String>()
            obj.optJSONArray("keyInsights")?.let { arr ->
                for (i in 0 until arr.length()) {
                    insightsList.add(arr.getString(i))
                }
            }
            if (insightsList.isEmpty()) insightsList.addAll(generateDeterministicExplanation(gapResult, policies).keyInsights)

            val risksList = mutableListOf<String>()
            obj.optJSONArray("risks")?.let { arr ->
                for (i in 0 until arr.length()) {
                    risksList.add(arr.getString(i))
                }
            }
            if (risksList.isEmpty()) risksList.addAll(generateDeterministicExplanation(gapResult, policies).risks)

            val nextStep = obj.optString(
                "suggestedNextStep",
                "Review term insurance quotes to bridge your cover gap while investing premium savings."
            )

            val glossaryList = mutableListOf<AiGlossaryItem>()
            obj.optJSONArray("glossary")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val item = arr.getJSONObject(i)
                    glossaryList.add(
                        AiGlossaryItem(
                            term = item.optString("term"),
                            definition = item.optString("definition")
                        )
                    )
                }
            }
            if (glossaryList.isEmpty()) glossaryList.addAll(generateDeterministicExplanation(gapResult, policies).glossary)

            val disclaimer = obj.optString(
                "disclaimer",
                "This analysis is illustrative only and does not constitute formal legal, tax or financial advice."
            )

            AiExplanation(
                headline = headline,
                keyInsights = insightsList,
                risks = risksList,
                suggestedNextStep = nextStep,
                glossary = glossaryList,
                disclaimer = disclaimer,
                isAiGenerated = true
            )
        } catch (e: Exception) {
            generateDeterministicExplanation(gapResult, policies)
        }
    }

    /**
     * High-fidelity deterministic explainer that guarantees accurate insights even offline.
     */
    fun generateDeterministicExplanation(
        gapResult: ProtectionGapResult,
        policies: List<PolicyAnalysisResult>
    ): AiExplanation {
        val pureTerm = policies.firstOrNull { it.product.type == PolicyType.PURE_TERM }
        val endowment = policies.firstOrNull { it.product.type == PolicyType.ENDOWMENT }
        val trop = policies.firstOrNull { it.product.type == PolicyType.TROP }

        val headline = if (gapResult.isUnderInsured) {
            "Protection Shortfall: You Need ${FinancialEngine.formatIndianCurrency(gapResult.netProtectionGap)} Additional Cover"
        } else {
            "Healthy Coverage: Existing Cover Meets Human Life Value Benchmark"
        }

        val insights = mutableListOf<String>()
        insights.add(
            "Your recommended insurance cover is ${FinancialEngine.formatIndianCurrency(gapResult.recommendedCover)}, based on ${gapResult.workingYearsLeft} remaining earning years until retirement."
        )

        if (pureTerm != null && endowment != null) {
            insights.add(
                "Pure Term offers ${pureTerm.coverPerRupee.toInt()}x cover per rupee of premium, compared to only ${endowment.coverPerRupee.toInt()}x for traditional endowment savings plans."
            )
        } else {
            insights.add("Pure Term insurance provides maximum family security at the lowest annual cost.")
        }

        if (endowment?.btidCorpus != null) {
            val corpusStr = FinancialEngine.formatIndianCurrency(endowment.btidCorpus)
            insights.add(
                "Buying Pure Term and investing the difference in premium can yield an estimated corpus of $corpusStr, vastly outperforming the endowment maturity benefit."
            )
        } else {
            insights.add(
                "Separating protection from investment ensures both adequate life cover and higher compounded wealth."
            )
        }

        val risks = listOf(
            "Underinsurance risk: In event of an early demise, current assets may fail to replace future lost income (${FinancialEngine.formatIndianCurrency(gapResult.hlvGross)}).",
            "Inflation erosion: Fixed return savings plans often fail to beat retail inflation in India, eroding purchasing power.",
            "Policy lapse risk: Expensive premiums on endowment plans create a high risk of policy surrender before maturity."
        )

        val nextStep = if (gapResult.isUnderInsured) {
            "Apply for a pure term insurance policy of at least ${FinancialEngine.formatIndianCurrency(gapResult.netProtectionGap)} and direct excess savings into diversified equity/debt funds."
        } else {
            "Maintain current coverage and review protection needs whenever family liabilities or incomes increase."
        }

        val glossary = listOf(
            AiGlossaryItem("Human Life Value (HLV)", "The present value of all future earnings your family depends on, minus personal expenses."),
            AiGlossaryItem("Pure Term Plan", "Life insurance that pays a lump sum only on demise during the term, with no investment return."),
            AiGlossaryItem("Return of Premium (TROP)", "A term policy that refunds all paid premiums at maturity, but at a 2x-3x higher annual cost."),
            AiGlossaryItem("IRR (Internal Rate of Return)", "The annualized rate of return earned on cash flows into an investment plan."),
            AiGlossaryItem("NPV (Net Present Value)", "The current worth of future cash inflows and outflows discounted at your cost of capital.")
        )

        return AiExplanation(
            headline = headline,
            keyInsights = insights,
            risks = risks,
            suggestedNextStep = nextStep,
            glossary = glossary,
            disclaimer = "Illustrative calculation tool only. Not SEBI registered or insurance advice.",
            isAiGenerated = false
        )
    }

    private fun buildFinancialPayload(
        gapResult: ProtectionGapResult,
        policies: List<PolicyAnalysisResult>,
        userAge: Int,
        retirementAge: Int
    ): String {
        val root = JSONObject().apply {
            put("userAge", userAge)
            put("retirementAge", retirementAge)
            put("workingYearsRemaining", gapResult.workingYearsLeft)
            put("humanLifeValue", gapResult.hlvGross)
            put("liabilities", gapResult.liabilities)
            put("futureGoalsPV", gapResult.goalsTotal)
            put("liquidAssetsOffset", gapResult.liquidAssetsOffset)
            put("recommendedCover", gapResult.recommendedCover)
            put("existingCover", gapResult.existingCover)
            put("protectionGap", gapResult.netProtectionGap)
            put("coverageRatioPct", gapResult.coverageRatioPct)

            val policiesArr = JSONArray()
            policies.forEach { p ->
                val pObj = JSONObject().apply {
                    put("name", p.product.name)
                    put("type", p.product.type.name)
                    put("annualPremium", p.product.annualPremium)
                    put("termYears", p.product.termYears)
                    put("sumAssured", p.product.sumAssured)
                    put("maturityBenefit", p.product.maturityBenefit)
                    put("irrPct", p.irrPct ?: 0.0)
                    put("npv", p.npv)
                    put("coverPerRupee", p.coverPerRupee)
                    put("btidAccumulatedCorpus", p.btidCorpus ?: 0.0)
                }
                policiesArr.put(pObj)
            }
            put("policies", policiesArr)
        }
        return root.toString(2)
    }

    /**
     * Smart policy text parser for "Paste / Upload Policy Details" feature.
     * Extracts premium, sum assured, term, and maturity benefit from unstructured text or quotes.
     */
    fun extractPolicyFromText(text: String): ExtractedPolicyData {
        var premium = 0.0
        var sumAssured = 0.0
        var term = 25
        var maturity = 0.0

        val lower = text.lowercase()

        // Extract numbers with Crore / Lakh or direct numbers:
        fun parseAmount(str: String): Double {
            val clean = str.replace(",", "").replace("₹", "").trim()
            val num = clean.filter { it.isDigit() || it == '.' }.toDoubleOrNull() ?: return 0.0
            return when {
                str.contains("cr", ignoreCase = true) || str.contains("crore", ignoreCase = true) -> num * 1_00_00_000.0
                str.contains("lac", ignoreCase = true) || str.contains("lakh", ignoreCase = true) -> num * 1_00_000.0
                else -> num
            }
        }

        // Search for Sum Assured:
        val saRegex = Regex("""(?:sum assured|life cover|cover amount|sum assured of|coverage)[:\s\-₹]+([0-9.,\s]+(?:crore|cr|lakh|lac)?)""", RegexOption.IGNORE_CASE)
        saRegex.find(text)?.let {
            sumAssured = parseAmount(it.groupValues[1])
        }

        // Search for Premium:
        val premRegex = Regex("""(?:annual premium|premium amount|yearly premium|premium)[:\s\-₹]+([0-9.,\s]+(?:lakh|lac|k)?)""", RegexOption.IGNORE_CASE)
        premRegex.find(text)?.let {
            premium = parseAmount(it.groupValues[1])
        }

        // Search for Term:
        val termRegex = Regex("""(?:policy term|term|period)[:\s\-]+([0-9]{1,2})\s*(?:years|yrs)?""", RegexOption.IGNORE_CASE)
        termRegex.find(text)?.let {
            term = it.groupValues[1].toIntOrNull() ?: 25
        }

        // Search for Maturity Benefit:
        val matRegex = Regex("""(?:maturity benefit|maturity value|guaranteed maturity|returns at maturity)[:\s\-₹]+([0-9.,\s]+(?:crore|cr|lakh|lac)?)""", RegexOption.IGNORE_CASE)
        matRegex.find(text)?.let {
            maturity = parseAmount(it.groupValues[1])
        }

        // Defaults if not captured:
        if (sumAssured == 0.0 && lower.contains("1 crore")) sumAssured = 1_00_00_000.0
        if (sumAssured == 0.0 && lower.contains("50 lakh")) sumAssured = 50_00_000.0

        return ExtractedPolicyData(
            premium = if (premium > 0) premium else 24_000.0,
            sumAssured = if (sumAssured > 0) sumAssured else 1_00_00_000.0,
            termYears = if (term in 5..50) term else 30,
            maturityBenefit = maturity
        )
    }

    data class ExtractedPolicyData(
        val premium: Double,
        val sumAssured: Double,
        val termYears: Int,
        val maturityBenefit: Double
    )
}
