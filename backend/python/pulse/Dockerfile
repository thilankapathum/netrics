FROM python:3.9-slim

WORKDIR /app

# Install system dependencies for numpy/pandas and PostgreSQL
RUN apt-get update && apt-get install -y \
    gcc \
    g++ \
    libopenblas-dev \
    libpq-dev \
    postgresql-client \
    && rm -rf /var/lib/apt/lists/*

# Copy requirements.txt
COPY requirements.txt .

# Force reinstall of numpy and pandas (no cache, no prebuilt incompatibilities)
RUN pip install --no-cache-dir --force-reinstall -r requirements.txt

# Copy your application
COPY kpi_processor_ltefdd_day.py .
COPY config-ltefdd-day.json .

# Create required directories
RUN mkdir -p /app/day-average/ltefdd/ftp /app/day-average/ltefdd/processed /app/day-average/ltefdd/temp /app/day-average/ltefdd/logs

ENV PYTHONUNBUFFERED=1

CMD ["python", "kpi_processor_ltefdd_day.py"]