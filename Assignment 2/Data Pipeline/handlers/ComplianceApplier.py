from enumerations.Enumerations import Event
from handlers.HandlerInterface import HandlerInterface


class ComplianceApplier(HandlerInterface):
    def __init__(self, orchestrator):
        self.orchestrator = orchestrator

    def handle(self, orchestrator):
        print("ComplianceApplier")
        self.orchestrator.event_finished(Event.APPLY_COMPLIANCE)
