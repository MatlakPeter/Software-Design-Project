import concurrent.futures

from context.Context import Context
from control.Workflow import Workflow
from enumerations.Enumerations import *
from handlers.AudioTextHandler import AudioTextHandler
from handlers.Ingestor import Ingestor
from handlers.Analyzer import Analyzer
from handlers.ComplianceApplier import ComplianceApplier
from handlers.Packager import Packager
from handlers.Finisher import Finisher
from handlers.VisualsHandler import VisualsHandler


class Orchestrator:
    def __init__(self, file_path):
        self.file_path = file_path
        self.workflow = Workflow()
        self.context = Context(file_path)

        self._handlers = {
            State.INITIAL_STATE: Ingestor(self.context),
            State.INGESTED:      [Analyzer(self.context),
                                  VisualsHandler(self.context),
                                  AudioTextHandler(self.context)],
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

            if isinstance(handler, list): # Check if we have a list of handlers (parallel execution) or just one (sequential exec)
                event = self._run_in_parallel(handler)
            else:
                event = handler.handle()

            if event is None:
                raise RuntimeError(f"No event produced for state: {curr_state}")

            self.workflow.transition(event)

    def _run_in_parallel(self, handlers):
        events = []
        with concurrent.futures.ThreadPoolExecutor() as executor:
            # submit all handel() methods to the thread pool
            futures = [executor.submit(h.handle) for h in handlers]

            for future in concurrent.futures.as_completed(futures):
                try:
                    event = future.result()
                    events.append(event)
                except Exception as e:
                    raise RuntimeError(f"Exception while handling event: {e}")

        expected = {Event.ANALYZE, Event.VISUALS_DONE, Event.AUDIO_TEXT_DONE}
        if set(events) == expected:
            return Event.PROCESSING_DONE
        return None

