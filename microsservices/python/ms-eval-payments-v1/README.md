# ms-eval-payments-v1

A minimal FastAPI microservice that exposes the `case.joblib` model bundle trained for the case reconciliation task.

## Structure

- `main.py` – FastAPI application, loads the joblib model and defines `/predict` endpoint.
- `requirements.txt` – Python dependencies.

## Usage

1. **Install dependencies**
   ```bash
   python -m pip install -r requirements.txt
   ```

2. **Place `case.joblib`**
   The service looks for the model in the path specified by the `MODEL_PATH` environment variable. By default it will attempt to load the file from `../../ia/notebook/case.joblib` relative to the service directory.
   You can override it like:
   ```bash
   export MODEL_PATH="/absolute/path/to/case.joblib"
   ```
   (on Windows use `set` or PowerShell `$env:MODEL_PATH=...`).

3. **Run the server**
   ```bash
   uvicorn main:app --host 0.0.0.0 --port 8000 --reload
   ```

4. **Endpoints**
   - `GET /health` – simple health check, returns `{"status":"ok","loaded":true}` when the model is loaded.
   - `POST /predict` – supply JSON with a list of feature vectors and receive calibrated scores.

   ```json
   {
     "records": [
       {"amt_ratio":0.5,"dd_ratio":1.0,"hit_count":3,"rare2":0,"ss":2.3,"tfidf_cos":0.12}
     ]
   }
   ```

   Response:
   ```json
   {"predictions":[0.342]}
   ```

## Notes

- The service does **not** compute raw features from transaction records; it expects the six numeric features in the same order the model was trained on (`amt_ratio`, `dd_ratio`, `hit_count`, `rare2`, `ss`, `tfidf_cos`).
- The model bundle contains a regressor and an isotonic calibrator; the endpoint applies both before returning probabilities.
