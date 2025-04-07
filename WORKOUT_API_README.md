# Workout API Implementation

## Overview

This document explains how to implement a workout API for the Nirvana Fitness App. The app has been updated to fetch workout categories and information from a remote API instead of hardcoding the data in the app.

## API Requirements

To implement your own workout API, you'll need to create a backend service that returns data in the following format:

### Workout Categories API

#### Endpoint: `/workouts/categories`

Response format:
```json
{
  "success": true,
  "message": "Categories fetched successfully",
  "categories": [
    {
      "name": "Free Weights",
      "image_url": "https://example.com/images/free-weights.jpg",
      "description": "Dumbbells, barbells, and weight plates"
    },
    {
      "name": "Machines",
      "image_url": "https://example.com/images/machines.jpg",
      "description": "Weight machines and cable equipment"
    },
    // Additional categories...
  ]
}
```

### Workouts by Category API

#### Endpoint: `/workouts`

Query parameters:
- `category`: The workout category name
- `experience`: The experience level (Beginner, Intermediate, Advanced)

Response format:
```json
{
  "success": true,
  "message": "Workouts fetched successfully",
  "categories": [
    {
      "name": "Chest Day",
      "image_url": "https://example.com/images/chest-workout.jpg",
      "description": "Complete chest workout for beginners"
    },
    {
      "name": "Full Upper Body",
      "image_url": "https://example.com/images/upper-body.jpg",
      "description": "Focus on all upper body muscle groups"
    },
    // Additional workouts...
  ]
}
```

## Integration Steps

1. Update the API URL: Open `app/src/main/java/com/example/nirvana/network/ApiClient.java` and replace `WORKOUT_API_URL` with your actual API endpoint:

```java
private static final String WORKOUT_API_URL = "https://your-api-domain.com/api/";
```

2. API Authentication: If your API requires authentication, update the OkHttpClient configuration in the `getWorkoutClient()` method to include your authentication headers.

3. Testing: Run the app and verify that workout categories are loading properly. If API calls fail, the app will fall back to the hardcoded sample data.

## Example API Implementation

You can implement this API using:
- Node.js with Express
- Python with Flask
- Firebase Cloud Functions
- Or any other backend technology of your choice

The app will handle both success and error states from the API, displaying fallback data if the API is unavailable.

## Customizing the Models

If your API returns data in a different format, you may need to update the following files:
- `app/src/main/java/com/example/nirvana/data/models/WorkoutCategoryResponse.java`
- `app/src/main/java/com/example/nirvana/data/models/WorkoutCategory.java`
- `app/src/main/java/com/example/nirvana/network/WorkoutApiService.java` 