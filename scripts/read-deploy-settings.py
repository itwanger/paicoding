#!/usr/bin/env python3
"""Emit connection settings to deploy.sh without evaluating dotenv as shell code."""
import os
from pathlib import Path
values = {}
for path in (Path('.env'), Path('.env.local')):
    if path.is_file():
        for line in path.read_text().splitlines():
            key, sep, value = line.partition('=')
            if sep and not key.lstrip().startswith('#'):
                values[key.strip().removeprefix('export ')] = value.strip().strip('"\'')
for key in ('DEPLOY_SERVER_HOST', 'DEPLOY_SERVER_USER', 'DEPLOY_SERVER_KEY', 'DEPLOY_TARGET_DIR'):
    value = os.environ.get(key, values.get(key, ''))
    if value and '\n' not in value and '\r' not in value:
        print(key)
        print(value)
