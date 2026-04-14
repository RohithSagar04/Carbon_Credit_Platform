from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
import random

app = FastAPI(title="Carbon AI Verification")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)


class VerifyPayload(BaseModel):
    co2Emission: float = Field(0.0, ge=0.0, description="Reported CO2 (tonnes or internal units)")


@app.post("/verify")
def verify_emission(payload: VerifyPayload):
    emission = float(payload.co2Emission)
    carbon_score = max(0, int(100 - emission))
    fraud_probability = random.uniform(0, 0.1)
    return {"carbonScore": carbon_score, "fraudProbability": fraud_probability}
