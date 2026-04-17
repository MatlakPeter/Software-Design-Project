from pathlib import Path

class Context:
    def __init__(self, file_path):
        self.file_path = Path(file_path)
        self.video_metadata = None
