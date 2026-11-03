#!/usr/bin/env python3
"""Fail CI when Android manifest or FileProvider exposure drifts from the security baseline."""
from pathlib import Path
import sys
import xml.etree.ElementTree as ET

ANDROID_NS = "{http://schemas.android.com/apk/res/android}"
ROOT = Path(__file__).resolve().parents[1]
manifest_path = ROOT / "app/src/main/AndroidManifest.xml"
paths_path = ROOT / "app/src/main/res/xml/file_paths.xml"

def require(condition: bool, message: str) -> None:
    if not condition:
        print(f"FAIL: {message}", file=sys.stderr)
        raise SystemExit(1)

manifest = ET.parse(manifest_path).getroot()
application = manifest.find("application")
require(application is not None, "Android application element is missing")
require(application.get(ANDROID_NS + "allowBackup") == "false", "allowBackup must remain false")
require(application.get(ANDROID_NS + "usesCleartextTraffic") == "false", "cleartext network traffic must be disabled")

providers = [node for node in application.findall("provider") if node.get(ANDROID_NS + "name", "").endswith("FileProvider")]
require(len(providers) == 1, "exactly one FileProvider must be declared")
provider = providers[0]
require(provider.get(ANDROID_NS + "exported") == "false", "FileProvider must not be exported")
require(provider.get(ANDROID_NS + "grantUriPermissions") == "true", "FileProvider must use explicit URI grants")

paths = ET.parse(paths_path).getroot()
allowed = {
    ("cache-path", "camera_capture/"),
    ("external-files-path", "Pictures/"),
    ("files-path", "media/support_attachments/"),
}
actual = {(node.tag, node.get("path")) for node in list(paths)}
require(actual == allowed, f"FileProvider paths are broader or unexpected: {sorted(actual)}")
require(all(path not in ("", ".", "../") for _, path in actual), "FileProvider must not expose broad filesystem roots")

print("PASS: Android manifest transport, backup, and FileProvider exposure checks")
