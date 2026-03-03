import os
from typing import List

import joblib
import numpy as np
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel

app = FastAPI(
    title="Case Reconciliation Model",
    description="Simple FastAPI service that loads a joblib model bundle and exposes a prediction endpoint.",
    version="0.1.0",
)

# default path can be overridden with env var MODEL_PATH
DEFAULT_MODEL_PATH = os.environ.get(
    "MODEL_PATH",
    os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "..", "..", "ia", "notebook", "case.joblib")),
)

model_bundle = None


def load_model(path: str):
    global model_bundle
    if not os.path.exists(path):
        raise FileNotFoundError(f"Model file not found at {path}")
    model_bundle = joblib.load(path)
    # sanity check
    if not isinstance(model_bundle, dict):
        raise ValueError("Loaded model is not a dict-like bundle")


@app.on_event("startup")
def startup_event():
    try:
        load_model(DEFAULT_MODEL_PATH)
        print("Loaded model from", DEFAULT_MODEL_PATH)
    except Exception as exc:
        print("Failed to load model:", exc)
        # we don't crash startup - predictions will raise


class FeatureInput(BaseModel):
    amt_ratio: float
    dd_ratio: float
    hit_count: float
    rare2: float
    ss: float
    tfidf_cos: float


class PredictRequest(BaseModel):
    records: List[FeatureInput]


class PredictResponse(BaseModel):
    predictions: List[float]


@app.get("/health")
def health() -> dict:
    """Simple health check."""
    return {"status": "ok", "loaded": model_bundle is not None}


@app.post("/predict", response_model=PredictResponse)
def predict(req: PredictRequest):
    """Return calibrated score for each feature vector in the request."""
    if model_bundle is None:
        raise HTTPException(status_code=503, detail="Model not loaded")

    # assemble matrix
    X = np.array(
        [
            [
                r.amt_ratio,
                r.dd_ratio,
                r.hit_count,
                r.rare2,
                r.ss,
                r.tfidf_cos,
            ]
            for r in req.records
        ]
    )

    # apply regressor then calibrator if available
    reg = model_bundle.get("regressor")
    if reg is None:
        raise HTTPException(status_code=500, detail="Regressor missing from model bundle")

    raw = reg.predict(X)

    cal = model_bundle.get("calibrator")
    if cal is not None:
        try:
            probs = cal.predict(raw)
        except Exception:
            # some calibrators implement predict_proba
            probs = cal.predict_proba(raw)[:, 1]
    else:
        probs = raw

    return PredictResponse(predictions=probs.tolist())
