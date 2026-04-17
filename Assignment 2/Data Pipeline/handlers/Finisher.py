from enumerations.Enumerations import Event
from handlers.HandlerInterface import HandlerInterface


class Finisher(HandlerInterface):
    def __init__(self, context):
        self.context = context

    def handle(self):
        print("Finisher")
        return Event.FINISH