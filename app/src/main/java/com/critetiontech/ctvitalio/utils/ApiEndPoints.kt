package com.critetiontech.ctvitalio.utils

/**
 * ApiEndPoints.kt
 *
 * Single source of truth for ALL API endpoint paths used in the app.
 * Previously split across ApiEndPoint.kt (class) and ApiEndPointCorporateModule.kt (class).
 *
 * Consolidated into a single object with logical grouping by module/feature.
 * Reference via: ApiEndPoints.Auth.LOGIN, ApiEndPoints.Vital.INSERT, etc.
 */
object ApiEndPoints {

    // ─── Authentication & OTP ────────────────────────────────────────────────
    object Auth {
        const val SEND_OTP    = "api/LogInForVitalioApp/SentLogInOTPForVitalioApp"
        const val VERIFY_OTP  = "api/LogInForVitalioApp/VerifyLogInOTPForVitalioApp"
        const val LOGOUT      = "api/LogInForVitalioApp/LogOutOTPForVitalioApp"

        const val CORPORATE_LOGIN           = "api/CorporateEmployee/CorporateEmployeeLogin"
        const val EMPLOYEE_RESET_PASSWORD   = "api/CorporateEmployee/EmployeeResetPassowrdLink"
        const val CORPORATE_CHANGE_PASSWORD = "api/CorporateEmployee/CorporateEmployeeChangePassword"
        const val CORPORATE_FORGOT_PASSWORD = "api/CorporateEmployee/CorporateEmployeeForgotPassword"
    }

    // ─── Patient / Registration ───────────────────────────────────────────────
    object Patient {
        const val SIGN_UP              = "api/PatientRegistration/PatientSignUp"
        const val UPDATE_PROFILE       = "api/PatientRegistration/UpdatePatientProfile"
        const val GET_BY_MOBILE        = "api/PatientRegistration/GetPatientDetailsByMobileNo"
        const val PIN_AUDIT            = "api/PatientRegistration/EmployeePinAudit"
        const val DELETE_GOAL          = "api/PatientRegistration/DeleteEmployeeGoal"

        // Location lookups
        const val GET_STATE_BY_COUNTRY = "api/StateMaster/GetStateMasterByCountryId"
        const val GET_CITY_BY_STATE    = "api/CityMaster/GetCityMasterByStateId"
    }

    // ─── Allergy ──────────────────────────────────────────────────────────────
    object Allergy {
        const val GET_ALLERGIES    = "api/PatientIPDPrescription/PatientAllergies"
        const val SAVE_ALLERGIES   = "api/PatientIPDPrescription/SavePatientAllergies"
        const val DELETE_ALLERGIES = "api/PatientIPDPrescription/DeletePatientAllergies"
        const val HISTORY_CATEGORY = "api/HistorySubCategory/GetHistorySubCategoryMasterById"
    }

    // ─── Problems / Symptoms ──────────────────────────────────────────────────
    object Symptoms {
        const val GET_PROBLEM_LIST       = "api/KnowMedApis/GetProblemList"
        const val GET_PROBLEMS_WITH_ICON = "Patient/getProblemsWithIcon"
        const val GET_ALL_SUGGESTED      = "Patient/getAllSuggestedProblem"
        const val GET_ALL_PROBLEMS       = "Patient/getAllProblems"
        const val INSERT_SYMPTOMS        = "api/PatientIPDPrescription/InsertSymtoms"
        const val GET_SYMPTOMS           = "api/PatientIPDPrescription/GetSymtoms"
    }

    // ─── Vitals ───────────────────────────────────────────────────────────────
    object Vital {
        const val GET_LAST_VITAL    = "api/UltrahumanVitals/GetUltrahumanVitalsByPid"
        const val INSERT_VITAL      = "api/PatientVital/InsertPatientVital"
        const val GET_VITAL_GRAPH   = "api/PatientVital/GetPatientVitalGraph"
        const val INSERT_API_TOKEN  = "api/APITokens/InsertAPITokens"

        const val GET_BLOOD_PRESSURE_BY_PID = "api/UltrahumanVitals/GetBloodPressureDetailsByPid"
        const val GET_GLUCOSE_BY_PID        = "api/UltrahumanVitals/GetGlucoseDetailsByPid"
    }

    // ─── Medication ───────────────────────────────────────────────────────────
    object Medication {
        const val GET_MEDICATION    = "api/EmployeeMedicineIntake/GetEmployeeMedicineIntake"
        const val INSERT_MEDICATION = "api/EmployeeMedicineIntake/InsertEmployeeMedicineIntake"
        const val GET_FREQUENCY     = "api/KnowMedApis/GetFrequencyList"
    }

    // ─── Investigation / Reports ──────────────────────────────────────────────
    object Report {
        const val INSERT_RESULT     = "api/InvestigationByPatient/InsertResult"
        const val INSERT_MEDIA_DATA = "api/PatientMediaData/InsertPatientMediaData"
        const val GET_MEDIA_DATA    = "api/PatientMediaData/GetPatientMediaData"
    }

    // ─── Fluid Intake / Output ────────────────────────────────────────────────
    object Fluid {
        const val GET_INTAKE_LIST   = "api/ManualFoodAssign/GetManualFoodAssignList"
        const val GET_INTAKE_RANGE  = "api/ManualFoodAssign/FluidSummaryByDateRange"
        const val GET_OUTPUT_RANGE  = "api/output/OutputSummaryByDateRange"
        const val SAVE_OUTPUT       = "api/v1/output/SavePatientOutput"
        const val GET_OUTPUT_DAILY  = "api/output/GetPatientOutputList"
        const val INSERT_FOOD_INTAKE= "api/FoodIntake/InsertFoodIntake"
        const val GET_FOOD_INTAKE   = "api/FoodIntake/GetFoodIntake"
        const val INTAKE_BY_DIET_ID = "api/FoodIntake/IntakeByDietID"
    }

    // ─── Emergency Contact ────────────────────────────────────────────────────
    object Emergency {
        const val GET    = "api/EmergencyContact/GetEmergencyContactByPid"
        const val SAVE   = "api/EmergencyContact/InsertEmergencyContact"
        const val DELETE = "api/EmergencyContact/RemoveEmergencyContact"
    }

    // ─── Smart Watch ──────────────────────────────────────────────────────────
    object Watch {
        const val INSERT = "api/WatchDetails/InsertWatchDetails"
        const val GET    = "api/WatchDetails/GetWatchDetails"
        const val DELETE = "api/WatchDetails/DeleteWatchDetails"
    }

    // ─── Goals ────────────────────────────────────────────────────────────────
    object Goal {
        const val GET_EMPLOYEE_SMART_GOALS = "api/EmployeeGoals/GetEmployeeeSmartGoals"
        const val GET_ALL_GOALS            = "api/EmployeeGoals/GetAllSmartGoals"
    }

    // ─── Chat ─────────────────────────────────────────────────────────────────
    object Chat {
        const val SEND = "SaveUserChat"
        const val GET  = "GetUserChat"
    }

    // ─── Corporate Module ─────────────────────────────────────────────────────
    object Corporate {
        const val GET_JOINED_CHALLENGES    = "api/CorporateChallenges/GetJoinedChallengesByEmployeeId"
        const val GET_NEW_CHALLENGES       = "api/CorporateChallenges/GetCorporateChallengesByClientId"
        const val GET_JOINED_DETAILS       = "api/CorporateChallenges/GetJoinedChallengesDetailsByEmployeeId"

        const val INSERT_PARTICIPANT       = "api/Challengeparticipants/InsertChallengeparticipants"
        const val LEAVE_PARTICIPANT        = "api/Challengeparticipants/LeaveChallengeparticipants"

        const val INSERT_MOOD              = "api/MoodTracker/InsertMood"
        const val GET_MOOD_BY_PID          = "api/MoodTracker/GetMoodByPid"
        const val GET_ALL_MOODS            = "api/MoodTracker/GetAllMoods"

        const val INSERT_ENERGY_TANK       = "api/EnergyTankMaster/InsertEnergyTankMaster"
        const val GET_ALL_ENERGY_TANK      = "api/EnergyTankMaster/GetAllEnergyTankMaster"

        const val GET_ALL_EMPLOYEE_ACTIVITY= "api/EmployeeActivity/GetAllEmployeeActivity"
        const val INSERT_EMPLOYEE_ACTIVITY = "api/EmployeeActivity/InsertEmployeeActivity"
        const val GET_ALL_ACTIVITY_MASTER  = "api/ActivityMaster/GetAllActivityMaster"

        const val GET_MINDFULNESS          = "api/CorporateMindfulness/GetCorporateMindfulness"
    }
}
