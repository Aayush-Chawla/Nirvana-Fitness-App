# API Keys Security

This project uses Google Maps API and other sensitive credentials. To keep these secure:

## Setting Up

1. Copy the template file `gradle.properties.template` to `gradle.properties`
2. Replace `YOUR_API_KEY` with your actual Google Maps API key in `gradle.properties`
3. Never commit `gradle.properties` to Git (it's in `.gitignore`)

## How API Keys Are Handled

- API keys are stored in `gradle.properties` which is not committed to Git
- The build script reads these keys and makes them available to the app
- The AndroidManifest.xml references keys via manifest placeholders
- This approach keeps sensitive data out of source control

## For New Team Members

When you clone this repository:

1. Ask an existing team member for the API keys
2. Copy `gradle.properties.template` to `gradle.properties` 
3. Replace placeholder values with actual API keys
4. Build the project

## Recovering from Accidental Commits

If you accidentally commit sensitive information:

1. Change your API keys immediately
2. Remove the sensitive data from Git history using:
   ```
   git filter-branch --force --index-filter \
   "git rm --cached --ignore-unmatch path/to/file" \
   --prune-empty --tag-name-filter cat -- --all
   ```
3. Force push to overwrite the remote history:
   ```
   git push origin --force --all
   ```

Remember: API keys are like passwords. Keep them secure! 