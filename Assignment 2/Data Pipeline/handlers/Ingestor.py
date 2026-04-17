from handlers.HandlerInterface import HandlerInterface
from enumerations.Enumerations import *


class Ingestor(HandlerInterface):
    def __init__(self, orchestrator):
        self.orchestrator = orchestrator

    def handle(self,):
        print("Ingestor")
        return Event.INGEST