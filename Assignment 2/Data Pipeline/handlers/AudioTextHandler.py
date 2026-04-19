import os

from handlers.HandlerInterface import HandlerInterface
from enumerations.Enumerations import Event

class AudioTextHandler(HandlerInterface):
    def __init__(self, context):
        self.context = context

    def handle(self):
        print("=== AUDIO / TEXT ===")

        self._audio_text_generation()
        return Event.AUDIO_TEXT_DONE

    def _audio_text_generation(self, base_directory="../../../videos/movie_101"):
        text_dir = os.path.join(base_directory, "text")
        audio_dir = os.path.join(base_directory, "audio")
        os.makedirs(text_dir, exist_ok=True)
        os.makedirs(audio_dir, exist_ok=True)

        source_transcript_path = os.path.join(text_dir, "source_transcript.txt")
        ro_translation_path = os.path.join(text_dir, "ro_translation.txt")
        ro_audio_path = os.path.join(audio_dir, "ro_audio.aac")

        with open(source_transcript_path, "w", encoding="utf-8") as f:
            f.write("00:00:15 - Hello, welcome to the movie.\n00:00:18 - This is a test.")

        with open(ro_translation_path, "w", encoding="utf-8") as f:
            f.write("00:00:15 - Bună, bun venit la film.\n00:00:18 - Acesta este un test.")

        with open(ro_audio_path, "wb") as f:
            f.write(b"") # Empty byte string -> 0-byte file

        self.context.text_assets["source_transcript"] = source_transcript_path
        self.context.text_assets["ro_translation"] = ro_translation_path
        self.context.audio_assets["audio"] = ro_audio_path

        print("Audio Text Generation Complete.")
