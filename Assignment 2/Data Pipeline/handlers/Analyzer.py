from enumerations.Enumerations import Event
from handlers.HandlerInterface import HandlerInterface


class Analyzer(HandlerInterface):
    def __init__(self, context):
        self.context = context

    def handle(self,):
        print("Analyzer")
        return Event.ANALYZE
