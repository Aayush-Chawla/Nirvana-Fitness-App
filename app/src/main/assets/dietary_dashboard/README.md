# Dietary Patterns Dashboard Integration

This folder contains files for the Dietary Patterns Dashboard feature, which integrates with the Jupyter notebooks from the [Dietary-Patterns-DAP](https://github.com/Aayush-Chawla/Dietary-Patterns-DAP) repository.

## How to Update the Dashboard

To update the dashboard with the latest analysis from the Jupyter notebooks:

1. Clone the Dietary-Patterns-DAP repository:
   ```
   git clone https://github.com/Aayush-Chawla/Dietary-Patterns-DAP.git
   ```

2. Run the Jupyter notebooks to generate the visualizations.

3. Export the notebook as HTML:
   - Open the notebook in Jupyter
   - Go to File > Export Notebook As... > HTML
   - Save the HTML file

4. Extract the relevant visualizations and data from the exported HTML.

5. Update the `dashboard.html` file in this folder with the new content.

## Implementation Details

The dashboard is displayed in a WebView within the DietaryDashboardFragment. The HTML content includes:

- Data visualizations from the Jupyter notebook
- Key insights from the dietary patterns analysis
- Personalized recommendations based on the user's data

## Custom CSS Styling

The dashboard uses custom CSS to match the app's design language and ensure a consistent user experience across the application.

## Troubleshooting

If the dashboard doesn't display correctly:

1. Check that the HTML file is properly formatted
2. Ensure all referenced assets (images, CSS, etc.) are included in the assets folder
3. Verify that JavaScript is enabled in the WebView configuration 