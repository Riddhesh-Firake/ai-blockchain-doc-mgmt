FROM python:3.9-slim
WORKDIR /app

# Install system dependencies for OCR
RUN apt-get update && apt-get install -y tesseract-ocr && rm -rf /var/lib/apt/lists/*

# The Ultimate Permission Fix: 
# 1. Force all AI libraries to use the universally writable /tmp folder
ENV HF_HOME="/tmp/huggingface"
ENV TRANSFORMERS_CACHE="/tmp/huggingface"
ENV HUGGINGFACE_HUB_CACHE="/tmp/huggingface"
ENV XDG_CACHE_HOME="/tmp/huggingface"

# 2. Create the default root cache folder anyway and unlock it for all OpenShift users
RUN mkdir -p /.cache && chmod -R 777 /.cache

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

# Unlock the app folder just in case FastAPI needs to write temp files here
RUN chmod -R 777 /app

EXPOSE 5000
CMD ["python", "main.py"]
