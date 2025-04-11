import os
import urllib.request

# Create directories if they don't exist
os.makedirs("app/src/main/assets", exist_ok=True)

# Download the model
model_url = "https://tfhub.dev/tensorflow/lite-model/mobilenet_v2_1.0_224/1/metadata/1?lite-format=tflite"
model_path = "app/src/main/assets/food_recognition_model.tflite"

print(f"Downloading model from {model_url}...")

# Add headers to mimic a browser request
headers = {
    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3'
}

req = urllib.request.Request(model_url, headers=headers)
with urllib.request.urlopen(req) as response:
    with open(model_path, 'wb') as f:
        f.write(response.read())

print(f"Model downloaded to {model_path}")
print("Download complete!") 