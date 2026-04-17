from context.Context import Context
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
        self.context = Context(file_path)

        self._handlers = {
            State.INITIAL_STATE: Ingestor(self.context),
            State.INGESTED:      Analyzer(self.context),
            State.ANALYZED:      Processor(self.context),
            State.PROCESSED:     ComplianceApplier(self.context),
            State.COMPLIANT:     Packager(self.context),
            State.PACKAGED:      Finisher(self.context)
        }

    def do_workflow(self):
        while self.workflow.state != State.COMPLETED:
            curr_state = self.workflow.state

            if curr_state not in State:
                raise ValueError(f"Invalid workflow state: {curr_state}")

            handler = self._handlers[curr_state]
            event = handler.handle()
            self.workflow.transition(event)

