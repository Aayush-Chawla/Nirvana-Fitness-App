#!/usr/bin/env python3
"""
Script to convert Jupyter notebooks from the Dietary-Patterns-DAP repository
to HTML for use in the Nirvana Fitness App.
"""
import os
import sys
import subprocess
import shutil
from pathlib import Path

def check_requirements():
    """Check if required packages are installed"""
    try:
        import nbconvert
        print("nbconvert is installed.")
    except ImportError:
        print("nbconvert is not installed. Installing...")
        subprocess.run([sys.executable, "-m", "pip", "install", "nbconvert"])

def clone_repository(repo_url, target_dir):
    """Clone the GitHub repository"""
    if os.path.exists(target_dir):
        print(f"Directory {target_dir} already exists.")
        response = input("Do you want to delete it and clone again? (y/n): ")
        if response.lower() == 'y':
            shutil.rmtree(target_dir)
        else:
            print("Using existing directory.")
            return
    
    print(f"Cloning repository {repo_url} to {target_dir}...")
    subprocess.run(["git", "clone", repo_url, target_dir])
    print("Repository cloned successfully.")

def convert_notebook_to_html(notebook_path, output_dir):
    """Convert Jupyter notebook to HTML"""
    import nbconvert
    
    print(f"Converting notebook {notebook_path} to HTML...")
    
    # Get the basename without extension
    notebook_name = os.path.splitext(os.path.basename(notebook_path))[0]
    output_path = os.path.join(output_dir, "dashboard.html")
    
    # Convert notebook to HTML
    subprocess.run([
        "jupyter", "nbconvert", 
        "--to", "html", 
        "--output", output_path,
        notebook_path
    ])
    
    print(f"Notebook converted to HTML: {output_path}")
    return output_path

def post_process_html(html_path):
    """Post-process the HTML to make it work better in a WebView"""
    with open(html_path, 'r', encoding='utf-8') as file:
        content = file.read()
    
    # Add viewport meta tag for better mobile display
    viewport_meta = '<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">'
    content = content.replace('<head>', f'<head>\n    {viewport_meta}')
    
    # Add custom styling
    custom_style = """
    <style>
        body {
            font-family: 'Roboto', Arial, sans-serif;
            margin: 0;
            padding: 16px;
            background-color: #f8f9fa;
            color: #212529;
        }
        .container {
            max-width: 100%;
            margin: 0 auto;
        }
        .header {
            text-align: center;
            margin-bottom: 24px;
        }
        h1, h2, h3, h4, h5, h6 {
            color: #4a6572;
        }
        .output_png img {
            max-width: 100%;
            height: auto;
        }
    </style>
    """
    content = content.replace('</head>', f'{custom_style}\n</head>')
    
    # Wrap content in a container
    content = content.replace('<body>', '<body>\n<div class="container">\n<div class="header">\n<h1>Dietary Patterns Dashboard</h1>\n<p>Advanced analysis based on your dietary data</p>\n</div>')
    content = content.replace('</body>', '</div>\n</body>')
    
    with open(html_path, 'w', encoding='utf-8') as file:
        file.write(content)
    
    print(f"Post-processed HTML file: {html_path}")

def main():
    """Main function"""
    # Check requirements
    check_requirements()
    
    # Set paths
    script_dir = os.path.dirname(os.path.abspath(__file__))
    repo_url = "https://github.com/Aayush-Chawla/Dietary-Patterns-DAP.git"
    target_dir = os.path.join(script_dir, "temp_repo")
    
    # Clone repository
    clone_repository(repo_url, target_dir)
    
    # Find the main notebook file (assuming it's in the root directory)
    notebook_files = list(Path(target_dir).glob('*.ipynb'))
    if not notebook_files:
        print("No Jupyter notebooks found in the repository.")
        return
    
    # Use the first notebook found
    main_notebook = str(notebook_files[0])
    print(f"Found notebook: {main_notebook}")
    
    # Convert notebook to HTML
    html_path = convert_notebook_to_html(main_notebook, script_dir)
    
    # Post-process HTML
    post_process_html(html_path)
    
    # Clean up
    if os.path.exists(target_dir):
        shutil.rmtree(target_dir)
        print(f"Removed temporary directory: {target_dir}")
    
    print("\nDashboard update complete!")
    print(f"The dashboard HTML is available at: {html_path}")
    print("You can now run the app to see the updated dashboard.")

if __name__ == "__main__":
    main() 