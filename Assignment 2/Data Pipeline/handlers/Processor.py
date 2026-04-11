from enumerations.Enumerations import Event
from handlers.HandlerInterface import HandlerInterface


class Processor(HandlerInterface):
    def __init__(self, orchestrator):
        self.orchestrator = orchestrator

    def handle(self, orchestrator):
        print("Processor")
        # both Visual + Audio/Text done here
        self.orchestrator.event_finished(Event.PROCESS)