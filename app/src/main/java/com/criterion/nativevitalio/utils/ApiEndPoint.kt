package com.critetiontech.ctvitalio.utils

class ApiEndPoint {

    val digiDoctorBaseUrl="http://52.172.134.222:205/api/v1.0/"

    val getPatientDetailsByMobileNo = "api/PatientRegistration/GetPatientDetailsByMobileNo"
    val sentLogInOTPForVitalioApp="api/LogInForVitalioApp/SentLogInOTPForVitalioApp"
    val verifyLogInOTPForVitalioApp= "api/LogInForVitalioApp/VerifyLogInOTPForVitalioApp"
    val logoutFromApp= "api/LogInForVitalioApp/LogOutOTPForVitalioApp"



    val getAllPatientMedication="api/PatientMedication/GetAllPatientMedication"
    val insertPatientMedication="api/PatientMedication/InsertPatientMedication"



    val getProblemsWithIcon="Patient/getProblemsWithIcon"
    val getAllSuggestedProblem="Patient/getAllSuggestedProblem"
    val getAllProblems="Patient/getAllProblems"
    val insertSymtoms="api/PatientIPDPrescription/InsertSymtoms"
    val getSymptoms="api/PatientIPDPrescription/GetSymtoms"


    val getFluidIntakeDetails="api/ManualFoodAssign/GetManualFoodAssignList"
    val getFluidIntakeDetailsByRange="/api/ManualFoodAssign/FluidSummaryByDateRange"
    val getFluidOutPutDetailsByRange="api/output/OutputSummaryByDateRange"
    val getBpRangeHistory="api/PatientVital/GetPatientVitalGraph"
    val savePatientOutput="/api/v1/output/SavePatientOutput"
    val savePatientIntake="api/FoodIntake/InsertFoodIntake"
    val getFluidOutputDaily="api/output/GetPatientOutputList"



    val getPatientLastVital="api/PatientVital/GetPatientLastVital"
    val insertPatientVital="api/PatientVital/InsertPatientVital"


    val getFoodIntake="api/FoodIntake/GetFoodIntake"
    val intakeByDietID="api/FoodIntake/IntakeByDietID"

}