package com.criterion.nativevitalio.Omron.Database;

import android.net.Uri;

/**
 * Created by Omron HealthCare Inc
 */

public class OmronDBConstans {

    public static final String AUTHORITY = "com.criterion.nativevitalio.Database.OmronDBProvider";
    public static final String DB_NAME = "omron_app.db";
    public static final int DB_VERSION = 1;

    public static final String DEVICE_SELECTED_USER = "selected_user";
    public static final String DEVICE_LOCAL_NAME = "local_name";
    public static final String DEVICE_DISPLAY_NAME = "display_name";
    public static final String DEVICE_IDENTITY_NAME = "identity_name";
    public static final String DEVICE_CATEGORY = "device_category";

    // Blood Pressure Keys
    public static final int VITAL_DATA = 1;

    private static final String STR_ID = "_id";
    private static final String STR_CONTENT = "content://";
    private static final String STR_CREATE_TABLE = "create table %s";
    private static final String STR_INTEGER_KEY = "(%s integer primary key autoincrement, ";
    private static final String STR_START_DATE = "StartDateUTCKey";
    private static final String STR_TEXT3 = "%s text,%s text,%s text,";
    private static final String STR_TEXT3E = "%s text,%s text,%s text)";
    private static final String STR_TEXT2 = "%s text,%s text,";
    private static final String STR_TEXT2E = "%s text,%s text)";

    public static final String VITAL_DATA_TABLE = "vital_data_table";
    public static final String VITAL_DATA_INDEX = STR_ID;

    public static final String VITAL_DATA_OMRONVitalDataCuffFlagKey = "OMRONVitalDataCuffFlagKey";
    public static final String VITAL_DATA_OMRONVitalDataDiastolicKey = "OMRONVitalDataDiastolicKey";
    public static final String VITAL_DATA_OMRONVitalDataIrregularFlagKey = "OMRONVitalDataIrregularFlagKey";
    public static final String VITAL_DATA_OMRONVitalDataMeasurementDateKey = "OMRONVitalDataMeasurementDateKey";
    public static final String VITAL_DATA_OMRONVitalDataMovementFlagKey = "OMRONVitalDataMovementFlagKey";
    public static final String VITAL_DATA_OMRONVitalDataPulseKey = "OMRONVitalDataPulseKey";
    public static final String VITAL_DATA_OMRONVitalDataSystolicKey = "OMRONVitalDataSystolicKey";
    public static final String VITAL_DATA_OMRONVitalDataMeasurementDateUTCKey = "OMRONVitalDataMeasurementDateUTCKey";

    public static final String VITAL_DATA_OMRONVitalDataAtrialFibrillationDetectionFlagKey = "OMRONVitalDataAtrialFibrillationDetectionFlagKey";
    public static final String VITAL_DATA_OMRONVitalDataMeasurementModeKey = "OMRONVitalDataMeasurementModeKey";

    public static final String VITAL_DATA_OMRONVitalDataPositionIndicatorKey = "OMRONVitalDataPositionIndicatorKey";
    public static final String VITAL_DATA_OMRONVitalDataConsecutiveMeasurementKey = "OMRONVitalDataConsecutiveMeasurementKey";
    public static final String VITAL_DATA_OMRONVitalDataIrregularHeartBeatCountKey = "OMRONVitalDataIrregularHeartBeatCountKey";

    public static final Uri VITAL_DATA_CONTENT_URI = Uri.parse(STR_CONTENT + AUTHORITY + "/" + VITAL_DATA_TABLE);

    public static final String SQL_CREATE_VITAL_DATA_TABLE = String
            .format(STR_CREATE_TABLE
                            + STR_INTEGER_KEY
                            + STR_TEXT3
                            + STR_TEXT3
                            + STR_TEXT3
                            + STR_TEXT3
                            + STR_TEXT3E,
                    VITAL_DATA_TABLE,
                    VITAL_DATA_INDEX,
                    VITAL_DATA_OMRONVitalDataCuffFlagKey, VITAL_DATA_OMRONVitalDataDiastolicKey,VITAL_DATA_OMRONVitalDataIrregularFlagKey,
                    VITAL_DATA_OMRONVitalDataMeasurementDateKey, VITAL_DATA_OMRONVitalDataMovementFlagKey, VITAL_DATA_OMRONVitalDataPulseKey,
                    VITAL_DATA_OMRONVitalDataSystolicKey, VITAL_DATA_OMRONVitalDataMeasurementDateUTCKey, VITAL_DATA_OMRONVitalDataAtrialFibrillationDetectionFlagKey,
                    VITAL_DATA_OMRONVitalDataMeasurementModeKey, VITAL_DATA_OMRONVitalDataPositionIndicatorKey, VITAL_DATA_OMRONVitalDataConsecutiveMeasurementKey, 
                    VITAL_DATA_OMRONVitalDataIrregularHeartBeatCountKey, DEVICE_SELECTED_USER, DEVICE_LOCAL_NAME);


    // Sleep Keys
    public static final int SLEEP_DATA = 2;
    public static final String SLEEP_DATA_TABLE = "sleep_data_table";
    public static final String SLEEP_DATA_INDEX = STR_ID;

    public static final String SLEEP_DATA_SleepStartTimeKey = "SleepStartTimeKey";
    public static final String SLEEP_DATA_SleepOnSetTimeKey = "SleepOnSetTimeKey";
    public static final String SLEEP_DATA_WakeUpTimeKey = "WakeUpTimeKey";
    public static final String SLEEP_DATA_SleepingTimeKey = "SleepingTimeKey";
    public static final String SLEEP_DATA_SleepEfficiencyKey = "SleepEfficiencyKey";
    public static final String SLEEP_DATA_SleepArousalTimeKey = "SleepArousalTimeKey";
    public static final String SLEEP_DATA_SleepBodyMovementKey = "SleepBodyMovementKey";
    public static final String SLEEP_DATA_StartDateUTCKey = STR_START_DATE;
    public static final String SLEEP_DATA_StartEndDateUTCKey = "StartEndDateUTCKey";

    public static final Uri SLEEP_DATA_CONTENT_URI = Uri.parse(STR_CONTENT + AUTHORITY + "/" + SLEEP_DATA_TABLE);

    public static final String SQL_CREATE_SLEEP_DATA_TABLE = String
            .format(STR_CREATE_TABLE
                            + STR_INTEGER_KEY
                            + STR_TEXT2
                            + STR_TEXT2
                            + STR_TEXT2
                            + STR_TEXT2
                            + STR_TEXT2E,
                    SLEEP_DATA_TABLE,
                    SLEEP_DATA_INDEX,
                    SLEEP_DATA_SleepStartTimeKey, SLEEP_DATA_SleepOnSetTimeKey,
                    SLEEP_DATA_WakeUpTimeKey, SLEEP_DATA_SleepingTimeKey,
                    SLEEP_DATA_SleepEfficiencyKey, SLEEP_DATA_SleepArousalTimeKey,
                    SLEEP_DATA_SleepBodyMovementKey,SLEEP_DATA_StartDateUTCKey,
                    SLEEP_DATA_StartEndDateUTCKey, DEVICE_LOCAL_NAME);

    // Record Keys
    public static final int RECORD_DATA = 3;
    public static final String RECORD_DATA_TABLE = "record_data_table";
    public static final String RECORD_DATA_INDEX = STR_ID;

    public static final String RECORD_DATA_StartDateUTCKey = STR_START_DATE;

    public static final Uri RECORD_DATA_CONTENT_URI = Uri.parse(STR_CONTENT + AUTHORITY + "/" + RECORD_DATA_TABLE);

    public static final String SQL_CREATE_RECORD_DATA_TABLE = String
            .format(STR_CREATE_TABLE
                            + STR_INTEGER_KEY
                            + STR_TEXT2E,
                    RECORD_DATA_TABLE,
                    RECORD_DATA_INDEX,
                    RECORD_DATA_StartDateUTCKey, DEVICE_LOCAL_NAME);

    // Activity Keys
    public static final int ACTIVITY_DATA = 4;

    public static final String ACTIVITY_DATA_TABLE = "activity_data_table";
    public static final String ACTIVITY_DATA_INDEX = STR_ID;

    public static final String ACTIVITY_DATA_StartDateUTCKey = STR_START_DATE;
    public static final String ACTIVITY_DATA_EndDateUTCKey = "EndDateUTCKey";
    public static final String ACTIVITY_DATA_MeasurementValueKey = "MeasurementValueKey";
    public static final String ACTIVITY_DATA_SeqNumKey = "SeqNumKey";
    public static final String ACTIVITY_DATA_Type = "Datatype";

    public static final Uri ACTIVITY_DATA_CONTENT_URI = Uri.parse(STR_CONTENT
            + AUTHORITY + "/" + ACTIVITY_DATA_TABLE);

    public static final String SQL_CREATE_ACTIVITY_DATA_TABLE = String
            .format(STR_CREATE_TABLE
                            + STR_INTEGER_KEY
                            + STR_TEXT3
                            + STR_TEXT3E,
                    ACTIVITY_DATA_TABLE,
                    ACTIVITY_DATA_INDEX,
                    ACTIVITY_DATA_StartDateUTCKey, ACTIVITY_DATA_EndDateUTCKey, ACTIVITY_DATA_MeasurementValueKey,
                    ACTIVITY_DATA_SeqNumKey, ACTIVITY_DATA_Type, DEVICE_LOCAL_NAME);

    public static final int ACTIVITY_DIVIDED_DATA = 6;

    public static final String ACTIVITY_DIVIDED_DATA_TABLE = "activity_divided_data_table";
    public static final String ACTIVITY_DIVIDED_DATA_INDEX = STR_ID;

    public static final String ACTIVITY_DIVIDED_DATA_MainStartDateUTCKey = "MainStartDateUTCKey";
    public static final String ACTIVITY_DIVIDED_DATA_StartDateUTCKey = STR_START_DATE;
    public static final String ACTIVITY_DIVIDED_DATA_MeasurementValueKey = "MeasurementValueKey";
    public static final String ACTIVITY_DIVIDED_DATA_SeqNumKey = "SeqNumKey";
    public static final String ACTIVITY_DIVIDED_DATA_Type = "type";


    public static final Uri ACTIVITY_DIVIDED_DATA_CONTENT_URI = Uri.parse(STR_CONTENT + AUTHORITY + "/" + ACTIVITY_DIVIDED_DATA_TABLE);

    public static final String SQL_CREATE_ACTIVITY_DIVIDED_DATA_TABLE = String
            .format(STR_CREATE_TABLE
                            + STR_INTEGER_KEY
                            + STR_TEXT3
                            + STR_TEXT3E,
                    ACTIVITY_DIVIDED_DATA_TABLE,
                    ACTIVITY_DIVIDED_DATA_INDEX,
                    ACTIVITY_DIVIDED_DATA_MainStartDateUTCKey,
                    ACTIVITY_DIVIDED_DATA_StartDateUTCKey, ACTIVITY_DIVIDED_DATA_MeasurementValueKey, ACTIVITY_DIVIDED_DATA_SeqNumKey,
                    ACTIVITY_DIVIDED_DATA_Type, DEVICE_LOCAL_NAME);


    public static final int REMINDER_DATA = 5;
    public static final String REMINDER_DATA_TABLE = "reminder_data_table";
    public static final String REMINDER_DATA_INDEX = STR_ID;

    public static final String REMINDER_DATA_DEVICE_LOCAL_NAME = "device_local_name";
    public static final String REMINDER_DATA_TimeFormat = "time_format";
    public static final String REMINDER_DATA_Hour = "time_hour";
    public static final String REMINDER_DATA_Minute = "time_minute";
    public static final String REMINDER_DATA_Days = "time_days";

    public static final Uri REMINDER_DATA_CONTENT_URI = Uri.parse(STR_CONTENT
            + AUTHORITY + "/" + REMINDER_DATA_TABLE);

    public static final String SQL_CREATE_REMINDER_DATA_TABLE = String
            .format(STR_CREATE_TABLE
                            + STR_INTEGER_KEY
                            + STR_TEXT2
                            + STR_TEXT3E,
                    REMINDER_DATA_TABLE,
                    REMINDER_DATA_INDEX, REMINDER_DATA_DEVICE_LOCAL_NAME, REMINDER_DATA_TimeFormat,
                    REMINDER_DATA_Hour, REMINDER_DATA_Minute, REMINDER_DATA_Days);


    // Weight Data
    public static final int WEIGHT_DATA = 7;
    public static final String WEIGHT_DATA_TABLE = "weight_data_table";
    public static final String WEIGHT_DATA_INDEX = STR_ID;

    public static final String WEIGHT_DATA_StartTimeKey = "WeightStartTimeKey";
    public static final String WEIGHT_DATA_WeightKey = "WeightWeightKey";
    public static final String WEIGHT_DATA_BodyFatLevelKey = "BodyFatLevelKey";
    public static final String WEIGHT_DATA_BodyFatPercentageKey = "BodyFatPercentageKey";
    public static final String WEIGHT_DATA_RestingMetabolismKey = "WeightRestingMetabolismKey";
    public static final String WEIGHT_DATA_SkeletalMuscleKey = "SkeletalMusclePercentageKey";
    public static final String WEIGHT_DATA_BMIKey = "WeightBMIKey";
    public static final String WEIGHT_DATA_BMIClassificationKey = "WeightBMIClassificationKey";
    public static final String WEIGHT_DATA_BodyAgeKey = "BodyAgeKey";
    public static final String WEIGHT_DATA_VisceralFatLevelKey = "VisceralFatLevelKey";
    public static final String WEIGHT_DATA_VisceralFatLevelClassificationKey = "VisceralFatLeveClassificationKey";
    public static final String WEIGHT_DATA_SkeletalMuscleLevelKey = "SkeletalMuscleLevelKey";

    public static final Uri WEIGHT_DATA_CONTENT_URI = Uri.parse(STR_CONTENT + AUTHORITY + "/" + WEIGHT_DATA_TABLE);

    public static final String SQL_CREATE_WEIGHT_DATA_TABLE = String
            .format(STR_CREATE_TABLE
                            + STR_INTEGER_KEY
                            + STR_TEXT3
                            + STR_TEXT3
                            + STR_TEXT3
                            + STR_TEXT3
                            + STR_TEXT2E,
                    WEIGHT_DATA_TABLE,
                    WEIGHT_DATA_INDEX,
                    WEIGHT_DATA_StartTimeKey, WEIGHT_DATA_WeightKey, WEIGHT_DATA_BodyFatLevelKey,
                    WEIGHT_DATA_BodyFatPercentageKey, WEIGHT_DATA_RestingMetabolismKey, WEIGHT_DATA_SkeletalMuscleKey, 
                    WEIGHT_DATA_BMIKey, WEIGHT_DATA_BodyAgeKey, WEIGHT_DATA_VisceralFatLevelKey,
                    WEIGHT_DATA_SkeletalMuscleLevelKey, DEVICE_SELECTED_USER,WEIGHT_DATA_BMIClassificationKey,
                    WEIGHT_DATA_VisceralFatLevelClassificationKey,DEVICE_LOCAL_NAME);

    // Pulse Oxymeter Data
    public static final int OXYMETER_DATA = 8;
    public static final String OXYMETER_DATA_TABLE = "oxymeter_data_table";
    public static final String OXYMETER_DATA_INDEX = STR_ID;

    public static final String OXYMETER_DATA_StartTimeKey = "OxymeterStartTimeKey";
    public static final String OXYMETER_DATA_SpO2Key = "OxymeterSpO2Key";
    public static final String OXYMETER_DATA_PulseKey = "OxymeterPulseKey";
    
    public static final Uri OXYMETER_DATA_CONTENT_URI = Uri.parse(STR_CONTENT + AUTHORITY + "/" + OXYMETER_DATA_TABLE);

    public static final String SQL_CREATE_OXYMETER_DATA_TABLE = String
            .format(STR_CREATE_TABLE
                            + STR_INTEGER_KEY
                            + STR_TEXT3
                            + STR_TEXT2E,
                    OXYMETER_DATA_TABLE,
                    OXYMETER_DATA_INDEX,
                    OXYMETER_DATA_StartTimeKey, OXYMETER_DATA_SpO2Key, OXYMETER_DATA_PulseKey,
                    DEVICE_SELECTED_USER,DEVICE_LOCAL_NAME);
    //wheeze Data
    public static final int WHEEZE_DATA = 9;
    public static final String WHEEZE_DATA_TABLE = "wheeze_data_table";
    public static final String WHEEZE_DATA_INDEX = STR_ID;

    public static final String WHEEZE_DATA_StartTimeKey = "WheezeStartTimeKey";
    public static final String WHEEZE_DATA_WheezeKey = "WheezeWheezeKey";
    public static final String WHEEZE_DATA_ErrorNoiseKey = "WheezeErrorNoiseKey";
    public static final String WHEEZE_DATA_ErrorDecreaseBreathingSoundLevelKey = "WheezeErrorDecreaseBreathingSoundLevelKey";
    public static final String WHEEZE_DATA_ErrorSurroundingNoiseKey = "WheezeErrorSurroundingNoiseKey";


    public static final Uri WHEEZE_DATA_CONTENT_URI = Uri.parse(STR_CONTENT + AUTHORITY + "/" + WHEEZE_DATA_TABLE);

    public static final String SQL_CREATE_WHEEZE_DATA_TABLE = String
            .format(STR_CREATE_TABLE
                            + STR_INTEGER_KEY
                            + STR_TEXT3
                            + STR_TEXT3
                            + "%s text)",
                    WHEEZE_DATA_TABLE,
                    WHEEZE_DATA_INDEX,
                    WHEEZE_DATA_StartTimeKey,WHEEZE_DATA_WheezeKey, WHEEZE_DATA_ErrorNoiseKey,
                    WHEEZE_DATA_ErrorDecreaseBreathingSoundLevelKey, WHEEZE_DATA_ErrorSurroundingNoiseKey, DEVICE_SELECTED_USER,
                    DEVICE_LOCAL_NAME);


    // pairing device Data
    public static final int PAIRING_DATA = 10;
    public static final String PAIRING_DATA_TABLE = "pairing_data_table";
    public static final String PAIRING_DATA_INDEX = STR_ID;

    public static final String PAIRING_DATA_LocalName = "LocalNameKey";
    public static final String PAIRING_DATA_uuid = "uuidKey";

    public static final String PAIRING_DATA_UserNumber = "UserNumberKey";
    public static final String PAIRING_DATA_SequenceNo = "SequenceNoKey";

    public static final Uri PAIRING_DATA_CONTENT_URI = Uri.parse(STR_CONTENT + AUTHORITY + "/" + PAIRING_DATA_TABLE);

    public static final String SQL_CREATE_PAIRING_DATA_TABLE = String
            .format(STR_CREATE_TABLE
                            + STR_INTEGER_KEY
                            + STR_TEXT3
                            + "%s text)",
                    PAIRING_DATA_TABLE,
                    PAIRING_DATA_INDEX,
                    PAIRING_DATA_LocalName, PAIRING_DATA_uuid, PAIRING_DATA_UserNumber, PAIRING_DATA_SequenceNo);

    // Personal Data
    public static final int PERSONAL_DATA = 11;
    public static final String PERSONAL_DATA_TABLE = "Personal_data_table";
    public static final String PERSONAL_DATA_INDEX = STR_ID;

    public static final String PERSONAL_DATA_Birthday = "BirthdayKey";
    public static final String PERSONAL_DATA_Height = "HeightKey";
    public static final String PERSONAL_DATA_Weight = "WeightKey";
    public static final String PERSONAL_DATA_Unit = "UnitKey";
    public static final String PERSONAL_DATA_Gender = "GenderKey";
    public static final String PERSONAL_DATA_Stride = "StrideKey";

    public static final Uri PERSONAL_DATA_CONTENT_URI = Uri.parse(STR_CONTENT + AUTHORITY + "/" + PERSONAL_DATA_TABLE);

    public static final String SQL_CREATE_PERSONAL_DATA_TABLE = String
            .format(STR_CREATE_TABLE
                            + STR_INTEGER_KEY
                            + STR_TEXT3
                            + STR_TEXT3E,
                    PERSONAL_DATA_TABLE,
                    PERSONAL_DATA_INDEX,
                    PERSONAL_DATA_Birthday, PERSONAL_DATA_Height, PERSONAL_DATA_Weight,
                    PERSONAL_DATA_Unit, PERSONAL_DATA_Gender, PERSONAL_DATA_Stride);

}
