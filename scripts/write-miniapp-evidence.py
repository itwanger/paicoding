#!/usr/bin/env python3
"""Write sanitized WeChat miniapp handoff evidence records."""

import argparse
import os
import re
import sys
from datetime import datetime


GROUP_TITLES = {
    "device-login": "Device Login Evidence",
    "device-avatar-profile": "Device Avatar Profile Evidence",
    "device-article-flow": "Device Article Flow Evidence",
    "device-interactions": "Device Interactions Evidence",
    "wechat-domain-privacy": "WeChat Domain Privacy Evidence",
}

SENSITIVE_PATTERN = re.compile(
    r"authorization\s*:\s*bearer\s+[A-Za-z0-9._~+/=-]{8,}"
    r"|\bbearer\s+eyJ[A-Za-z0-9._~+/=-]{8,}"
    r"|\"?(appsecret|app_secret|app secret|token|cookie|openid|session_key|secret)\"?\s*[:=]\s*\"?[^\"\s，,。]+"
    r"|sk-[A-Za-z0-9_-]{12,}",
    re.IGNORECASE,
)
PLACEHOLDER_PATTERN = re.compile(r"未执行|未验证|未确认|TODO|TBD|待补", re.IGNORECASE)


def parse_detail(value):
    if "=" not in value:
        raise argparse.ArgumentTypeError("--detail must use key=value")
    key, val = value.split("=", 1)
    key = key.strip()
    val = val.strip()
    if not key or not re.fullmatch(r"[A-Za-z0-9_.-]{1,40}", key):
        raise argparse.ArgumentTypeError("--detail key must be 1-40 chars: letters, numbers, dot, underscore or dash")
    if not val:
        raise argparse.ArgumentTypeError("--detail value must not be empty")
    return key, val


def validate_text(name, value, allow_empty=False):
    text = value or ""
    if not allow_empty and not text.strip():
        raise ValueError(f"{name} must not be empty")
    if SENSITIVE_PATTERN.search(text):
        raise ValueError(f"{name} looks like it contains a secret/token/openid; write a redacted value instead")
    return text.strip()


def validate_pass_record(args, details):
    if args.status != "pass":
        return
    if PLACEHOLDER_PATTERN.search(args.result):
        raise ValueError("--status pass cannot be used with pending placeholders in --result")
    if len(args.result.strip()) < 12:
        raise ValueError("--status pass requires a concrete --result description")
    if not re.fullmatch(r"[A-Za-z0-9]{6}", args.app_id_suffix or ""):
        raise ValueError("--status pass requires --app-id-suffix with exactly 6 letters/numbers")
    if args.group.startswith("device-") and not args.device:
        raise ValueError(f"--status pass for {args.group} requires --device")
    if args.group == "wechat-domain-privacy":
        required = {"requestDomain", "uploadDomain", "downloadDomain", "privacy"}
        keys = {key for key, _value in details}
        missing = sorted(required - keys)
        if missing:
            raise ValueError("--status pass for wechat-domain-privacy requires --detail " + ",".join(missing))


def build_output(args, details):
    checked_at = args.checked_at or datetime.now().strftime("%Y-%m-%d %H:%M:%S CST")
    lines = [
        f"# {GROUP_TITLES[args.group]}",
        "",
        f"- status: {args.status}",
        f"- checkedAt: {checked_at}",
        f"- appIdSuffix: {args.app_id_suffix or 'redacted'}",
        f"- env: {args.env}",
    ]
    if args.device:
        lines.append(f"- device: {args.device}")
    for key, value in details:
        lines.append(f"- {key}: {value}")
    lines.extend([
        f"- result: {args.result}",
        f"- redaction: {args.redaction}",
        "",
    ])
    return "\n".join(lines)


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--group", required=True, choices=sorted(GROUP_TITLES))
    parser.add_argument("--status", required=True, choices=("pass", "fail", "pending"))
    parser.add_argument("--env", required=True, choices=("dev", "pre", "prod", "trial", "release"))
    parser.add_argument("--result", required=True)
    parser.add_argument("--app-id-suffix", default="")
    parser.add_argument("--device", default="")
    parser.add_argument("--redaction", default="已打码用户昵称、头像、openid、token、Cookie、手机号和后台账号信息。")
    parser.add_argument("--checked-at", default="")
    parser.add_argument("--detail", action="append", type=parse_detail, default=[])
    parser.add_argument("--output-dir", default=os.path.join("paicoding-miniapp", "evidence"))
    args = parser.parse_args(argv)

    details = list(args.detail)
    try:
        args.result = validate_text("--result", args.result)
        args.redaction = validate_text("--redaction", args.redaction)
        args.app_id_suffix = validate_text("--app-id-suffix", args.app_id_suffix, allow_empty=True)
        args.device = validate_text("--device", args.device, allow_empty=True)
        args.checked_at = validate_text("--checked-at", args.checked_at, allow_empty=True)
        details = [(key, validate_text(f"--detail {key}", value)) for key, value in details]
        validate_pass_record(args, details)
    except ValueError as exc:
        print(f"write-miniapp-evidence: {exc}", file=sys.stderr)
        return 2

    os.makedirs(args.output_dir, exist_ok=True)
    stamp = datetime.now().strftime("%Y%m%d-%H%M%S")
    path = os.path.join(args.output_dir, f"{args.group}.{stamp}.md")
    content = build_output(args, details)
    with open(path, "w") as f:
        f.write(content)
    print(path)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
