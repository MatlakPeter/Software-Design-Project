from enumerations.Enumerations import Event
from handlers.HandlerInterface import HandlerInterface


class Packager(HandlerInterface):
    def __init__(self, orchestrator):
        self.orchestrator = orchestrator

    def handle(self, orchestrator):
        print("Packager")
        self.orchestrator.event_finished(Event.PACKAGE)
