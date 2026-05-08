FROM python:3.9-slim
WORKDIR /app

# Install system dependencies for OCR
RUN apt-get update && apt-get install -y tesseract-ocr && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt
COPY . .
EXPOSE 5000
CMD ["python", "main.py"]