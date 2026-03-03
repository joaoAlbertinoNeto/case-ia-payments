FROM python:3.12-slim

WORKDIR /app

COPY requirements.txt ./
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

# By default the model path will be searched relative to the working directory.
# You can override with MODEL_PATH environment variable.

CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
