# SPDX-License-Identifier: GPL-3.0-or-later
"""Package exact rebuildable Android sources; omit games, keys, caches and unused artwork."""
from pathlib import Path
import zipfile,hashlib,json,sys,re
root=Path(__file__).resolve().parents[1]
build=(root/'app/build.gradle.kts').read_text(encoding='utf-8')
version=re.search(r'versionName\s*=\s*"([^"]+)"',build).group(1)
code=int(re.search(r'versionCode\s*=\s*(\d+)',build).group(1))
target=Path(sys.argv[1]) if len(sys.argv)>1 else root/f'releases/{version}/Bruma-Pocket-{version}-Corresponding-Source.zip' 
target.parent.mkdir(parents=True,exist_ok=True)
root_files={'AGENTS.md','README.md','LICENSE','NOTICE','.gitignore','build.gradle.kts','settings.gradle.kts','gradle.properties','gradlew','gradlew.bat'}
root_dirs={'app','bruma-core','bruma-library','bruma-input','bruma-covers','bruma-saves','gradle','docs','tools'}
skip_dirs={'build','.cxx','.gradle','.kotlin','.git','__pycache__','node_modules'}
# Android LIBMGBA_ONLY does not compile upstream desktop/test artwork or binaries.
skip_extensions={'.cgfx','.pk','.mvl','.bin','.rom','.sav','.state','.svg','.png','.jpg','.jpeg','.gif','.bmp','.ico','.icns','.qpic','.t3x','.ttf','.otf','.wav','.mp3','.ogg','.mp4','.raw','.chm','.pdf','.elf','.so','.o','.a','.apk','.aab','.zip','.gba','.gb','.gbc','.nds','.bios','.jks','.keystore','.p12','.pem'}
files=[]
for p in sorted(root.rglob('*')):
 if not p.is_file():continue
 rel=p.relative_to(root)
 if rel.parts[:5]==('app','src','androidTest','assets','fixtures'):continue
 if 'cinema' in rel.parts:continue
 if rel.parts[0] not in root_dirs and rel.as_posix() not in root_files:continue
 if any(x in skip_dirs for x in rel.parts):continue
 if p.suffix.lower() in skip_extensions:continue
 if p.suffix.lower()=='.jar' and rel.as_posix()!='gradle/wrapper/gradle-wrapper.jar':continue
 if p.name=='local.properties':continue
 files.append((p,rel))
manifest={'format':1,'package':'com.brumastudio.pocket','versionName':version,'versionCode':code,'signing':'No keys included; rebuild with your own debug/release signing key' ,'excluded':'Generated diagnostic ROM binaries, unused upstream desktop/test artwork, caches, SDKs, private keys and local paths','files':[{'path':rel.as_posix(),'sha256':hashlib.sha256(p.read_bytes()).hexdigest(),'bytes':p.stat().st_size} for p,rel in files]}
with zipfile.ZipFile(target,'w',zipfile.ZIP_DEFLATED,compresslevel=9) as z:
 for p,rel in files:z.write(p,'BrumaPocket/'+rel.as_posix())
 z.writestr('BrumaPocket/SOURCE-MANIFEST.json',json.dumps(manifest,indent=2))
with zipfile.ZipFile(target) as z:
 assert z.testzip() is None
 for entry in manifest['files']:assert hashlib.sha256(z.read('BrumaPocket/'+entry['path'])).hexdigest()==entry['sha256']
print(json.dumps({'archive':str(target),'sourceFiles':len(files),'bytes':target.stat().st_size,'sha256':hashlib.sha256(target.read_bytes()).hexdigest()}))
