FROM python:3.12-slim

RUN useradd -m -u 1000 appuser
WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY alarm-puller.py .

RUN mkdir -p /data/state /data/incoming && chown -R appuser:appuser /data /app
USER appuser

ENV CONFIG_PATH=/app/config.yaml
ENV LOG_LEVEL=INFO
ENV PYTHONUNBUFFERED=1

CMD ["python", "alarm-puller.py"]
