# ProtectionIQ - Financial Decision Tool for Life Insurance in India

ProtectionIQ is an actuarial decision engine built for individuals and families in India to determine how much life insurance cover they truly need and evaluate whether an insurance policy offers good financial value.

---

## Why Calculations Are Kept Out of the LLM

In financial planning, **determinism and precision are non-negotiable**. Large Language Models (LLMs) are probabilistic token generators:
1. **Hallucination & Math Drift:** LLMs often approximate compounding equations ($PV = \frac{FV}{(1+r)^t}$), drop exponent terms, or invent interest rates during iterative token generation.
2. **Auditability & Regulatory Safety:** In India, financial metrics must be verifiable against standard actuarial formulas. By executing all equations in strict Kotlin/TypeScript math functions, every rupee output is deterministic, unit-tested, and reproducible down to the decimal point.
3. **Role of Gemini AI:** The Gemini API (`gemini-3.5-flash`) is used **strictly for natural language synthesis, risk highlights, and plain-English translation** of the pre-computed figures. The LLM receives the numbers as a read-only JSON payload and is explicitly instructed:
   > *"Use ONLY the numbers provided. Never recompute or invent figures. Use simple language. Do not give legal, tax or financial advice. End with a one-line disclaimer."*

---

## Financial Formulas & Mathematical Methodology

### Module 1: Human Life Value (HLV) & Protection Gap

The **Human Life Value (HLV)** method calculates the economic loss a family would suffer in the event of the earning member's premature demise:

$$HLV = \sum_{t=1}^{N} \frac{\text{Income}_t - \text{PersonalExpenses}_t}{(1 + r)^t}$$

Where:
- $N$: Number of remaining working years ($\text{Retirement Age} - \text{Current Age}$)
- $\text{Income}_t = \text{Annual Income} \times (1 + g)^t$, with $g$ = income growth rate
- $\text{PersonalExpenses}_t = (\text{Income} \times \text{Personal Expense Ratio}) \times (1 + i)^t$, with $i$ = inflation rate
- $r$: Discount rate (cost of capital / risk-free benchmark)

**Recommended Cover:**
$$\text{Recommended Cover} = \max(0, HLV + \text{Outstanding Liabilities} + \text{PV of Financial Goals} - \text{Liquid Savings})$$

**Net Protection Gap:**
$$\text{Net Protection Gap} = \max(0, \text{Recommended Cover} - \text{Existing Life Cover Held})$$

---

### Module 2: Policy Value Analyzer & BTID (Buy Term + Invest Difference)

1. **Internal Rate of Return (IRR):**
   Computed using the Newton-Raphson / Bisection algorithm to find rate $r$ such that:
   $$\text{NPV} = \sum_{t=0}^{T} \frac{CF_t}{(1 + r)^t} = 0$$
   where $CF_t = -\text{Annual Premium}$ during the paying term, and $CF_T = +\text{Maturity Benefit}$ at maturity.

2. **Net Present Value (NPV):**
   $$\text{NPV} = \sum_{t=0}^{T} \frac{CF_t}{(1 + \text{Discount Rate})^t}$$

3. **Cover-per-Rupee Leverage:**
   $$\text{Cover Leverage} = \frac{\text{Sum Assured}}{\text{Annual Premium}}$$
   *Observation:* Pure term insurance delivers **500x – 800x** cover per rupee, whereas traditional endowment policies typically provide only **15x – 25x**.

4. **Buy Term + Invest the Difference (BTID) Strategy:**
   Instead of paying a high premium for an endowment or Return of Premium (TROP) plan:
   - Buy Pure Term for protection ($\text{Premium}_{\text{Term}}$).
   - Annually invest the difference ($\Delta P = \text{Premium}_{\text{Endowment}} - \text{Premium}_{\text{Term}}$) at the expected investment return $k$.
   - Future Wealth Corpus at term $T$:
     $$\text{Corpus}_{\text{BTID}} = \sum_{t=1}^{T} \Delta P \times (1 + k)^{T - t + 1}$$

---

### Module 3: Scenario & Sensitivity Engine (Tornado Chart)

Assumptions are varied by $\pm 2.0\%$ to measure which economic assumption produces the greatest swing in recommended cover:
- **Discount Rate ($r$):** Directly alters the denominator discount factor $(1+r)^t$.
- **Income Growth ($g$):** Alters the annual cash stream growth.
- **Inflation ($i$):** Alters expense expansion and education goal costs.
- **Investment Return ($k$):** Alters the BTID terminal corpus.

**Variance Alerts:**
- **Severe Underinsurance Alert:** Flagged when $\frac{\text{Existing Cover}}{\text{Recommended Cover}} < 50\%$.
- **Macro Variance Alert:** Flagged when any scenario changes recommended cover by $> 15\%$ against the base benchmark.
- **Negative Real Return:** Flagged when an endowment policy's IRR is below the long-term retail inflation rate.

---

## Currency & Numbering

All figures are formatted in the **Indian Numbering System** with the Rupee symbol (`₹`):
- $\ge 1\text{ Crore} \rightarrow \text{₹X.XX Cr}$
- $\ge 1\text{ Lakh} \rightarrow \text{₹X.XX L}$
- Grouping follows Indian comma conventions: `₹15,00,000`.

---

## Privacy Architecture

- **Session Security:** "Data stays in your browser session and is not stored."
- No personal financial information is ever transmitted to third-party databases.
- The Gemini API receives only anonymized numerical metrics strictly when requested by the user.
