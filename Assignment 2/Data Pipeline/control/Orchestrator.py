from control.Workflow import Workflow
from enumerations.Enumerations import *
from handlers.Ingestor import Ingestor
from handlers.Analyzer import Analyzer
from handlers.Processor import Processor
from handlers.ComplianceApplier import ComplianceApplier
from handlers.Packager import Packager
from handlers.Finisher import Finisher

class Orchestrator:
    def __init__(self, file_path):
        self.file_path = file_path
        self.workflow = Workflow()

        self._handlers = {
            State.INITIAL_STATE: Ingestor(self),
            State.INGESTED:      Analyzer(self),
            State.ANALYZED:      Processor(self),
            State.PROCESSED:     ComplianceApplier(self),
            State.COMPLIANT:     Packager(self),
            State.PACKAGED:      Finisher(self)
        }

    def do_workflow(self):
        curr_state = self.workflow.state

        if curr_state not in State:
            raise ValueError(f"Invalid workflow state: {curr_state}")

        if curr_state == State.COMPLETED:
            print("Completed")
            return

        handler = self._handlers[curr_state]
        handler.handle(self)

    def event_finished(self, event):
        self.workflow.transition(event)
        self.do_workflow()
