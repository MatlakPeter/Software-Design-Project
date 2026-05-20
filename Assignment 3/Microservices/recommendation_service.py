from flask import Flask
import os
import random
import time

app = Flask(__name__)

@app.route('/recommendations')
def get_recommendations():
    # Check environement variable
    chaos_mode = os.environ.get("CHAOS_MODE", "false").lower() == "true"
    
    if chaos_mode:
        event = random.choice(["fail", "delay", "success"])
        if event == "fail":
            return "Service Unavailable", 503
        elif event == "delay":
            time.sleep(random.uniform(3.0, 10.0))
            
    # Successful response:
    return {"recommendations": [101, 102, 103]}

if __name__ == '__main__':
    app.run(port=5002)