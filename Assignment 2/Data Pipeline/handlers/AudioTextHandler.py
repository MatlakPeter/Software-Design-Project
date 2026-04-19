from handlers.HandlerInterface import HandlerInterface
from enumerations.Enumerations import Event

class AudioTextHandler(HandlerInterface):
    def __init__(self, context):
        self.context = context

    def handle(self):
        print("=== AUDIO / TEXT ===")

        return Event.AUDIO_TEXT_DONE