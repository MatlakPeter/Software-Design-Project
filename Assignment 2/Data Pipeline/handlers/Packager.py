from enumerations.Enumerations import Event
from handlers.HandlerInterface import HandlerInterface


class Packager(HandlerInterface):
    def __init__(self, orchestrator):
        self.orchestrator = orchestrator

    def handle(self):
        print("Packager")
        return Event.PACKAGE