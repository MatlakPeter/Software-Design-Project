from pathlib import Path

class Context:
    def __init__(self, file_path):
        self.file_path = Path(file_path)

        # Ingest
        self.video_metadata = None

        # Analysis
        self.metadata_assets = {}

        # Visuals
        self.encoding_profile = None
        self.visuals_video_assets = {}
        self.visuals_images_assets = {}

        # Audio/Text
        self.text_assets = {}
        self.audio_assets = {}

        # Compliance
        self.compliance_passed = False

        # Packager
        self.drm_applied = False
        self.manifest_file_path = None
