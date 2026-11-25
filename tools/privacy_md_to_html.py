# SPDX-FileCopyrightText: 2026 Luis Jesus
# SPDX-License-Identifier: GPL-3.0-or-later
# Usage: python3 tools/privacy_md_to_html.py docs/PRIVACY_POLICY.md sites/privacy.html ["Page title"]
#        python3 tools/privacy_md_to_html.py docs/DATA_DELETION.md sites/delete-data.html "Cancionero — Delete your data / Eliminar tus datos"
import re, html, sys
src = open(sys.argv[1], encoding='utf-8').read()
src = re.sub(r'<!--.*?-->\s*', '', src, flags=re.S)   # drop the internal draft comment
def inline(t):
    t = html.escape(t, quote=False)
    t = re.sub(r'`([^`]+)`', r'<code>\1</code>', t)
    t = re.sub(r'\*\*([^*]+)\*\*', r'<strong>\1</strong>', t)
    t = re.sub(r'(?<![\w*])\*([^*]+)\*(?![\w*])', r'<em>\1</em>', t)
    t = re.sub(r'\[([^\]]+)\]\(([^)\s]+)\)', r'<a href="\2">\1</a>', t)
    t = re.sub(r'(?<!["=>])(https?://[^\s<)]+)', r'<a href="\1">\1</a>', t)
    return t
out, para, inlist, section_open = [], [], False, False
table = []
def flush_table():
    global table
    if not table: return
    rows = [[c.strip() for c in r.strip().strip('|').split('|')] for r in table if not re.match(r'^\|[\s:|-]+\|$', r.strip())]
    out.append('<div class="tw"><table>')
    out.append('<tr>' + ''.join('<th>' + inline(c) + '</th>' for c in rows[0]) + '</tr>')
    for r in rows[1:]:
        out.append('<tr>' + ''.join('<td>' + inline(c) + '</td>' for c in r) + '</tr>')
    out.append('</table></div>')
    table = []
def flush():
    global para
    if para: out.append('<p>' + inline(' '.join(para)) + '</p>'); para = []
def endlist():
    global inlist
    if inlist: out.append('</ul>'); inlist = False
first_h1 = True
for line in src.splitlines():
    s = line.rstrip()
    if s.startswith('|'): flush(); endlist(); table.append(s); continue
    if table: flush_table()
    if not s.strip(): flush(); endlist(); continue
    if s.strip() == '---': flush(); endlist(); continue   # language split handled by the H1s
    m = re.match(r'(#{1,3}) (.*)', s)
    if m:
        flush(); endlist()
        lvl, txt = len(m[1]), m[2]
        if lvl == 1:
            if section_open: out.append('</section>')
            lang = 'en' if first_h1 else 'es'
            out.append(f'<section id="{lang}" lang="{lang}">'); section_open = True; first_h1 = False
        out.append(f'<h{lvl}>{inline(txt)}</h{lvl}>'); continue
    m = re.match(r'- (.*)', s)
    if m:
        flush()
        if not inlist: out.append('<ul>'); inlist = True
        out.append('<li>' + inline(m[1]) + '</li>'); continue
    para.append(s.strip())
flush(); endlist(); flush_table()
if section_open: out.append('</section>')
body = '\n'.join(out)
title = html.escape(sys.argv[3]) if len(sys.argv) > 3 else 'Cancionero — Privacy Policy / Política de privacidad'
page = f'''<!doctype html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>{title}</title>
<!-- Generated from docs/PRIVACY_POLICY.md. Edit the Markdown file, then regenerate this page. -->
<style>
:root {{ color-scheme: light; --bg:#faf3e3; --ink:#2b2118; --soft:#6b5a48; --line:#e0cfa8; --link:#8a4b1c; }}
@media (prefers-color-scheme: dark) {{ :root:not([data-theme="light"]) {{ color-scheme: dark; --bg:#17202e; --ink:#f1e6cf; --soft:#b9a98d; --line:#34405a; --link:#e3b562; }} }}
body {{ margin:0; background:var(--bg); color:var(--ink); font:17px/1.6 Georgia, "Times New Roman", serif; }}
main {{ max-width: 46rem; margin: 0 auto; padding: 1.5rem 1rem 4rem; }}
nav {{ display:flex; gap:1rem; padding-bottom:.75rem; border-bottom:1px solid var(--line); font-family: system-ui, sans-serif; font-size:.95rem; }}
a {{ color:var(--link); word-break: break-word; }}
h1 {{ font-size:1.8rem; margin:1.5rem 0 .25rem; }} h2 {{ font-size:1.25rem; margin:2rem 0 .5rem; }} h3 {{ font-size:1.05rem; margin:1.4rem 0 .4rem; }}
section + section {{ margin-top:3.5rem; padding-top:1rem; border-top:2px solid var(--line); }}
table {{ border-collapse:collapse; font-size:.95rem; }} th,td {{ border:1px solid var(--line); padding:.4rem .6rem; text-align:left; vertical-align:top; }} .tw {{ overflow-x:auto; }}
code {{ background:rgba(127,127,127,.15); padding:.05em .3em; border-radius:4px; font-size:.9em; }}
em {{ color:var(--soft); }}
</style>
</head>
<body>
<main>
<nav><a href="#en">English</a><a href="#es">Español</a></nav>
{body}
</main>
</body>
</html>
'''
open(sys.argv[2], 'w', encoding='utf-8').write(page)
