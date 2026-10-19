#!/usr/bin/env python3
"""Upload market-research report pdf/docx to MinIO and update file_storage sizes."""

from __future__ import annotations

import json
import sys
from pathlib import Path

try:
    import pymysql
    from minio import Minio
except ImportError as exc:
    print("Missing dependency:", exc, file=sys.stderr)
    print("Run: pip install minio pymysql", file=sys.stderr)
    sys.exit(1)

ROOT = Path(__file__).resolve().parents[1]
MANIFEST = ROOT / "manifest.json"
TARGET_IDS = {5021, 5022}

MINIO_ENDPOINT = "localhost:9000"
MINIO_ACCESS = "minioadmin"
MINIO_SECRET = "minioadmin"
MINIO_SECURE = False

DB_HOST = "localhost"
DB_PORT = 3306
DB_USER = "root"
DB_PASSWORD = "root"
DB_NAME = "b1_dev"


def load_entries() -> list[dict]:
    data = json.loads(MANIFEST.read_text(encoding="utf-8"))
    return [e for e in data["files"] if e["id"] in TARGET_IDS]


def upload_and_update() -> None:
    client = Minio(
        MINIO_ENDPOINT,
        access_key=MINIO_ACCESS,
        secret_key=MINIO_SECRET,
        secure=MINIO_SECURE,
    )

    conn = pymysql.connect(
        host=DB_HOST,
        port=DB_PORT,
        user=DB_USER,
        password=DB_PASSWORD,
        database=DB_NAME,
        charset="utf8mb4",
    )

    try:
        with conn.cursor() as cur:
            for entry in load_entries():
                local = (ROOT / entry["localPath"]).resolve()
                if not local.is_file():
                    raise FileNotFoundError(local)

                bucket = entry["bucket"]
                object_key = entry["objectKey"]
                content_type = entry["contentType"]
                file_id = entry["id"]
                size = local.stat().st_size

                if not client.bucket_exists(bucket):
                    client.make_bucket(bucket)

                client.fput_object(
                    bucket,
                    object_key,
                    str(local),
                    content_type=content_type,
                )
                print(f"[MinIO] {local.name} -> {bucket}/{object_key} ({size} bytes)")

                cur.execute(
                    """
                    UPDATE file_storage
                    SET file_size = %s, content_type = %s, original_name = %s
                    WHERE id = %s
                    """,
                    (size, content_type, entry["originalName"], file_id),
                )
                if cur.rowcount == 0:
                    print(f"[WARN] file_storage id={file_id} not found; skipped DB update")
                else:
                    print(f"[DB] file_storage id={file_id} file_size={size}")

        conn.commit()
    finally:
        conn.close()

    print("Sync complete.")


if __name__ == "__main__":
    upload_and_update()
