from enum import Enum

class State(Enum):
    INITIAL_STATE = "Initial State"
    INGESTED = "Ingested"
    ANALYZED = "Analyzed"
    PROCESSED = "Processed" # Visual + Audio/Text done
    COMPLIANT = "Compliant"
    PACKAGED = "Packaged"
    COMPLETED = "Completed"

class Event(Enum):
    INGEST = "Ingest"
    ANALYZE = "Analyze"
    PROCESS = "Process" # Visual + Audio/Text
    APPLY_COMPLIANCE = "ApplyCompliance"
    PACKAGE = "Package"
    FINISH = "Finish"