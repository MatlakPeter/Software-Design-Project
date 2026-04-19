from enumerations.Enumerations import *

class Workflow:
    def __init__(self):
        self._state = State.INITIAL_STATE
        self._transitions = {
            (State.INITIAL_STATE, Event.INGEST)           : State.INGESTED,
            (State.INGESTED,      Event.PROCESSING_DONE)  : State.PROCESSED,
            (State.PROCESSED,     Event.APPLY_COMPLIANCE) : State.COMPLIANT,
            (State.COMPLIANT,     Event.PACKAGE)          : State.PACKAGED,
            (State.PACKAGED,      Event.FINISH)           : State.COMPLETED
        }

    def transition(self, event):
        key = (self.state, event)
        if key not in self._transitions:
            raise ValueError(f"Invalid transition: {self.state} -> {event}")
        print("Transition from", self.state.value, "to", self._transitions[key].value)
        next_state = self._transitions[key]
        self.state = next_state



    @property
    def state(self):
        return self._state
    @state.setter
    def state(self, value):
        self._state = value
