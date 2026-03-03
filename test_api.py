import requests
import json

url = "http://127.0.0.1:8000/predict"
payload = {
    "records": [
        {
            "amt_ratio": 0.95,
            "dd_ratio": 1.0,
            "hit_count": 4,
            "rare2": 0,
            "ss": 2.5,
            "tfidf_cos": 0.85
        },
        {
            "amt_ratio": 0.5,
            "dd_ratio": 0.8,
            "hit_count": 2,
            "rare2": 1,
            "ss": 1.2,
            "tfidf_cos": 0.3
        }
    ]
}

response = requests.post(url, json=payload)
print("Status Code:", response.status_code)
print("Response:", json.dumps(response.json(), indent=2))
