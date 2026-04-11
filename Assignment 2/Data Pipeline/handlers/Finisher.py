from enumerations.Enumerations import Event
from handlers.HandlerInterface import HandlerInterface


class Finisher(HandlerInterface):
    def __init__(self, orchestrator):
        self.orchestrator = orchestrator

    def handle(self, orchestrator):
        print("Finisher")
        self.orchestrator.event_finished(Event.FINISH)
