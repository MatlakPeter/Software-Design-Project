from enumerations.Enumerations import Event
from handlers.HandlerInterface import HandlerInterface


class Analyzer(HandlerInterface):
    def __init__(self, orchestrator):
        self.orchestrator = orchestrator

    def handle(self,):
        print("Analyzer")
        return Event.ANALYZE
