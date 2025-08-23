package com.example.dive_app.util

import android.util.Log

data class SpeciesProfile(
    val temp: Pair<Double, Double>,
    val current: Pair<Double, Double>,
    val hours: List<Pair<Int, Int>>,
    val mul: List<Pair<Int, Int>>,
    val months: List<Int>
)

data class SpeciesScore(
    val name: String,
    val total: Int
)

object FishingAnalyzer {

    // 어종별 조건 정의
    private val speciesProfiles = mapOf(
        "붕장어" to SpeciesProfile(
            temp = 15.0 to 20.0,
            current = 0.3 to 2.5,
            hours = listOf(4 to 8, 17 to 20),
            mul = listOf(2 to 10, 11 to 13),
            months = listOf(8)
        ),
        "숭어" to SpeciesProfile(
            temp = 15.0 to 20.0,
            current = 0.3 to 2.5,
            hours = listOf(4 to 8, 17 to 20),
            mul = listOf(2 to 10, 11 to 13),
            months = listOf(8)
        ),
        "감성돔" to SpeciesProfile(
            temp = 15.0 to 20.0,
            current = 0.3 to 0.8,
            hours = listOf(5 to 8, 17 to 20),
            mul = listOf(2 to 5, 11 to 13),
            months = listOf(3, 4, 5, 9, 10, 11)
        ),
        "벵에돔" to SpeciesProfile(
            temp = 20.0 to 26.0,
            current = 0.5 to 1.0,
            hours = listOf(4 to 9, 13 to 16),
            mul = listOf(3 to 6, 12 to 12),
            months = listOf(6, 7, 8, 9)
        ),
        "고등어" to SpeciesProfile(
            temp = 15.0 to 22.0,
            current = 0.5 to 1.5,
            hours = listOf(5 to 8, 17 to 19),
            mul = listOf(2 to 7),
            months = listOf(4, 5, 6, 7, 8, 9, 10)
        ),
        "농어" to SpeciesProfile(
            temp = 15.0 to 22.0,
            current = 0.3 to 1.0,
            hours = listOf(20 to 23, 0 to 2, 4 to 7),
            mul = listOf(2 to 6, 10 to 13),
            months = listOf(3, 4, 5, 9, 10, 11, 12)
        ),
        "루어" to SpeciesProfile(
            temp = 16.0 to 24.0,
            current = 0.5 to 1.0,
            hours = listOf(4 to 8, 18 to 23),
            mul = listOf(2 to 6, 11 to 13),
            months = listOf(3, 4, 5, 9, 10)
        )
    )

    // ---------------- 점수 계산 함수 ----------------

    private fun scoreRange(x: Double, lo: Double, hi: Double, slack: Double): Int {
        return when {
            x in lo..hi -> 2
            x >= lo - slack && x <= hi + slack -> 1
            else -> 0
        }
    }

    private fun scoreHour(hour: Int, ranges: List<Pair<Int,Int>>): Int {
        val h = hour % 24
        for ((a,b) in ranges) {
            if (h in a..b) return 2
            if ((h == (a-1+24)%24) || (h == (b+1)%24)) return 1
        }
        return 0
    }

    private fun wrap15(x: Int) = ((x - 1) % 15) + 1

    private fun scoreMul(mul: Int, ranges: List<Pair<Int,Int>>): Int {
        val m = wrap15(mul)
        for ((a,b) in ranges) {
            if (m in a..b) return 2
            if (wrap15(m-1) in a..b || wrap15(m+1) in a..b) return 1
        }
        return 0
    }

    private fun scoreMonth(month: Int, months: List<Int>) =
        if (months.contains(month)) 2 else 0

    // ---------------- 메인 함수 ----------------

    fun scoreSpecies(temp: Double, current: Double, hour: Int, mul: Int, month: Int): List<SpeciesScore> {
        val result = mutableListOf<SpeciesScore>()
        for ((name, prof) in speciesProfiles) {
            val sTemp = scoreRange(temp, prof.temp.first, prof.temp.second, 2.0)
            val sCur  = scoreRange(current, prof.current.first, prof.current.second, 0.2)
            val sHr   = scoreHour(hour, prof.hours)
            val sMul  = scoreMul(mul, prof.mul)
            val sMon  = scoreMonth(month, prof.months)
            val total = sTemp + sCur + sHr + sMul + sMon
            result.add(SpeciesScore(name, total))
        }
        return result.sortedByDescending { it.total }
    }

    fun recommendFish(
        target: String,
        temp: Double,
        current: Double,
        hour: Int,
        mul: Int,
        month: Int
    ): String {
        val speciesOnly = extractSpecies(target)
        Log.d("fishTTT", "parsed speciesOnly=$speciesOnly")

        val scores = scoreSpecies(temp, current, hour, mul, month)

        // 점수 목록에서 speciesOnly 어종만 필터링
        val filtered = scores.filter { it.name in speciesOnly }
        Log.d("fishTTT", "parsed speciesOnly=$filtered")

        return filtered
            .sortedByDescending { it.total }
            .take(1)
            .joinToString(", ") { it.name }
    }

    fun extractSpecies(target: String): List<String> {
        // ▶ 다음에 오는 "한글 단어"만 추출 (공백 허용)
        val regex = Regex("▶\\s*([가-힣]+)")
        return regex.findAll(target)
            .map { it.groupValues[1] }
            .toList()
    }
}
