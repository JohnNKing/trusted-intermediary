// defines the ORM-O01 (loosely) input that is expected for newborn screening

Logical: ORMO01
Id: orm-O01-logical-model
Title: "ORM-O01"
Description: "The expected input for ORM formatted data for the newborn screening ETOR workflow"
* MSH 1..1 SU MSHSegment "MSH segment"
* PID 1..1 SU PIDSegment "PID segment"
* ORC 1..* SU ORCSegment "ORC segment"
* OBR 1..* SU OBRSegment "OBR segment"
* OBX 1..* SU OBXSegment "OBX segment"


