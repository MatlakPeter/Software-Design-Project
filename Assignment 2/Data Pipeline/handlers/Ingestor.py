from handlers.HandlerInterface import HandlerInterface
from enumerations.Enumerations import *


class Ingestor(HandlerInterface):
    def __init__(self, context):
        self.context = context

    def handle(self,):
        print("Ingestor")
        return Event.INGEST