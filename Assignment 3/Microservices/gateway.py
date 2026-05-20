from flask import Flask, jsonify
import requests

app = Flask(__name__)

@app.route('/api/movies')
def route_movies():
    try:
        # Route requests to the movie_service
        response = requests.get("http://localhost:5001/movies")
        return jsonify(response.json()), response.status_code
    except Exception:
        # Handle gateway-level routing failures 
        return jsonify({"error": "Gateway Error: Service A is down"}), 502

if __name__ == '__main__':
    app.run(port=5000)