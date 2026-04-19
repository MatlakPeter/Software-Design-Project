import json
import os

from enumerations.Enumerations import Event
from handlers.HandlerInterface import HandlerInterface


class Analyzer(HandlerInterface):
    def __init__(self, context):
        self.context = context

    def handle(self,):
        print("=== ANALYZER ===")

        self._analyze()

        return Event.ANALYZE

    def _analyze(self):
        base_directory = self.context.base_directory
        metadata_directory = os.path.join(base_directory, "metadata")
        os.makedirs(metadata_directory, exist_ok=True)
        output_file_path = os.path.join(metadata_directory, "scene_analysis.json")

        # Dummy data representing the ML outputs
        dummy_data = {
            "theme_song_end_timestamp": "00:01:30",
            "credits_start_timestamp": "01:55:00",
            "segments": [
                {"type": "establishing_shot", "start": "00:00:00", "end": "00:00:15"},
                {"type": "dialogue", "start": "00:00:15", "end": "00:05:00"}
            ]
        }

        with open(output_file_path, 'w') as output_file:
            json.dump(dummy_data, output_file)

        self.context.metadata_assets["scene_analysis"] = output_file_path

        print(f"Analysis completed. Saved to {output_file_path}")
