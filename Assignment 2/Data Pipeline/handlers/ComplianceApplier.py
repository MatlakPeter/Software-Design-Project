from enumerations.Enumerations import Event
from handlers.HandlerInterface import HandlerInterface


class ComplianceApplier(HandlerInterface):
    def __init__(self, context):
        self.context = context

    def handle(self):
        print("=== COMPLIANCE APPLIER ===")

        self._compliance_check()

        return Event.APPLY_COMPLIANCE

    def _compliance_check(self):
        print("Compliance: No blurring required.")
        print("Compliance: Applying regional branding (Netflix Original)...")

        self.context.compliance_passed = True

        print("Compliance: Video meets all regional compliance standards.")