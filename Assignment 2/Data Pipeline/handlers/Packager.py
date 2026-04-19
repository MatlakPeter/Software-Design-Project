import json
import os

from enumerations.Enumerations import Event
from handlers.HandlerInterface import HandlerInterface


class Packager(HandlerInterface):
    def __init__(self, context):
        self.context = context

    def handle(self):
        print("=== PACKAGER ===")

        self._package()

        return Event.PACKAGE

    def _package(self):
        base_directory = self.context.base_directory
        # Simulate encription
        self.context.drm_applied = True
        print("DRM encription successful.")

        os.makedirs(base_directory, exist_ok=True)
        manifest_file_path = os.path.join(base_directory, "manifest.json")

        manifest_data = {
            "pipeline_status": "Complete",
            "drm_protected": self.context.drm_applied,
            "compliance_verified": self.context.compliance_passed,
            "assets": {
                "video": self.context.visuals_video_assets,
                "images": self.context.visuals_images_assets,
                "text": self.context.text_assets,
                "audio": self.context.audio_assets,
                "metadata": self.context.metadata_assets
            }
        }

        with open(manifest_file_path, "w") as f:
            json.dump(manifest_data, f, indent=4)

        self.context.manifest_file_path = manifest_file_path

        print("Packaging successful!")