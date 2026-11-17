package com.critetiontech.vitalioCorporate.model

sealed class SymptomItem {
    data class DateHeader(val date: String) : SymptomItem()
    data class Symptom(val detail: SymptomDetail) : SymptomItem()
}