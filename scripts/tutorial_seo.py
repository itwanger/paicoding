"""Curated tutorial descriptions. Missing entries must be reviewed before import."""
import json
import re
from functools import lru_cache
from pathlib import Path

@lru_cache(maxsize=1)
def descriptions():
    return json.loads(Path(__file__).with_name('tutorial_descriptions.json').read_text())

def description_for(slug, title=''):
    description = descriptions().get(slug)
    if not isinstance(description, str) or not 50 <= len(description) <= 150:
        raise ValueError(f'{slug}: add a reviewed 50–150 character description to tutorial_descriptions.json before syncing')
    if description != description.strip() or re.search(r'https?://|[<>\n\r]|!\[|\]\(|```', description):
        raise ValueError(f'{slug}: description must be plain text without links or Markdown')
    if description == title.strip():
        raise ValueError(f'{slug}: description must summarize the article, not repeat its title')
    return description
