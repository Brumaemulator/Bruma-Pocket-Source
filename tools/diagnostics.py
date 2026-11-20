# SPDX-License-Identifier: GPL-3.0-or-later
# Original noncommercial diagnostic generators; not bundled in the product APK.
from pathlib import Path
import zipfile
root=Path(__file__).resolve().parents[1];a=root/'app/src/androidTest/assets/fixtures';a.mkdir(parents=True,exist_ok=True)
for system in ('gb','gbc'):
 b=bytearray(32768);b[0x100:0x104]=bytes([0,0xc3,0x50,0x01]);b[0x104:0x108]=bytes([0xce,0xed,0x66,0x66]);title=('POCKET QA '+system.upper()).encode();b[0x134:0x134+len(title)]=title;b[0x143]=0x80 if system=='gbc' else 0;b[0x147]=3;b[0x149]=2
 code=bytes.fromhex('f3 af e0 40 21 00 80 06 10 3e aa 22 05 20 fc 3e e4 e0 47 3e 0a ea 00 00 3e 5a ea 00 a0 3e 80 e0 26 3e 77 e0 24 3e 11 e0 25 3e 80 e0 11 3e f3 e0 12 3e 00 e0 13 3e 87 e0 14 3e 80 e0 68 3e ff e0 69 3e 7f e0 69 3e e0 e0 69 3e 03 e0 69 3e e0 e0 69 3e 01 e0 69 af e0 69 e0 69 3e 91 e0 40 18 fe');b[0x150:0x150+len(code)]=code
 x=0
 for v in b[0x134:0x14d]:x=(x-v-1)&255
 b[0x14d]=x;(a/f'Diagnostic.{system}').write_bytes(b)
if (a/'Diagnostic.gba').exists():
 with zipfile.ZipFile(a/'Collection.zip','w',zipfile.ZIP_DEFLATED) as z:
  for ext in ('gb','gbc','gba'):z.write(a/f'Diagnostic.{ext}',f'nested/Diagnostic.{ext}')

# Mixed-system scanner regressions; no commercial content.
other=a/"OtherSystem"; (other/"Data").mkdir(parents=True,exist_ok=True); (other/"Game.ini").write_text("[Game]\nLibrary=RGSS102E.dll\n"); (other/"Data"/"DoNotIndex.gba").write_bytes(b"not a cartridge")
(a/"Broken.gba").write_bytes(b"bad")
with zipfile.ZipFile(a/"Unsafe.zip","w",zipfile.ZIP_DEFLATED) as z:z.writestr("../unsafe.gba",b"bad")
