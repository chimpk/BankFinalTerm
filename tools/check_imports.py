import re, glob, os
import pathlib
ROOT=str(pathlib.Path(__file__).resolve().parent.parent/'app'/'src'/'main'/'java')
files=glob.glob(ROOT+'/**/*.java',recursive=True)
pkgs={}
for f in files:
    d=os.path.dirname(f); pkgs.setdefault(d,set()).add(os.path.splitext(os.path.basename(f))[0])
JAVA_LANG={'String','Object','Integer','Long','Double','Float','Boolean','Math','System','Exception','Throwable','Runnable','Override','Deprecated','SuppressWarnings','IllegalStateException','IllegalArgumentException','NumberFormatException','AssertionError','StringBuilder','Character','Byte','Short','Number','Thread','Class','Enum','Iterable','Comparable','CharSequence','Void','Error','RuntimeException','InterruptedException','SecurityException','UnsupportedOperationException','NullPointerException','FunctionalInterface','SafeVarargs','Process'}
BUILTIN_ANDROID_R={'R','BuildConfig'}
bad=0
for f in files:
    src=open(f,encoding='utf-8').read()
    code=re.sub(r'//.*','',src); code=re.sub(r'/\*.*?\*/','',code,flags=re.S); code=re.sub(r'"(?:\\.|[^"\\])*"','""',code)
    imports=set(m.group(1).split('.')[-1] for m in re.finditer(r'^import\s+(?:static\s+)?([\w.]+);',code,flags=re.M))
    declared=set(re.findall(r'\b(?:class|interface|enum)\s+([A-Z]\w*)',code))
    typevars=set(re.findall(r'<\s*([A-Z])\b',code))|set(re.findall(r'\b([A-Z])\s*[,>]',code))
    samepkg=pkgs[os.path.dirname(f)]
    used=set(re.findall(r'(?<![\w.@])([A-Z][A-Za-z0-9_]*)\b(?=\s*[.<(\[\s]|\s*\w)',code))
    used|=set(re.findall(r'@([A-Z]\w*)',code))
    # bỏ hằng số IN_HOA (static final), tên enum-constant trong switch
    cands=[u for u in used if not u.isupper() and u not in imports and u not in declared and u not in samepkg and u not in JAVA_LANG and u not in typevars and u not in BUILTIN_ANDROID_R]
    # các tên đầy đủ package được viết trực tiếp (vn.edu...X) bị regex bỏ nhờ lookbehind '.'
    # Loại tên là hằng enum trong case, hoặc thành viên nested của lớp đã import (vd Status)
    nested=set(re.findall(r'\b(?:%s)\.([A-Z]\w+)'%'|'.join(re.escape(i) for i in imports|declared|samepkg) if (imports|declared|samepkg) else 'ZZZ',code))
    cands=[c for c in cands if c not in nested]
    if cands:
        bad+=1; print(os.path.relpath(f,ROOT),'->',sorted(cands))
print('files with suspicious names:',bad)
import sys
sys.exit(1 if bad else 0)
