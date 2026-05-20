from flask import Flask, jsonify
import requests
import time

app = Flask(__name__)

# Circuit Breaker variables
cb_state = "CLOSED"
failure_count = 0
last_failure_time = 0
FAILURE_THRESHOLD = 3
RESET_TIMEOUT = 10

def get_recommendations_with_resiliency():
    global cb_state, failure_count, last_failure_time
    
    if cb_state == "OPEN": # requests currently blocked
        if time.time() - last_failure_time > RESET_TIMEOUT:
            cb_state = "HALF_OPEN" # let 1 request through
        else:
            raise Exception("Circuit is OPEN. Fast-failing.")

    try:
        response = requests.get("http://localhost:5002/recommendations", timeout=1.5) # 1.5-second timeout for the Recommendation Service 
        response.raise_for_status() # raise HTTP error, if no successful response 
        
        # If request succeeds, reset the circuit breaker back to healthy
        if cb_state == "HALF_OPEN":
            cb_state = "CLOSED" # if all right, set state to normal
            failure_count = 0
            
        return response.json()["recommendations"]
        
    except requests.exceptions.RequestException as e:
        failure_count += 1
        if failure_count >= FAILURE_THRESHOLD:
            cb_state = "OPEN"
            last_failure_time = time.time()
        raise e


@app.route('/movies')
def get_movies():
    # Movie titles and descriptions
    movie_page = {"title": "Inception", "description": "A dream within a dream."}
    
    try:
        # Call Service B
        recs = get_recommendations_with_resiliency()
        movie_page["recommendations"] = recs
    except Exception:
        # Give the hardcoded movies 
        movie_page["recommendations"] = ["Trending: The Matrix", "Trending: Interstellar"]
        movie_page["system_note"] = "Recommendations currently unavailable."
        
    return jsonify(movie_page)

if __name__ == '__main__':
    app.run(port=5001)