package com.criterion.nativevitalio.Utils

class ApiEndPoint {

    val digiDoctorBaseUrl="http://52.172.134.222:205/api/v1.0/"

    val getPatientDetailsByMobileNo = "api/PatientRegistration/GetPatientDetailsByMobileNo"
    val sentLogInOTPForSHFCApp="api/LogInForSHFCApp/SentLogInOTPForSHFCApp"
    val verifyLogInOTPForSHFCApp= "api/LogInForSHFCApp/VerifyLogInOTPForSHFCApp"



    val getAllPatientMedication="api/PatientMedication/GetAllPatientMedication"
    val insertPatientMedication="api/PatientMedication/InsertPatientMedication"



    val getProblemsWithIcon="Patient/getProblemsWithIcon"
    val getAllSuggestedProblem="Patient/getAllSuggestedProblem"
    val getAllProblems="Patient/getAllProblems"
    val insertSymtoms="api/PatientIPDPrescription/InsertSymtoms"
    val getSymptoms="api/PatientIPDPrescription/GetSymtoms"
}