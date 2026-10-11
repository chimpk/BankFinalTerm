import re, os, sys, glob
import pathlib
ROOT=str(pathlib.Path(__file__).resolve().parent.parent/'app'/'src'/'main')
RES=ROOT+'/res'
java=[p for p in glob.glob(ROOT+'/java/**/*.java', recursive=True)]
xmls=[p for p in glob.glob(RES+'/**/*.xml', recursive=True)]+[ROOT+'/AndroidManifest.xml']

# ---- definitions ----
defs={k:set() for k in ['string','id','layout','drawable','menu','navigation','array','color','xml','style','mipmap']}
for p in xmls:
    rel=os.path.relpath(p,RES) if p.startswith(RES) else ''
    d=rel.split(os.sep)[0] if rel else ''
    base=os.path.splitext(os.path.basename(p))[0]
    for kind,folder in [('layout','layout'),('drawable','drawable'),('menu','menu'),('navigation','navigation'),('xml','xml'),('mipmap','mipmap-anydpi-v26')]:
        if d==folder: defs[kind].add(base)
    txt=open(p,encoding='utf-8').read()
    if d=='values':
        for m in re.finditer(r'<string name="([^"]+)"',txt): defs['string'].add(m.group(1))
        for m in re.finditer(r'<string-array name="([^"]+)"',txt): defs['array'].add(m.group(1))
        for m in re.finditer(r'<color name="([^"]+)"',txt): defs['color'].add(m.group(1))
        for m in re.finditer(r'<style name="([^"]+)"',txt): defs['style'].add(m.group(1).replace('.','_'))
    for m in re.finditer(r'@\+id/([A-Za-z0-9_]+)',txt): defs['id'].add(m.group(1))

# ---- usages ----
missing=[]
def need(kind,name,where):
    if name not in defs[kind]: missing.append((kind,name,where))
for p in java:
    t=open(p,encoding='utf-8').read()
    for m in re.finditer(r'(?<![.\w])R\.(string|id|layout|drawable|menu|navigation|array|color|xml|mipmap)\.([A-Za-z0-9_]+)',t):
        need(m.group(1),m.group(2),os.path.basename(p))
for p in xmls:
    t=open(p,encoding='utf-8').read()
    for m in re.finditer(r'@(string|drawable|color|menu|navigation|xml|mipmap|array)/([A-Za-z0-9_]+)',t):
        need(m.group(1),m.group(2),os.path.relpath(p,ROOT))
    for m in re.finditer(r'@id/([A-Za-z0-9_]+)',t): need('id',m.group(1),os.path.relpath(p,ROOT))
    for m in re.finditer(r'@style/([A-Za-z0-9_.]+)',t):
        n=m.group(1)
        if n.startswith(('Widget.Material3','Theme.Material3','TextAppearance.Material3')): continue
        need('style',n.replace('.','_'),os.path.relpath(p,ROOT))

# ---- ViewBinding fields ----
def camel(s):
    parts=s.split('_'); return parts[0]+''.join(x.capitalize() for x in parts[1:])
def binding_name(layout):
    return ''.join(x.capitalize() for x in layout.split('_'))+'Binding'
layout_ids={}
for p in glob.glob(RES+'/layout/*.xml'):
    n=os.path.splitext(os.path.basename(p))[0]
    layout_ids[binding_name(n)]={camel(i) for i in re.findall(r'@\+id/([A-Za-z0-9_]+)',open(p,encoding='utf-8').read())}
for p in java:
    t=open(p,encoding='utf-8').read()
    # chỉ kiểm tra file có đúng 1 loại binding để gán 'binding.' chính xác
    kinds=set(re.findall(r'\b([A-Z][A-Za-z0-9]*Binding)\b',t))-{'ViewBinding'}
    kinds={k for k in kinds if k in layout_ids or k.startswith(('Fragment','Activity','Dialog','Item'))}
    for k in kinds:
        if k not in layout_ids: missing.append(('binding-class',k,os.path.basename(p)))
    if len(kinds)==1:
        k=next(iter(kinds))
        if k in layout_ids:
            for m in re.finditer(r'\b(?:binding|h\.binding|\w+\.binding)\.([a-z][A-Za-z0-9]*)',t):
                f=m.group(1)
                if f in ('getRoot',): continue
                if f not in layout_ids[k]: missing.append(('binding-field',f'{k}.{f}',os.path.basename(p)))
    elif len(kinds)>1:
        print('NOTE multiple bindings in',os.path.basename(p),kinds)
seen=set(); out=[]
for k,n,w in missing:
    if (k,n) in seen: continue
    seen.add((k,n)); out.append((k,n,w))
for k,n,w in sorted(out): print(f'MISSING {k}: {n}   (used in {w})')
print('total missing:',len(out))
