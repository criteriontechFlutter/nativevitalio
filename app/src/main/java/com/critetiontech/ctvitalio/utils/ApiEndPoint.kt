package com.critetiontech.ctvitalio.utils

class ApiEndPoint {

    val digiDoctorBaseUrl="http://52.172.134.222:205/api/v1.0/"

    val updatePatient="api/PatientRegistration/UpdatePatientProfile"




    val getStateMasterByCountryId="api/StateMaster/GetStateMasterByCountryId"
    val getCityMasterByStateId="api/CityMaster/GetCityMasterByStateId"
    val patientSignUp="api/PatientRegistration/PatientSignUp"
    val getProblemList="api/KnowMedApis/GetProblemList"

    val getFrequencyList="api/KnowMedApis/GetFrequencyList"

    val patientAllergies="api/PatientIPDPrescription/PatientAllergies"
    val getHistorySubCategoryMasterById="api/HistorySubCategory/GetHistorySubCategoryMasterById"
    val savePatientAllergies="api/PatientIPDPrescription/SavePatientAllergies"
    val deletePatientAllergies="api/PatientIPDPrescription/DeletePatientAllergies"


    val getPatientDetailsByMobileNo = "api/PatientRegistration/GetPatientDetailsByMobileNo"
    val sentLogInOTPForVitalioApp="api/LogInForVitalioApp/SentLogInOTPForVitalioApp"
    val verifyLogInOTPForVitalioApp= "api/LogInForVitalioApp/VerifyLogInOTPForVitalioApp"
    val logoutFromApp= "api/LogInForVitalioApp/LogOutOTPForVitalioApp"



    val getAllPatientMedication="api/PatientMedication/GetAllPatientMedication"
    val insertPatientMedication="api/PatientMedication/InsertPatientMedication"


    val insertResult="api/InvestigationByPatient/InsertResult"
    val insertPatientMediaData="api/PatientMediaData/InsertPatientMediaData"

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
    val insertFoodIntake="api/FoodIntake/InsertFoodIntake"
    val sentMessageChat="SaveUserChat"
    val getUserChat="GetUserChat"



    val getPatientLastVital="api/PatientVital/GetPatientLastVital"
    val insertPatientVital="api/PatientVital/InsertPatientVital"
    val getEmergencyContact="api/EmergencyContact/GetEmergencyContactByPid"
    val saveEmergencyContact="api/EmergencyContact/InsertEmergencyContact"
    val deleteEmergency="api/EmergencyContact/RemoveEmergencyContact"


    val getFoodIntake="api/FoodIntake/GetFoodIntake"
    val intakeByDietID="api/FoodIntake/IntakeByDietID"



    val getPatientMediaData="api/PatientMediaData/GetPatientMediaData"






}