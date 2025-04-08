# Dietary Dashboard Integration Guide

This guide explains how to integrate the Jupyter notebook from the [Dietary-Patterns-DAP](https://github.com/Aayush-Chawla/Dietary-Patterns-DAP) repository into the Nirvana Fitness App.

## Overview

The integration process involves:
1. Cloning the GitHub repository
2. Converting the Jupyter notebook to HTML
3. Customizing the HTML for mobile display
4. Adding the HTML to the app's assets

## Prerequisites

- Python 3.6 or higher
- Jupyter and nbconvert packages
- Git

## Steps for Integration

### 1. Automated Integration

The easiest way to integrate the dashboard is to use the provided Python script:

```bash
cd /path/to/Nirvana-Fitness-App/app/src/main/assets/dietary_dashboard
python update_dashboard.py
```

This script will:
- Clone the repository
- Convert the notebook to HTML
- Apply styling for mobile viewing
- Place the HTML file in the correct location

### 2. Manual Integration

If you prefer to integrate manually:

#### a. Clone the repository
```bash
git clone https://github.com/Aayush-Chawla/Dietary-Patterns-DAP.git
cd Dietary-Patterns-DAP
```

#### b. Find the main Jupyter notebook
Look for `.ipynb` files in the repository. The main analysis notebook should be identified.

#### c. Convert to HTML
```bash
jupyter nbconvert --to html /path/to/notebook.ipynb --output dashboard.html
```

#### d. Customize the HTML
Edit the HTML file to:
- Add responsive meta tags
- Apply mobile-friendly styling
- Ensure visualizations are properly sized

#### e. Add to assets
Copy the HTML file to:
```
app/src/main/assets/dietary_dashboard/dashboard.html
```

### 3. Additional Resources

For advanced customization:
- Copy any required images or resources from the notebook output to the assets folder
- If there are interactive elements, ensure they work properly in the WebView

## Troubleshooting

### Common Issues:

1. **Visualizations don't render properly**
   - Solution: Ensure the HTML file includes all necessary JavaScript libraries
   - Alternative: Convert visualizations to static images

2. **Performance issues**
   - Solution: Reduce the size of the HTML file by removing unnecessary elements
   - Alternative: Load visualizations asynchronously

3. **Missing dependencies**
   - Solution: Ensure all required JavaScript libraries are included in the HTML file
   - Alternative: Simplify visualizations to avoid external dependencies

### Getting Help

If you encounter issues with the integration:
1. Check the [Dietary-Patterns-DAP repository](https://github.com/Aayush-Chawla/Dietary-Patterns-DAP) for documentation
2. Refer to the Jupyter nbconvert documentation
3. Contact the developer team for assistance 