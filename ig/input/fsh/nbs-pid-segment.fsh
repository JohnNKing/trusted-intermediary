Logical: PIDSegment
Id: segment-pid-logical-model
Title: "PID Segment"
Description: "The PID Segment"
* segmentType 1..1 SU string "PID"
* setId 1..1 SU string "PID-1 a sequence number"
* patientId 0..1 SU string "a patient id (retained for backward compatibility, but should be blank"
* patientIdList 1..1 SU string "PID-3 a list of patient identifiers"
* alternatePatientId 0..1 SU string "alternate patient id, should be blank"
* patientName 1..1 SU string "PID-5 patient name"
* mothersMaidenName 0..1 SU string "PID-6 mothers maiden name"
* birthDateTime 1..1 SU string "PID-7 birth date and time"
* administrativeSex 1..1 SU string "PID-8 administrative sex"
* patientAlias 0..1 SU string "should be blank"
* race 1..1 SU string "race/ethnicity"
* patientAddress 0..1 SU string "PID-11 patient address"
* countyCode 0..1 SU string "retained for backward compatibility"
* homePhone 1..1 SU string "home phone number"
* businessPhone 0..1 SU string "business phone number"
* primaryLanguage 0..1 SU string "language"
* maritalStatus 1..1 SU string "marital status"
* religion 0..1 SU string "religion"
* patientAccountNumber 1..1 SU string "patient account number"
* ssn 0..1 SU string "retained for backward compatibility"
// other optional fields