import re
import sys
from pathlib import Path
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]

def parse_getters(bean_path):
    text = bean_path.read_text()
    return set(re.findall(r'\bpublic\s+\w+\s+(get\w+)\s*\(', text))

def parse_fields(jrxml_path):
    tree = ET.parse(jrxml_path)
    return {f.get('name') for f in tree.findall('.//{http://jasperreports.sourceforge.net/jasperreports}field')}

def find_bean_and_jrxml(generator_path):
    text = generator_path.read_text()
    m = re.search(r'List<([^>]+)>\s+getReportData', text)
    bean = m.group(1) if m else None
    m = re.search(r'return\s+"([^"]+\.jrxml)"', text)
    jrxml = m.group(1) if m else None
    return bean, jrxml

errors = []
for gen in ROOT.glob('src/main/java/nonprofitbookkeeping/reports/generator/*JasperGenerator.java'):
    bean, jrxml = find_bean_and_jrxml(gen)
    if not bean or not jrxml:
        continue
    bean_path = Path('src/main/java') / Path(bean.replace('.', '/') + '.java')
    bean_file = ROOT / bean_path
    jrxml_file = ROOT / 'src/main/resources' / jrxml
    if not bean_file.exists() or not jrxml_file.exists():
        continue
    getters = parse_getters(bean_file)
    fields = parse_fields(jrxml_file)
    missing = [f for f in fields if not any(g.lower()==f'get{f}'.lower() for g in getters)]
    if missing:
        errors.append((gen.name, missing))

if errors:
    for gen, miss in errors:
        print(f'{gen}: missing getters for {miss}')
    sys.exit(1)
print('All JRXML fields have corresponding getters')
