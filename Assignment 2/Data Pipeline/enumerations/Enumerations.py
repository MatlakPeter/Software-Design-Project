from enum import Enum

class State(Enum):
    INITIAL_STATE = "Initial State"
    INGESTED = "Ingested"
    PROCESSED = "Processed" # Analyse + Visual + Audio/Text done
    COMPLIANT = "Compliant"
    PACKAGED = "Packaged"

class Event(Enum):
    INGEST = "Ingest"
    ANALYZE = "Analyze"
    VISUALS_DONE = "Visuals_done"
    AUDIO_TEXT_DONE = "Audio_text_done"
    PROCESSING_DONE = "Processing_done"
    APPLY_COMPLIANCE = "ApplyCompliance"
    PACKAGE = "Package"
