
from control.Orchestrator import Orchestrator

if __name__ == "__main__":
    folder_path = "../../../videos/"
    file_name = "IMG_2599.MOV"
    file_path = folder_path + file_name

    orchestrator = Orchestrator(file_path)
    orchestrator.do_workflow()
