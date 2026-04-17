from enumerations.Enumerations import Event
from handlers.HandlerInterface import HandlerInterface


class Packager(HandlerInterface):
    def __init__(self, context):
        self.context = context

    def handle(self):
        print("Packager")
        return Event.PACKAGE