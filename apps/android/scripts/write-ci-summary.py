#!/usr/bin/env python3
"""Create GitHub Actions summary and detailed, downloadable Android QA reports."""
from __future__ import annotations

from pathlib import Path
import json
import os
import re
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
APP_BUILD = ROOT / "app" / "build"
REPORT_DIR = ROOT / "ci-report"
REPORT_DIR.mkdir(parents=True, exist_ok=True)

STAGES = {
    "JVM Unit Tests": os.getenv("UNIT_OUTCOME", "not-run"),
    "Android Lint": os.getenv("LINT_OUTCOME", "not-run"),
    "Debug APK Build": os.getenv("APK_OUTCOME", "not-run"),
    "JaCoCo Coverage": os.getenv("COVERAGE_OUTCOME", "not-run"),
    "Instrumented UI Tests": os.getenv("UI_OUTCOME", "not-run"),
}


def safe_md(value: str) -> str:
    return str(value).replace("|", r"\|").replace("\r", " ").replace("\n", " ")


def empty_totals():
    return {"tests": 0, "passed": 0, "failures": 0, "errors": 0, "skipped": 0, "time": 0.0}


def junit_reports():
    files = []
    for base in (APP_BUILD / "test-results", APP_BUILD / "outputs" / "androidTest-results"):
        if base.exists():
            files.extend(base.rglob("TEST-*.xml"))
    return sorted(set(p for p in files if p.is_file()))


def parse_junit():
    totals = {"unit": empty_totals(), "instrumented": empty_totals()}
    cases = []
    report_files = junit_reports()
    for file in report_files:
        is_instrumented = "androidTest-results" in file.parts
        category = "instrumented" if is_instrumented else "unit"
        try:
            root = ET.parse(file).getroot()
        except (ET.ParseError, OSError):
            continue
        suites = [root] if root.tag.endswith("testsuite") else list(root.findall(".//testsuite"))
        for suite in suites:
            for case in suite.findall("testcase"):
                failure = case.find("failure")
                error = case.find("error")
                skipped = case.find("skipped")
                if failure is not None:
                    status = "FAIL"
                    detail = failure.attrib.get("message", "") or (failure.text or "")
                elif error is not None:
                    status = "ERROR"
                    detail = error.attrib.get("message", "") or (error.text or "")
                elif skipped is not None:
                    status = "SKIPPED"
                    detail = skipped.attrib.get("message", "") or (skipped.text or "")
                else:
                    status, detail = "PASS", ""
                try:
                    duration = float(case.attrib.get("time", 0.0))
                except ValueError:
                    duration = 0.0
                case_data = {
                    "test_type": "Instrumented UI" if is_instrumented else "JVM Unit",
                    "suite": suite.attrib.get("name", ""),
                    "classname": case.attrib.get("classname", ""),
                    "name": case.attrib.get("name", "(unnamed test)"),
                    "status": status,
                    "time_seconds": duration,
                    "detail": detail.strip()[:2500],
                    "source_file": str(file.relative_to(APP_BUILD)),
                }
                cases.append(case_data)
                bucket = totals[category]
                bucket["tests"] += 1
                bucket["failures"] += int(status == "FAIL")
                bucket["errors"] += int(status == "ERROR")
                bucket["skipped"] += int(status == "SKIPPED")
                bucket["passed"] += int(status == "PASS")
                bucket["time"] += duration
    cases.sort(key=lambda x: (x["status"] not in ("FAIL", "ERROR"), x["test_type"], x["classname"], x["name"]))
    return totals, cases, report_files


def coverage_metrics():
    report = APP_BUILD / "reports" / "jacoco" / "jacocoTestReport" / "jacocoTestReport.xml"
    counters = {}
    status = "Not generated"
    if report.exists():
        try:
            root = ET.parse(report).getroot()
            for kind in ("LINE", "BRANCH", "INSTRUCTION", "METHOD", "CLASS", "COMPLEXITY"):
                node = next((n for n in root.findall("counter") if n.attrib.get("type") == kind), None)
                if node is not None:
                    covered = int(node.attrib.get("covered", 0))
                    missed = int(node.attrib.get("missed", 0))
                    total = covered + missed
                    counters[kind.lower()] = {
                        "covered": covered,
                        "missed": missed,
                        "total": total,
                        "percent": round(covered / total * 100, 1) if total else 0.0,
                    }
            status = "Generated"
        except (ET.ParseError, OSError, ValueError):
            status = "Could not parse"
    android_cov = APP_BUILD / "reports" / "coverage" / "androidTest" / "debug" / "connected" / "index.html"
    instrumented_status = "Generated" if android_cov.exists() else "Not generated"
    return {"status": status, "counters": counters, "instrumented_status": instrumented_status}


def lint_issues():
    issues = []
    for file in APP_BUILD.rglob("lint-results*.xml"):
        try:
            root = ET.parse(file).getroot()
        except (ET.ParseError, OSError):
            continue
        for issue in root.findall(".//issue"):
            location = issue.find("location")
            issues.append({
                "severity": issue.attrib.get("severity", "Unknown"),
                "id": issue.attrib.get("id", ""),
                "message": issue.attrib.get("message", ""),
                "file": location.attrib.get("file", "") if location is not None else "",
                "line": location.attrib.get("line", "") if location is not None else "",
            })
    if issues:
        return issues
    # Some Android Gradle Plugin versions also emit only a text lint report.
    for file in APP_BUILD.rglob("lint-results-debug.txt"):
        try:
            data = file.read_text(encoding="utf-8", errors="replace")
        except OSError:
            continue
        for line in data.splitlines():
            match = re.search(r"\b(Error|Warning):\s*(.*?)\s+\[([^\]]+)\]", line)
            if match:
                issues.append({"severity": match.group(1), "id": match.group(3), "message": match.group(2), "file": "", "line": ""})
        if issues:
            break
    return issues


def compiler_warnings():
    logs_dir = REPORT_DIR / "logs"
    warnings = []
    if logs_dir.exists():
        for file in sorted(logs_dir.glob("*.log")):
            try:
                for line in file.read_text(encoding="utf-8", errors="replace").splitlines():
                    if re.search(r"(^|\s)w: .*deprecated|\bwarning:\s", line, re.IGNORECASE):
                        warnings.append({"log": file.name, "message": line.strip()[:500]})
            except OSError:
                pass
    return warnings


junit, cases, junit_files = parse_junit()
coverage = coverage_metrics()
lint = lint_issues()
compiler = compiler_warnings()
run_id = os.getenv("GITHUB_RUN_ID", "")
repo = os.getenv("GITHUB_REPOSITORY", "")
server = os.getenv("GITHUB_SERVER_URL", "https://github.com")
run_url = f"{server}/{repo}/actions/runs/{run_id}" if repo and run_id else ""

all_totals = empty_totals()
for bucket in junit.values():
    for key in all_totals:
        all_totals[key] += bucket[key]

lines = [
    "# FastLove Android — Detailed CI Test Report",
    "",
    f"- **Repository:** `{repo or 'local'}`",
    f"- **Branch / ref:** `{os.getenv('GITHUB_REF_NAME', 'local')}`",
    f"- **Commit:** `{os.getenv('GITHUB_SHA', 'unknown')}`",
    f"- **Run:** {f'[#' + os.getenv('GITHUB_RUN_NUMBER', '') + f']({run_url})' if run_url else 'local execution'}",
    f"- **Event:** `{os.getenv('GITHUB_EVENT_NAME', 'local')}`",
    "",
    "## Pipeline stage outcomes",
    "",
    "| Stage | Result |",
    "|---|---|",
]
for name, outcome in STAGES.items():
    label = "✅ PASS" if outcome == "success" else ("❌ FAIL" if outcome == "failure" else f"⚪ {outcome.upper()}")
    lines.append(f"| {name} | {label} |")
lines += [
    "",
    "## Test totals",
    "",
    "| Suite | Tests | Passed | Failed | Errors | Skipped | Duration |",
    "|---|---:|---:|---:|---:|---:|---:|",
    f"| JVM Unit | {junit['unit']['tests']} | {junit['unit']['passed']} | {junit['unit']['failures']} | {junit['unit']['errors']} | {junit['unit']['skipped']} | {junit['unit']['time']:.2f}s |",
    f"| Instrumented UI | {junit['instrumented']['tests']} | {junit['instrumented']['passed']} | {junit['instrumented']['failures']} | {junit['instrumented']['errors']} | {junit['instrumented']['skipped']} | {junit['instrumented']['time']:.2f}s |",
    f"| **Total** | **{all_totals['tests']}** | **{all_totals['passed']}** | **{all_totals['failures']}** | **{all_totals['errors']}** | **{all_totals['skipped']}** | **{all_totals['time']:.2f}s** |",
    "",
    f"- **JUnit XML files:** {len(junit_files)}",
    "",
    "## Code coverage (JaCoCo)",
    "",
    f"- **Unit-test report:** {coverage['status']}",
    f"- **Instrumented-test report:** {coverage['instrumented_status']}",
]
for kind, item in coverage["counters"].items():
    lines.append(f"- **{kind.title()} coverage:** {item['percent']:.1f}% ({item['covered']} covered / {item['total']} total; {item['missed']} missed)")
if not coverage["counters"]:
    lines.append("- No JaCoCo counters were available for this run.")
lines += [
    "",
    "## Android Lint findings",
    "",
    f"- **Issues parsed:** {len(lint)}",
    f"- **Errors:** {sum(1 for item in lint if item['severity'].lower() == 'error')}",
    f"- **Warnings:** {sum(1 for item in lint if item['severity'].lower() == 'warning')}",
]
if lint:
    lines += ["", "| Severity | Rule | Location | Finding |", "|---|---|---|---|"]
    for item in lint:
        location = item['file'] + (f":{item['line']}" if item['line'] else "")
        lines.append(f"| {safe_md(item['severity'])} | `{safe_md(item['id'])}` | `{safe_md(location)}` | {safe_md(item['message'])} |")
lines += ["", "## Compiler / deprecation warnings", "", f"- **Warnings captured from Gradle logs:** {len(compiler)}"]
for item in compiler:
    lines.append(f"- `{safe_md(item['log'])}` — {safe_md(item['message'])}")
lines += ["", "## Test case details", ""]
if cases:
    lines += ["| Test type | Result | Test class | Test case | Duration |", "|---|---|---|---|---:|"]
    for case in cases:
        icon = "✅" if case['status'] == 'PASS' else ("❌" if case['status'] in ('FAIL', 'ERROR') else "⏭️")
        lines.append(f"| {case['test_type']} | {icon} {case['status']} | `{safe_md(case['classname'] or case['suite'])}` | `{safe_md(case['name'])}` | {case['time_seconds']:.3f}s |")
    for case in cases:
        if case["detail"]:
            lines += ["", f"### {case['status']}: {case['classname']}.{case['name']}", "", "```text", case["detail"], "```"]
else:
    lines.append("No JUnit XML test cases were found. Check Gradle task outcomes and the uploaded raw test results.")
lines += [
    "",
    "## Downloaded report contents",
    "",
    "The artifact contains this Markdown and JSON report, raw JVM and instrumented JUnit XML, Gradle HTML test reports, JaCoCo XML/HTML, Android Lint reports, instrumented test outputs and Gradle console logs.",
    "",
    "The debug APK is published as a separate artifact only when every required quality-gate stage passes.",
]
report_text = "\n".join(lines) + "\n"
(REPORT_DIR / "fastlove-android-detailed-report.md").write_text(report_text, encoding="utf-8")
(REPORT_DIR / "fastlove-android-detailed-report.json").write_text(
    json.dumps({"stages": STAGES, "test_totals": junit, "combined_totals": all_totals, "test_cases": cases, "coverage": coverage, "lint": lint, "compiler_warnings": compiler}, ensure_ascii=False, indent=2),
    encoding="utf-8",
)

summary = [
    "## FastLove Android CI — Test Summary",
    "",
    f"**Run:** {f'[#' + os.getenv('GITHUB_RUN_NUMBER', '') + f']({run_url})' if run_url else 'local'}  ",
    f"**JVM Unit:** {junit['unit']['passed']} passed · {junit['unit']['failures']} failed · {junit['unit']['errors']} errors · {junit['unit']['skipped']} skipped  ",
    f"**Instrumented UI:** {junit['instrumented']['passed']} passed · {junit['instrumented']['failures']} failed · {junit['instrumented']['errors']} errors · {junit['instrumented']['skipped']} skipped  ",
]
line_cov = coverage["counters"].get("line")
summary.append(f"**Unit line coverage:** {line_cov['percent']:.1f}% ({line_cov['covered']}/{line_cov['total']})" if line_cov else "**Unit line coverage:** not available")
summary.append(f"**Android Lint:** {sum(1 for x in lint if x['severity'].lower() == 'error')} errors · {sum(1 for x in lint if x['severity'].lower() == 'warning')} warnings")
summary += ["", "### Pipeline stages", "", "| Stage | Result |", "|---|---|"]
for name, outcome in STAGES.items():
    summary.append(f"| {name} | {'✅' if outcome == 'success' else '❌' if outcome == 'failure' else '⚪'} `{outcome}` |")
summary += ["", "Download `fastlove-android-detailed-report-*` under this workflow run's **Artifacts** for every test case, failures, Lint findings, compiler warnings, coverage metrics, raw XML/HTML reports and Gradle logs."]
summary_text = "\n".join(summary) + "\n"
summary_path = os.environ.get("GITHUB_STEP_SUMMARY")
if summary_path:
    with open(summary_path, "a", encoding="utf-8") as handle:
        handle.write(summary_text)
else:
    print(summary_text)
