package com.example.healthyme.domain

enum class ActivityContext {
    RESTING,
    LIGHT_ACTIVITY,
    INTENSE_ACTIVITY
}

object HealthInterpretation {

    data class Result(
        val status: String,
        val message: String
    )

    fun interpretHydration(hydrationMl: Int): Result {

        val liters = hydrationMl / 1000.0

        return when {

            liters < 2.0 -> Result(
                status = "Below recommended range",
                message = "You're a little low on water today. Try to drink more throughout the day."
            )

            liters in 2.7..3.7 -> Result(
                status = "Adequate hydration",
                message = "You're doing well with hydration today. Keep it up!"
            )

            liters > 4.0 -> Result(
                status = "Above typical range",
                message = "You've had quite a lot of water today. Keep your intake balanced."
            )

            else -> Result(
                status = "Moderate hydration",
                message = "You're making progress. Keep drinking water regularly throughout the day."
            )
        }
    }

    fun interpretSleep(totalMinutes: Long): Result {

        val hours = totalMinutes / 60.0

        return when {

            hours < 6.0 -> Result(
                status = "Low sleep",
                message = "You didn't get much sleep last night. Try to give yourself more time to rest tonight."
            )

            hours in 7.0..9.0 -> Result(
                status = "Recommended sleep",
                message = "You got a healthy amount of sleep. Nice job giving your body time to recover."
            )

            hours > 10.0 -> Result(
                status = "Long sleep duration",
                message = "You slept longer than usual. Pay attention to how rested you feel today."
            )

            else -> Result(
                status = "Moderate sleep",
                message = "Your sleep is close to the recommended range. A little more consistent rest may help."
            )
        }
    }

    fun interpretRestingHeartRate(bpm: Int): Result {

        return when {

            bpm in 60..100 -> Result(
                status = "Normal resting range",
                message = "Your resting heart rate is within a typical range."
            )

            bpm < 60 -> Result(
                status = "Below resting range",
                message = "Your resting heart rate is lower than the usual range."
            )

            else -> Result(
                status = "Above resting range",
                message = "Your resting heart rate is higher than the usual range."
            )
        }
    }

    fun isHydrationAdequate(
        hydrationMl: Int
    ): Boolean {

        return hydrationMl in 2700..3700
    }

    fun interpretHeartRate(
        bpm: Int,
        activityContext: ActivityContext
    ): Result {

        return when (activityContext) {

            ActivityContext.RESTING -> {

                if (bpm in 60..100) {
                    Result(
                        status = "Within resting range",
                        message = "Your heart rate is within the expected range while resting."
                    )
                } else if (bpm < 60) {
                    Result(
                        status = "Below resting range",
                        message = "Your heart rate is below the usual resting range."
                    )
                } else {
                    Result(
                        status = "Above resting range",
                        message = "Your heart rate is above the usual resting range."
                    )
                }
            }

            ActivityContext.LIGHT_ACTIVITY -> {

                if (bpm in 90..120) {
                    Result(
                        status = "Within light activity range",
                        message = "Your heart rate is within the expected range for light activity."
                    )
                } else if (bpm < 90) {
                    Result(
                        status = "Below light activity range",
                        message = "Your heart rate is below the usual range for light activity."
                    )
                } else {
                    Result(
                        status = "Above light activity range",
                        message = "Your heart rate is above the usual range for light activity."
                    )
                }
            }

            ActivityContext.INTENSE_ACTIVITY -> {

                if (bpm in 120..160) {
                    Result(
                        status = "Within intense activity range",
                        message = "Your heart rate is within the expected range for intense activity."
                    )
                } else if (bpm < 120) {
                    Result(
                        status = "Below intense activity range",
                        message = "Your heart rate is below the usual range for intense activity."
                    )
                } else {
                    Result(
                        status = "Above intense activity range",
                        message = "Your heart rate is above the usual range for intense activity."
                    )
                }
            }
        }
    }
}