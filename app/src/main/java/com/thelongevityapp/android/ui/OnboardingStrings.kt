package com.thelongevityapp.android.ui

import androidx.annotation.StringRes
import com.thelongevityapp.android.R

/** Maps onboarding question id and option index (0-4) to string resource ids for localization. */
object OnboardingStrings {

    @StringRes
    fun promptResId(questionId: String): Int = when (questionId) {
        "sleep" -> R.string.onboarding_sleep_prompt
        "activity" -> R.string.onboarding_activity_prompt
        "muscle" -> R.string.onboarding_muscle_prompt
        "visceralFat" -> R.string.onboarding_visceral_fat_prompt
        "nutritionPattern" -> R.string.onboarding_nutrition_prompt
        "sugar" -> R.string.onboarding_sugar_prompt
        "stress" -> R.string.onboarding_stress_prompt
        "smokingAlcohol" -> R.string.onboarding_smoking_alcohol_prompt
        "metabolicHealth" -> R.string.onboarding_metabolic_prompt
        "energyFocus" -> R.string.onboarding_energy_focus_prompt
        else -> R.string.onboarding_sleep_prompt
    }

    @StringRes
    fun optionResId(questionId: String, optionIndex: Int): Int {
        val prefix = when (questionId) {
            "sleep" -> "onboarding_sleep"
            "activity" -> "onboarding_activity"
            "muscle" -> "onboarding_muscle"
            "visceralFat" -> "onboarding_visceral_fat"
            "nutritionPattern" -> "onboarding_nutrition"
            "sugar" -> "onboarding_sugar"
            "stress" -> "onboarding_stress"
            "smokingAlcohol" -> "onboarding_smoking_alcohol"
            "metabolicHealth" -> "onboarding_metabolic"
            "energyFocus" -> "onboarding_energy_focus"
            else -> "onboarding_sleep"
        }
        return when (optionIndex) {
            0 -> when (prefix) {
                "onboarding_sleep" -> R.string.onboarding_sleep_opt_0
                "onboarding_activity" -> R.string.onboarding_activity_opt_0
                "onboarding_muscle" -> R.string.onboarding_muscle_opt_0
                "onboarding_visceral_fat" -> R.string.onboarding_visceral_fat_opt_0
                "onboarding_nutrition" -> R.string.onboarding_nutrition_opt_0
                "onboarding_sugar" -> R.string.onboarding_sugar_opt_0
                "onboarding_stress" -> R.string.onboarding_stress_opt_0
                "onboarding_smoking_alcohol" -> R.string.onboarding_smoking_alcohol_opt_0
                "onboarding_metabolic" -> R.string.onboarding_metabolic_opt_0
                "onboarding_energy_focus" -> R.string.onboarding_energy_focus_opt_0
                else -> R.string.onboarding_sleep_opt_0
            }
            1 -> when (prefix) {
                "onboarding_sleep" -> R.string.onboarding_sleep_opt_1
                "onboarding_activity" -> R.string.onboarding_activity_opt_1
                "onboarding_muscle" -> R.string.onboarding_muscle_opt_1
                "onboarding_visceral_fat" -> R.string.onboarding_visceral_fat_opt_1
                "onboarding_nutrition" -> R.string.onboarding_nutrition_opt_1
                "onboarding_sugar" -> R.string.onboarding_sugar_opt_1
                "onboarding_stress" -> R.string.onboarding_stress_opt_1
                "onboarding_smoking_alcohol" -> R.string.onboarding_smoking_alcohol_opt_1
                "onboarding_metabolic" -> R.string.onboarding_metabolic_opt_1
                "onboarding_energy_focus" -> R.string.onboarding_energy_focus_opt_1
                else -> R.string.onboarding_sleep_opt_1
            }
            2 -> when (prefix) {
                "onboarding_sleep" -> R.string.onboarding_sleep_opt_2
                "onboarding_activity" -> R.string.onboarding_activity_opt_2
                "onboarding_muscle" -> R.string.onboarding_muscle_opt_2
                "onboarding_visceral_fat" -> R.string.onboarding_visceral_fat_opt_2
                "onboarding_nutrition" -> R.string.onboarding_nutrition_opt_2
                "onboarding_sugar" -> R.string.onboarding_sugar_opt_2
                "onboarding_stress" -> R.string.onboarding_stress_opt_2
                "onboarding_smoking_alcohol" -> R.string.onboarding_smoking_alcohol_opt_2
                "onboarding_metabolic" -> R.string.onboarding_metabolic_opt_2
                "onboarding_energy_focus" -> R.string.onboarding_energy_focus_opt_2
                else -> R.string.onboarding_sleep_opt_2
            }
            3 -> when (prefix) {
                "onboarding_sleep" -> R.string.onboarding_sleep_opt_3
                "onboarding_activity" -> R.string.onboarding_activity_opt_3
                "onboarding_muscle" -> R.string.onboarding_muscle_opt_3
                "onboarding_visceral_fat" -> R.string.onboarding_visceral_fat_opt_3
                "onboarding_nutrition" -> R.string.onboarding_nutrition_opt_3
                "onboarding_sugar" -> R.string.onboarding_sugar_opt_3
                "onboarding_stress" -> R.string.onboarding_stress_opt_3
                "onboarding_smoking_alcohol" -> R.string.onboarding_smoking_alcohol_opt_3
                "onboarding_metabolic" -> R.string.onboarding_metabolic_opt_3
                "onboarding_energy_focus" -> R.string.onboarding_energy_focus_opt_3
                else -> R.string.onboarding_sleep_opt_3
            }
            4 -> when (prefix) {
                "onboarding_sleep" -> R.string.onboarding_sleep_opt_4
                "onboarding_activity" -> R.string.onboarding_activity_opt_4
                "onboarding_muscle" -> R.string.onboarding_muscle_opt_4
                "onboarding_visceral_fat" -> R.string.onboarding_visceral_fat_opt_4
                "onboarding_nutrition" -> R.string.onboarding_nutrition_opt_4
                "onboarding_sugar" -> R.string.onboarding_sugar_opt_4
                "onboarding_stress" -> R.string.onboarding_stress_opt_4
                "onboarding_smoking_alcohol" -> R.string.onboarding_smoking_alcohol_opt_4
                "onboarding_metabolic" -> R.string.onboarding_metabolic_opt_4
                "onboarding_energy_focus" -> R.string.onboarding_energy_focus_opt_4
                else -> R.string.onboarding_sleep_opt_4
            }
            else -> R.string.onboarding_sleep_opt_0
        }
    }
}
