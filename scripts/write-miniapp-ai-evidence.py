#!/usr/bin/env python3
"""Write sanitized WeChat miniapp AI validate evidence files."""

import json
import os
import sys
from datetime import datetime


PASS_VALUES = {"pass", "passed", "success", "succeeded", "ok", "true", "done"}
FAIL_VALUES = {"fail", "failed", "failure", "error", "false", "timeout", "crash"}


def read_path(obj, expr):
    cur = obj
    for part in expr.split("."):
        if not part:
            continue
        if isinstance(cur, list):
            cur = cur[int(part)]
        elif isinstance(cur, dict):
            cur = cur.get(part)
        else:
            return ""
        if cur is None:
            return ""
    if isinstance(cur, bool):
        return "true" if cur else "false"
    return "" if cur is None else str(cur)


def walk(obj, path=()):
    if isinstance(obj, dict):
        for key, value in obj.items():
            yield from walk(value, path + (str(key),))
    elif isinstance(obj, list):
        for index, value in enumerate(obj):
            yield from walk(value, path + (str(index),))
    else:
        yield path, obj


def subtree_values(obj):
    for _path, value in walk(obj):
        if isinstance(value, bool):
            yield "true" if value else "false"
        elif value is not None:
            yield str(value).strip().lower()


def section_passed(obj, needles):
    found_pass = False
    found_section = False

    def visit(node, path=()):
        nonlocal found_pass, found_section
        joined = ".".join(path).lower()
        matched = any(needle in joined for needle in needles)
        if matched:
            found_section = True
            values = list(subtree_values(node))
            if any(value in FAIL_VALUES or "error" in value or "failed" in value for value in values):
                return
            if any(value in PASS_VALUES for value in values):
                found_pass = True
        if isinstance(node, dict):
            for key, value in node.items():
                visit(value, path + (str(key),))
        elif isinstance(node, list):
            for index, value in enumerate(node):
                visit(value, path + (str(index),))

    visit(obj)
    return found_section and found_pass


def main():
    if len(sys.argv) != 3:
        print("usage: write-miniapp-ai-evidence.py validate-report.json evidence-dir", file=sys.stderr)
        return 2

    report_path, evidence_dir = sys.argv[1], sys.argv[2]
    os.makedirs(evidence_dir, exist_ok=True)
    with open(report_path) as f:
        data = json.load(f)

    generated_at = datetime.now().strftime("%Y-%m-%d %H:%M:%S %Z").strip()
    preview_path = os.path.join(evidence_dir, "ai-preview.validate.md")
    with open(preview_path, "w") as f:
        f.write("# AI preview validate evidence\n\n")
        f.write(f"- generatedAt: {generated_at}\n")
        f.write("- command: RUN_AI_VALIDATE=true scripts/preflight-miniapp.sh\n")
        f.write(f"- summary.errors: {read_path(data, 'summary.errors')}\n")
        f.write(f"- summary.warnings: {read_path(data, 'summary.warnings')}\n")
        f.write(f"- summary.buildStatus: {read_path(data, 'summary.buildStatus')}\n")
        f.write(f"- build.stage: {read_path(data, 'build.stage')}\n")

    execute_passed = section_passed(data, ("execute", "invoke"))
    render_passed = section_passed(data, ("render", "snapshot"))
    if execute_passed and render_passed:
        execute_render_path = os.path.join(evidence_dir, "ai-execute-render.validate.md")
        with open(execute_render_path, "w") as f:
            f.write("# AI execute/render evidence\n\n")
            f.write(f"- generatedAt: {generated_at}\n")
            f.write("- source: validate-report.json\n")
            f.write("- execute: pass\n")
            f.write("- render: pass\n")
            f.write("- status: pass\n")

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
