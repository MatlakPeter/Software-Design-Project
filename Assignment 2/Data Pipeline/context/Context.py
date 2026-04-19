from pathlib import Path

class Context:
    def __init__(self, file_path):
        self.file_path = Path(file_path)

        # Ingest
        self.video_metadata = None

        # Analysis
        self.analysis_assets = {}

        # Visuals
        self.encoding_profile = None
        self.visual_assets = {}

        # Audio/Text
        self.text_assets = {}
        self.audio_assets = {}
