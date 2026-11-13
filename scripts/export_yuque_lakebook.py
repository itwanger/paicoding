#!/usr/bin/env python3
"""Convert a Yuque .lakebook archive into a categorized Markdown tree."""

from __future__ import annotations

import argparse
import json
import os
import re
import subprocess
import tarfile
import tempfile
from datetime import date
from pathlib import Path
from urllib.parse import urlparse

import yaml
from lxml import etree, html


LEGACY_SLUG_ALIASES = {
    
}


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser()
    parser.add_argument("lakebook", type=Path)
    parser.add_argument("output", type=Path)
    parser.add_argument("--title", default="技术派")
    parser.add_argument(
        "--url-map",
        type=Path,
        help="YAML mapping from Yuque document slugs to paicoding URL names",
    )
    parser.add_argument("--base-url", default="https://paicoding.com")
    return parser.parse_args()


def safe_name(title: str) -> str:
    replacements = {"/": "／", "\\": "＼", ":": "：", "\0": ""}
    for source, target in replacements.items():
        title = title.replace(source, target)
    title = re.sub(r"[\r\n\t]+", " ", title).strip(" .")
    return title or "无标题"


def read_archive(lakebook: Path) -> tuple[dict, dict[str, dict]]:
    with tempfile.TemporaryDirectory(prefix="paicoding-lakebook-") as temp_dir:
        temp = Path(temp_dir)
        with tarfile.open(lakebook) as archive:
            for member in archive.getmembers():
                target = (temp / member.name).resolve()
                if temp.resolve() not in target.parents:
                    raise RuntimeError(f"Unsafe path in lakebook: {member.name}")
            archive.extractall(temp)

        roots = [path for path in temp.iterdir() if path.is_dir()]
        if len(roots) != 1:
            raise RuntimeError(f"Unexpected lakebook layout: {roots}")

        root = roots[0]
        meta_wrapper = json.loads((root / "$meta.json").read_text(encoding="utf-8"))
        meta = json.loads(meta_wrapper["meta"])
        documents: dict[str, dict] = {}
        for path in root.glob("*.json"):
            if path.name == "$meta.json":
                continue
            wrapper = json.loads(path.read_text(encoding="utf-8"))
            document = wrapper["doc"]
            documents[document["slug"]] = document
        return meta, documents


def build_paths(nodes: list[dict], output: Path) -> dict[str, Path]:
    by_uuid = {node.get("uuid", ""): node for node in nodes if node.get("uuid")}
    paths: dict[str, Path] = {}

    def ancestor_directory(node: dict) -> Path:
        parts: list[str] = []
        parent_uuid = node.get("parent_uuid") or ""
        while parent_uuid:
            parent = by_uuid[parent_uuid]
            parts.append(safe_name(parent["title"]))
            parent_uuid = parent.get("parent_uuid") or ""
        return output.joinpath(*reversed(parts))

    seen: dict[str, str] = {}
    for node in nodes:
        if node.get("type") != "DOC":
            continue
        parent = ancestor_directory(node)
        title = safe_name(node["title"])
        if node.get("child_uuid"):
            path = parent / title / "README.md"
        else:
            path = parent / f"{title}.md"

        key = str(path).casefold()
        if key in seen:
            suffix = node["url"]
            path = path.with_name(f"{path.stem}-{suffix}{path.suffix}")
            key = str(path).casefold()
        seen[key] = node["url"]
        paths[node["url"]] = path
    return paths


def load_published_urls(
    mapping_path: Path | None,
    base_url: str,
    document_slugs: set[str],
) -> dict[str, str]:
    if mapping_path is None:
        return {}

    raw_mapping = yaml.safe_load(mapping_path.read_text(encoding="utf-8"))
    if not isinstance(raw_mapping, dict):
        raise RuntimeError(f"URL mapping must be a YAML object: {mapping_path}")

    mapping = {str(source): str(target).strip("/") for source, target in raw_mapping.items()}
    missing = sorted(document_slugs - set(mapping))
    extra = sorted(set(mapping) - document_slugs)
    if missing or extra:
        raise RuntimeError(f"URL mapping mismatch, missing={missing}, extra={extra}")

    invalid = sorted(target for target in mapping.values() if not re.fullmatch(r"[a-z0-9]+(?:-[a-z0-9]+)*", target))
    if invalid:
        raise RuntimeError(f"Invalid URL names: {invalid}")
    if len(set(mapping.values())) != len(mapping):
        duplicates = sorted({target for target in mapping.values() if list(mapping.values()).count(target) > 1})
        raise RuntimeError(f"Duplicate URL names: {duplicates}")

    root = base_url.rstrip("/")
    return {source: f"{root}/{target}" for source, target in mapping.items()}


def rewrite_internal_link(
    href: str,
    current: Path,
    paths: dict[str, Path],
    published_urls: dict[str, str],
) -> str:
    parsed = urlparse(href)
    path_parts = [part for part in parsed.path.split("/") if part]
    if len(path_parts) < 3:
        return href
    slug = LEGACY_SLUG_ALIASES.get(path_parts[2], path_parts[2])
    target = paths.get(slug)
    if target is None:
        return href

    if published_urls:
        destination = published_urls[slug]
        return f"{destination}#{parsed.fragment}" if parsed.fragment else destination
    if target == current and parsed.fragment:
        return f"#{parsed.fragment}"
    relative = Path(os.path.relpath(target, current.parent)).as_posix()
    relative = relative.replace(" ", "%20").replace("(", "%28").replace(")", "%29")
    return f"{relative}#{parsed.fragment}" if parsed.fragment else relative


def clean_html(
    body: str,
    current: Path,
    paths: dict[str, Path],
    published_urls: dict[str, str],
) -> str:
    root = html.fragment_fromstring(body, create_parent="div")

    for element in list(root.iterdescendants()):
        classes = set((element.get("class") or "").split())
        if "ne-quote" in classes or "ne-alert" in classes:
            element.tag = "blockquote"
        elif classes & {"ne-localdoc", "ne-yuque", "ne-bookmark", "ne-text-diagram"}:
            element.tag = "p"

        if element.tag == "img" and element.get("src") == "/spring-blog/imgs/230323/00.gif":
            element.set("src", "https://spring.hhui.top/spring-blog/imgs/230323/00.gif")
        if element.tag == "a" and element.get("href"):
            original_href = element.get("href", "")
            annotation = element.text_content().strip()
            if annotation.startswith("@") and original_href.replace("%20", "").strip() == "/" + annotation[1:]:
                element.tag = "code"
                for child in list(element):
                    element.remove(child)
                element.text = annotation
                element.attrib.clear()
                continue
            rewritten_href = rewrite_internal_link(original_href, current, paths, published_urls)
            element.set("href", rewritten_href)
            visible_text = element.text_content().strip()
            if rewritten_href != original_href and visible_text == original_href:
                path_parts = [part for part in urlparse(original_href).path.split("/") if part]
                slug = LEGACY_SLUG_ALIASES.get(path_parts[2], path_parts[2])
                target = paths[slug]
                label = target.parent.name if target.name == "README.md" else target.stem
                for child in list(element):
                    element.remove(child)
                element.text = label
            elif visible_text.startswith(("http://", "https://", "www.")):
                for child in list(element):
                    element.remove(child)
                element.text = None
                code = etree.Element("code")
                code.text = visible_text
                element.append(code)

    for element in list(root.iterdescendants()):
        classes = set((element.get("class") or "").split())
        if element.tag == "span" or "lake-content" in classes:
            element.drop_tag()
        elif element.tag == "ul" and "ne-list-wrap" in classes:
            element.drop_tag()

    for element in root.iterdescendants():
        classes = set((element.get("class") or "").split())
        if element.tag == "a":
            keep = {"href", "title"}
        elif element.tag == "img":
            keep = {"src", "alt", "title"}
        elif element.tag == "pre":
            languages = sorted(c.removeprefix("language-") for c in classes if c.startswith("language-"))
            if not languages and element.get("data-language"):
                languages = [element.get("data-language")]
            element.attrib.clear()
            if languages:
                element.set("class", " ".join(languages))
            continue
        else:
            keep = set()
        for attribute in list(element.attrib):
            if attribute not in keep:
                del element.attrib[attribute]

    return "".join(etree.tostring(child, encoding="unicode", method="html") for child in root)


def html_to_markdown(cleaned_html: str) -> str:
    result = subprocess.run(
        ["pandoc", "-f", "html", "-t", "gfm", "--wrap=none"],
        input=cleaned_html,
        text=True,
        capture_output=True,
        check=True,
    )
    markdown = result.stdout.strip()
    markdown = re.sub(r"\n{4,}", "\n\n\n", markdown)
    return markdown


def board_text(value: str) -> str:
    if not value:
        return ""
    fragment = html.fragment_fromstring(value, create_parent="div")
    return " ".join(fragment.text_content().replace("\u200b", "").split())


def board_to_markdown(body: str) -> str:
    board = json.loads(body)
    items = board.get("diagramData", {}).get("body", [])
    lines: list[str] = []
    used_ids: set[str] = set()

    def append_children(children: list[dict], depth: int) -> None:
        for child in children:
            text = board_text(child.get("html", ""))
            if text:
                lines.append(f"{'  ' * depth}- {text}")
            append_children(child.get("children", []), depth + 1)

    for item in items:
        if item.get("type") != "mindmap":
            continue
        title = board_text(item.get("html", "")) or "画板内容"
        lines.extend([f"## {title}", ""])
        for child in item.get("children", []):
            child_title = board_text(child.get("html", ""))
            if child_title:
                lines.extend([f"### {child_title}", ""])
            append_children(child.get("children", []), 0)
            lines.append("")
        used_ids.add(item.get("id", ""))

    extras: list[str] = []
    for item in items:
        if item.get("id") in used_ids or item.get("type") == "line":
            continue
        if item.get("type") == "group":
            candidates = item.get("children", [])
        else:
            candidates = [item]
        for candidate in candidates:
            text = board_text(candidate.get("html", ""))
            if text and text not in extras:
                extras.append(text)
    if extras:
        lines.extend(["## 画板补充节点", ""])
        lines.extend(f"- {text}" for text in extras)
        lines.append("")

    return "\n".join(lines).strip()


def markdown_link(path: Path, root: Path) -> str:
    relative = path.relative_to(root).as_posix()
    relative = relative.replace(" ", "%20").replace("(", "%28").replace(")", "%29")
    return relative


def write_indexes(
    output: Path,
    title: str,
    source: str,
    nodes: list[dict],
    paths: dict[str, Path],
    published_urls: dict[str, str],
    document_count: int,
) -> None:
    readme = (
        f"# {title}\n\n"
        f"本目录由语雀知识库导出并转换为 Markdown，共 {document_count} 篇正文。\n\n"
        f"- 原知识库：[语雀原知识库]({source})\n"
        f"- 导出日期：{date.today().isoformat()}\n"
        f"- 目录索引：[SUMMARY.md](SUMMARY.md)\n"
        f"- 技术派 URL 与导入清单：[URL-MAPPING.md](URL-MAPPING.md)\n"
        "- 图片：保留原文中的语雀与作者 CDN 地址\n"
    )
    (output / "README.md").write_text(readme, encoding="utf-8")

    lines = [f"# {title} 目录", ""]
    for node in nodes:
        indent = "  " * int(node.get("level", 0))
        title_text = node["title"].replace("[", "\\[").replace("]", "\\]")
        if node.get("type") == "DOC":
            target = markdown_link(paths[node["url"]], output)
            lines.append(f"{indent}- [{title_text}]({target})")
        else:
            lines.append(f"{indent}- {title_text}")
    lines.append("")
    (output / "SUMMARY.md").write_text("\n".join(lines), encoding="utf-8")

    if published_urls:
        mapping_lines = [
            f"# {title} 技术派 URL 与导入清单",
            "",
            "正文中的知识库内部引用已按本表替换为技术派最终地址。若导入时修改 URL 命名，请先更新本次使用的 URL 映射文件，再重新执行导出脚本。",
            "",
            "| 导入 | 分类 | 文档 | URL 命名 | 最终地址 |",
            "| --- | --- | --- | --- | --- |",
        ]
        categories: list[str] = []
        by_uuid = {node.get("uuid", ""): node for node in nodes if node.get("uuid")}
        for node in nodes:
            if node.get("type") != "DOC":
                continue
            parent_titles: list[str] = []
            parent_uuid = node.get("parent_uuid") or ""
            while parent_uuid:
                parent = by_uuid[parent_uuid]
                parent_titles.append(parent["title"])
                parent_uuid = parent.get("parent_uuid") or ""
            category = " / ".join(reversed(parent_titles)) or "根目录"
            local_target = markdown_link(paths[node["url"]], output)
            title_text = node["title"].replace("|", "\\|")
            category_text = category.replace("|", "\\|")
            final_url = published_urls[node["url"]]
            url_name = final_url.rsplit("/", 1)[-1]
            mapping_lines.append(
                f"| - [ ] | {category_text} | [{title_text}]({local_target}) | `{url_name}` | [{final_url}]({final_url}) |"
            )
        mapping_lines.append("")
        (output / "URL-MAPPING.md").write_text("\n".join(mapping_lines), encoding="utf-8")


def main() -> None:
    args = parse_args()
    meta, documents = read_archive(args.lakebook)
    toc = yaml.safe_load(meta["book"]["tocYml"])
    nodes = [node for node in toc if node.get("type") in {"DOC", "TITLE"}]
    paths = build_paths(nodes, args.output)

    toc_slugs = {node["url"] for node in nodes if node.get("type") == "DOC"}
    if toc_slugs != set(documents):
        missing = sorted(toc_slugs - set(documents))
        extra = sorted(set(documents) - toc_slugs)
        raise RuntimeError(f"Lakebook mismatch, missing={missing}, extra={extra}")

    published_urls = load_published_urls(args.url_map, args.base_url, toc_slugs)

    args.output.mkdir(parents=True, exist_ok=True)
    for node in nodes:
        if node.get("type") != "DOC":
            continue
        document = documents[node["url"]]
        destination = paths[node["url"]]
        destination.parent.mkdir(parents=True, exist_ok=True)
        if document.get("format") == "lakeboard":
            markdown = board_to_markdown(document["body"])
        else:
            cleaned = clean_html(document["body"], destination, paths, published_urls)
            markdown = html_to_markdown(cleaned)
        content = f"# {document['title']}\n\n{markdown}\n"
        destination.write_text(content, encoding="utf-8")

    source = meta["book"].get("path", "")
    write_indexes(args.output, args.title, source, nodes, paths, published_urls, len(documents))
    print(f"Exported {len(documents)} documents to {args.output}")


if __name__ == "__main__":
    main()
