#!/usr/bin/env python3
"""Validate Android manifest and resource XML files without requiring Android SDK."""
from pathlib import Path
import sys
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
source = ROOT / "apps" / "android" / "app" / "src"
files = sorted(source.rglob("*.xml"))
if not files:
    print("FAIL: no Android XML files found")
    sys.exit(1)
errors = []
for file in files:
    try:
        ET.parse(file)
    except ET.ParseError as exc:
        errors.append((file.relative_to(ROOT), str(exc)))
if errors:
    for file, error in errors:
        print(f"FAIL: {file}: {error}")
    sys.exit(1)
print(f"PASS: Android XML parsing succeeded for {len(files)} files")
