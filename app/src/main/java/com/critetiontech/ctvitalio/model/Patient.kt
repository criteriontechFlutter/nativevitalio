package com.critetiontech.ctvitalio.model

import com.google.gson.annotations.SerializedName

/**
 * Patient.kt
 *
 * Represents the logged-in employee/patient profile.
 * Moved here from utils/PrefsManager.kt where it was incorrectly placed.
 *
 * This is the single source of truth for patient model. Use PrefsManager
 * to persist/retrieve this object.
 */
data class Patient(
    val pid: String,
    @SerializedName("empName")
    val patientName: String,
    val registrationDate: String,
    val ageUnitId: String,
    val categoryId: String?,
    val createdDate: String,
    val educationalQualificationId: String?,
    val ethinicityId: String?,
    val gender: String,
    val guardianAddress: String,
    val guardianMobileNo: String,
    val guardianName: String,
    val guardianRelationId: String?,
    val height: String,
    val idNumber: String,
    val idTypeId: String,
    val imageURL: String,
    val languageId: String,
    val maritalStatusId: String,
    val occupationId: String?,
    val raceTypeId: String?,
    val refferedFrom: String?,
    val sexualOrientation: String,
    val status: String,
    val uhID: String,
    val userId: String,
    val weight: String,
    val departmentId: String,
    val doctorID: String,
    val patientGender: String,
    val departmentName: String,
    val isCashLess: Boolean,
    val insuranceCompanyId: Int,
    val policyOrCardNumber: String,
    val profileUrl: String,
    var isHoldToSpeak: Int = 0,
    val id: Int,
    val empId: String,
    val mobileNo: String,
    val genderId: Int,
    val age: String,
    val ageType: String,
    val address: String,
    val dob: String,
    val joiningDate: String,
    val countryCallingCode: String,
    val countryId: Int,
    val stateId: Int,
    val zip: String,
    val cityId: Int,
    val emailID: String,
    val bloodGroupId: Int,
    val clientId: Int,
    val isFirstLoginCompleted: Int,
    val leaderboardData: String?,
    val employeegoalsDetails: String
)
