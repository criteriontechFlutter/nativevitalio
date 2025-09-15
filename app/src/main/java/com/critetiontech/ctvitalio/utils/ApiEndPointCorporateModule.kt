package com.critetiontech.ctvitalio.utils

class ApiEndPointCorporateModule {

    val getJoinedChallenge="api/CorporateChallenges/GetJoinedChallengesByEmployeeId"
    val getNewChallenge="api/CorporateChallenges/GetCorporateChallengesByClientId"
    val insertChallengeparticipants ="api/Challengeparticipants/InsertChallengeparticipants"
    val leaveChallengeparticipants ="api/Challengeparticipants/LeaveChallengeparticipants"

    val insertMood="api/MoodTracker/InsertMood"
    val getMoodByPid="api/MoodTracker/GetMoodByPid"
    val getAllMoods="api/MoodTracker/GetAllMoods"


    val insertEnergyTankMaster="api/EnergyTankMaster/InsertEnergyTankMaster"
}