from abc import ABC, abstractmethod



class HandlerInterface(ABC):
    @abstractmethod
    def handle(self):
        pass