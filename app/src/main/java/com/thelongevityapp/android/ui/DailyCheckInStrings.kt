package com.thelongevityapp.android.ui

import androidx.annotation.StringRes
import com.thelongevityapp.android.R
import com.thelongevityapp.android.data.AnswerValue

/** Maps daily check-in question id and option value to string resource ids for localization. */
object DailyCheckInStrings {

    @StringRes
    fun promptResId(id: String): Int = when (id) {
        "sleep" -> R.string.daily_prompt_sleep
        "movement" -> R.string.daily_prompt_movement
        "foodQuality" -> R.string.daily_prompt_food_quality
        "sugar" -> R.string.daily_prompt_sugar
        "stress" -> R.string.daily_prompt_stress
        "mentalLoad" -> R.string.daily_prompt_mental_load
        "moodSocial" -> R.string.daily_prompt_mood_social
        "physicalWellbeing" -> R.string.daily_prompt_physical_wellbeing
        "recoveryStatus" -> R.string.daily_prompt_recovery_status
        "selfCare" -> R.string.daily_prompt_self_care
        else -> R.string.daily_prompt_sleep
    }

    @StringRes
    fun optionResId(value: AnswerValue): Int = when (value) {
        AnswerValue.MINUS_1 -> R.string.daily_option_very_poor
        AnswerValue.MINUS_HALF -> R.string.daily_option_below_average
        AnswerValue.ZERO -> R.string.daily_option_neutral
        AnswerValue.PLUS_HALF -> R.string.daily_option_good
        AnswerValue.PLUS_1 -> R.string.daily_option_excellent
    }
}
