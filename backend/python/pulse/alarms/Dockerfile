FROM python:3.12-slim

WORKDIR /app
COPY requirements.txt .
RUN pip install --no-cache-dir --force-reinstall -r requirements.txt

COPY alarm_ingest.py .

# ./files is where reports are dropped, processed, failed, and logged.
# Mount this as a volume so its contents persist outside the container.
VOLUME ["/app/files"]

CMD ["python", "alarm_ingest.py"]