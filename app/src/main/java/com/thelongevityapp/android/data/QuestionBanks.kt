package com.thelongevityapp.android.data

// -1, -0.5, 0, 0.5, 1
enum class AnswerValue(val value: Double) {
    MINUS_1(-1.0),
    MINUS_HALF(-0.5),
    ZERO(0.0),
    PLUS_HALF(0.5),
    PLUS_1(1.0)
}

data class OptionItem(val title: String, val value: AnswerValue)

data class OnboardingQuestion(val id: String, val prompt: String, val options: List<OptionItem>)

object QuestionBanks {
    @Suppress("UNUSED_PARAMETER")
    fun onboardingQuestions(lang: String): List<OnboardingQuestion> = listOf(
        OnboardingQuestion(
            "sleep",
            "How would you rate your typical sleep quality and duration?",
            listOf(
                OptionItem("Very poor (<5 hours, restless)", AnswerValue.MINUS_1),
                OptionItem("Below average (5-6 hours, interrupted)", AnswerValue.MINUS_HALF),
                OptionItem("Neutral (6-7 hours, occasional issues)", AnswerValue.ZERO),
                OptionItem("Good (7-8 hours, mostly restful)", AnswerValue.PLUS_HALF),
                OptionItem("Excellent (8+ hours, consistently deep)", AnswerValue.PLUS_1)
            )
        ),
        OnboardingQuestion(
            "activity",
            "How active is your daily routine?",
            listOf(
                OptionItem("Very sedentary (mostly sitting)", AnswerValue.MINUS_1),
                OptionItem("Light activity (walking <30 min/day)", AnswerValue.MINUS_HALF),
                OptionItem("Moderate (30-60 min movement/day)", AnswerValue.ZERO),
                OptionItem("Active (60+ min exercise/week)", AnswerValue.PLUS_HALF),
                OptionItem("Very active (150+ min/week, strength training)", AnswerValue.PLUS_1)
            )
        ),
        OnboardingQuestion(
            "muscle",
            "How would you assess your muscle mass and strength?",
            listOf(
                OptionItem("Very low (noticeable weakness)", AnswerValue.MINUS_1),
                OptionItem("Below average (struggles with daily tasks)", AnswerValue.MINUS_HALF),
                OptionItem("Average (maintains basic strength)", AnswerValue.ZERO),
                OptionItem("Good (regular strength training)", AnswerValue.PLUS_HALF),
                OptionItem("Excellent (high muscle mass, strong)", AnswerValue.PLUS_1)
            )
        ),
        OnboardingQuestion(
            "visceralFat",
            "How would you describe your body composition, especially around the midsection?",
            listOf(
                OptionItem("High visceral fat (significant belly fat)", AnswerValue.MINUS_1),
                OptionItem("Above average (some excess abdominal fat)", AnswerValue.MINUS_HALF),
                OptionItem("Average (moderate body fat)", AnswerValue.ZERO),
                OptionItem("Good (low body fat, toned)", AnswerValue.PLUS_HALF),
                OptionItem("Excellent (lean, defined abs)", AnswerValue.PLUS_1)
            )
        ),
        OnboardingQuestion(
            "nutritionPattern",
            "How would you rate your overall nutrition pattern?",
            listOf(
                OptionItem("Very poor (mostly processed, fast food)", AnswerValue.MINUS_1),
                OptionItem("Below average (inconsistent, some processed)", AnswerValue.MINUS_HALF),
                OptionItem("Neutral (mixed, some whole foods)", AnswerValue.ZERO),
                OptionItem("Good (mostly whole foods, balanced)", AnswerValue.PLUS_HALF),
                OptionItem("Excellent (whole foods, nutrient-dense, planned)", AnswerValue.PLUS_1)
            )
        ),
        OnboardingQuestion(
            "sugar",
            "How much added sugar and refined carbs do you consume?",
            listOf(
                OptionItem("Very high (daily sweets, sodas, desserts)", AnswerValue.MINUS_1),
                OptionItem("High (several times/week)", AnswerValue.MINUS_HALF),
                OptionItem("Moderate (occasional treats)", AnswerValue.ZERO),
                OptionItem("Low (rarely, mostly natural sources)", AnswerValue.PLUS_HALF),
                OptionItem("Minimal (almost none, whole foods only)", AnswerValue.PLUS_1)
            )
        ),
        OnboardingQuestion(
            "stress",
            "How would you rate your stress levels and management?",
            listOf(
                OptionItem("Very high (chronic stress, overwhelmed)", AnswerValue.MINUS_1),
                OptionItem("High (frequent stress, limited coping)", AnswerValue.MINUS_HALF),
                OptionItem("Moderate (some stress, occasional management)", AnswerValue.ZERO),
                OptionItem("Low (good coping strategies)", AnswerValue.PLUS_HALF),
                OptionItem("Very low (excellent stress management, calm)", AnswerValue.PLUS_1)
            )
        ),
        OnboardingQuestion(
            "smokingAlcohol",
            "Do you smoke or consume alcohol regularly?",
            listOf(
                OptionItem("Heavy (daily smoking or heavy drinking)", AnswerValue.MINUS_1),
                OptionItem("Regular (smoking or drinking several times/week)", AnswerValue.MINUS_HALF),
                OptionItem("Occasional (social drinking, no smoking)", AnswerValue.ZERO),
                OptionItem("Rare (very occasional, minimal)", AnswerValue.PLUS_HALF),
                OptionItem("None (no smoking, no alcohol)", AnswerValue.PLUS_1)
            )
        ),
        OnboardingQuestion(
            "metabolicHealth",
            "How would you assess your metabolic health (energy, blood sugar stability)?",
            listOf(
                OptionItem("Poor (energy crashes, sugar cravings)", AnswerValue.MINUS_1),
                OptionItem("Below average (occasional crashes)", AnswerValue.MINUS_HALF),
                OptionItem("Average (stable most of the time)", AnswerValue.ZERO),
                OptionItem("Good (consistent energy, stable)", AnswerValue.PLUS_HALF),
                OptionItem("Excellent (high energy, no crashes)", AnswerValue.PLUS_1)
            )
        ),
        OnboardingQuestion(
            "energyFocus",
            "How would you rate your daily energy and mental focus?",
            listOf(
                OptionItem("Very low (constant fatigue, brain fog)", AnswerValue.MINUS_1),
                OptionItem("Low (frequent tiredness, poor focus)", AnswerValue.MINUS_HALF),
                OptionItem("Average (moderate energy, decent focus)", AnswerValue.ZERO),
                OptionItem("Good (consistent energy, good focus)", AnswerValue.PLUS_HALF),
                OptionItem("Excellent (high energy, sharp focus)", AnswerValue.PLUS_1)
            )
        )
    )

    val dailyQuestionIds = listOf(
        "sleep", "movement", "foodQuality", "sugar", "stress", "mentalLoad",
        "moodSocial", "physicalWellbeing", "recoveryStatus", "selfCare"
    )

    fun dailyOptions(): List<OptionItem> = listOf(
        OptionItem("Very poor", AnswerValue.MINUS_1),
        OptionItem("Below average", AnswerValue.MINUS_HALF),
        OptionItem("Neutral", AnswerValue.ZERO),
        OptionItem("Good", AnswerValue.PLUS_HALF),
        OptionItem("Excellent", AnswerValue.PLUS_1)
    )
}
