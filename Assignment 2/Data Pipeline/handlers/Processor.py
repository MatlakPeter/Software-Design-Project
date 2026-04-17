from enumerations.Enumerations import Event
from handlers.HandlerInterface import HandlerInterface


class Processor(HandlerInterface):
    def __init__(self, context):
        self.context = context

    def handle(self):
        print("Processor")
        # both Visual + Audio/Text done here
        return Event.PROCESS