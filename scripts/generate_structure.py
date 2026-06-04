import os
import re
import datetime

IGNORE_DIRS = {'.git', '.gradle', '.idea', '.kotlin', 'build', 'node_modules', 'bin', 'obj', 'gradle', '.agents', 'new_chat_docs'}
IGNORE_FILES = {'.gitignore', 'gradlew', 'gradlew.bat', 'local.properties', 'gradle.properties'}

def find_project_root():
    """
    Dynamically finds the project root directory by searching for marker files.
    """
    current = os.path.abspath(os.path.dirname(__file__))
    while True:
        if os.path.exists(os.path.join(current, '.git')) or os.path.exists(os.path.join(current, 'settings.gradle.kts')):
            return current
        parent = os.path.dirname(current)
        if parent == current:
            return os.getcwd()
        current = parent

def get_dir_tree(startpath, indent=""):
    """
    Recursively builds a text representation of the directory tree.
    """
    lines = []
    try:
        names = sorted(os.listdir(startpath))
    except PermissionError:
        return []

    for name in names:
        if name in IGNORE_DIRS or name in IGNORE_FILES:
            continue
        path = os.path.join(startpath, name)
        if os.path.isdir(path):
            lines.append(f"{indent}├── {name}/")
            lines.extend(get_dir_tree(path, indent + "│   "))
        else:
            lines.append(f"{indent}├── {name}")
    return lines

def extract_kotlin_functions(file_path):
    """
    Extracts function definitions from a Kotlin/Java file, omitting comments.
    """
    functions = []
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
    except Exception:
        return []

    # Strip block comments /* ... */
    content_no_comments = re.sub(r'/\*.*?\*/', '', content, flags=re.DOTALL)
    # Strip line comments // ...
    content_no_comments = re.sub(r'//.*', '', content_no_comments)

    # Simple regex to find Kotlin function names
    pattern = r'\bfun\s+([a-zA-Z0-9_]+)\s*\('
    matches = re.findall(pattern, content_no_comments)
    for match in matches:
        if match not in functions:
            functions.append(match)
    return functions

def generate_structure_markdown(root_dir, output_file):
    tree_lines = [f". (root: {os.path.basename(os.path.abspath(root_dir))})"]
    tree_lines.extend(get_dir_tree(root_dir))

    function_map = {}
    for root, dirs, files in os.walk(root_dir):
        # In-place filtering to avoid traversing ignored directories
        dirs[:] = [d for d in dirs if d not in IGNORE_DIRS]
        for file in files:
            if file in IGNORE_FILES:
                continue
            if file.endswith('.kt') or file.endswith('.java'):
                full_path = os.path.join(root, file)
                rel_path = os.path.relpath(full_path, root_dir)
                funcs = extract_kotlin_functions(full_path)
                if funcs:
                    function_map[rel_path] = funcs

    # Generate Markdown
    md_content = []
    md_content.append("# 🏛️ Project Structure & API Map\n")
    md_content.append(f"*Generated automatically on: {datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')}*\n")
    md_content.append("## 1. Directory & File Tree\n")
    md_content.append("```text")
    md_content.extend(tree_lines)
    md_content.append("```\n")

    md_content.append("## 2. Source Files & Function Lists\n")
    for file_path, funcs in sorted(function_map.items()):
        md_content.append(f"### 📄 `{file_path.replace(os.sep, '/')}`")
        for func in funcs:
            md_content.append(f"- `fun {func}()`")
        md_content.append("")

    # Write file
    os.makedirs(os.path.dirname(output_file), exist_ok=True)
    with open(output_file, 'w', encoding='utf-8') as f:
        f.write("\n".join(md_content))

if __name__ == '__main__':
    project_root = find_project_root()
    output_path = os.path.join(project_root, 'docs', 'STRUCTURE.md')
    generate_structure_markdown(project_root, output_path)
    print(f"Successfully generated structure map at: {output_path}")
