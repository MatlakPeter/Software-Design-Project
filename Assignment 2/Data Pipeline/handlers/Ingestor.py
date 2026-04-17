import hashlib
from pathlib import Path
import subprocess
import json

from handlers.HandlerInterface import HandlerInterface
from enumerations.Enumerations import *


class Ingestor(HandlerInterface):
    def __init__(self, context):
        self.context = context

    def handle(self):
        print("=== INGESTOR ===")

        # Check if valid video path has been provided
        if not self._is_video_path():
            raise FileNotFoundError("Ingestor: Video path not found.")

        # Validate checksum
        self._validate_checksum()

        # File header
        self._validate_metadata()

        return Event.INGEST

    def _is_video_path(self):
        video_extensions = {".mp4", ".avi", ".mov", ".mkv", ".flv", ".wmv", ".webm"}
        path = self.context.file_path
        if not path.is_file():
            return False
        if path.suffix.lower() not in video_extensions:
            return False
        return True

    def _generate_sha256(self):
        filepath = self.context.file_path

        sha256_hash = hashlib.sha256()  # Create an SHA-256 hash object
        try:
            with open(filepath, "rb") as f: # Open the file
                # Read the file in 4K chunks to handle large files efficiently
                for byte_block in iter(lambda: f.read(4096), b""):
                    sha256_hash.update(byte_block)
            return sha256_hash.hexdigest() # Return the hexadecimal representation of the hash

        except FileNotFoundError:
            return "Error: File not found."

    def _validate_checksum(self):
        valid_checksum = '052f3074e96e4c08c1cbdebe161fc6d0b0b63832b9f185692374a14955939fe6'
        actual_checksum = self._generate_sha256()
        print("Checksum: ", actual_checksum)
        if actual_checksum != valid_checksum:
            raise Exception("Ingestor: Checksum mismatch.")
        return True

    def _get_video_metadata(self):
        filepath_string = str(self.context.file_path)
        command = [
            "ffprobe",  # the ffprobe command to run
            "-v", "quiet",  # -v quiet: Suppresses standard ffprobe terminal output
            "-print_format", "json",  # -print_format json: Outputs the result as a JSON string
            "-show_format",  # -show_format: Shows container format info (duration, size, bitrate)
            "-show_streams",  # -show_streams: Shows specific audio/video track info (codec, resolution)
            filepath_string
        ]
        try:
            result = subprocess.run(command, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)  # Run the subprocess command
            metadata = json.loads(result.stdout)  # json -> dict
            return metadata

        except json.JSONDecodeError:
            return {"error": "Failed to parse ffprobe output."}

    def _validate_metadata(self):
        metadata = self._get_video_metadata()

        if "error" in metadata:
            raise Exception(f"Ingestor: {metadata['error']}")

        self.context.video_metadata = metadata # store metadata to Context

        video_stream = next((stream for stream in metadata.get('streams', []) if stream.get('codec_type') == 'video'), None)

        if not video_stream:
            raise Exception("Ingestor: No video stream found in the master file.")

        required_codec = "hevc"
        required_width = 1920
        required_height = 1080

        actual_codec = video_stream.get('codec_name')
        actual_width = video_stream.get('width')
        actual_height = video_stream.get('height')
        print(f"Metadata Validated: {actual_width}x{actual_height} using {actual_codec}")

        # Collect all mismatches
        errors = []
        if actual_codec != required_codec:
            errors.append(f"codec '{actual_codec}' (expected '{required_codec}')")
        if actual_width != required_width:
            errors.append(f"width {actual_width}px (expected {required_width}px)")
        if actual_height != required_height:
            errors.append(f"height {actual_height}px (expected {required_height}px)")

        if errors: # Raise exception if any mismatch found
            raise Exception("Ingestor: Studio spec mismatch: " + ", ".join(errors))

        return True