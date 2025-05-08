package com.critetiontech.ctvitalio.model

sealed class SymptomItem {
    data class DateHeader(val date: String) : SymptomItem()
    data class Symptom(val detail: SymptomDetail) : SymptomItem()
}