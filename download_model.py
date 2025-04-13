import kagglehub
import os
import shutil

def download_model():
    # Download the model
    print("Downloading model from Kaggle Hub...")
    model_path = kagglehub.model_download("google/aiy/tfLite/vision-classifier-food-v1")
    print(f"Model downloaded to: {model_path}")
    
    # Create assets directory if it doesn't exist
    assets_dir = "app/src/main/assets"
    os.makedirs(assets_dir, exist_ok=True)
    
    # Copy model files to assets directory
    for file in os.listdir(model_path):
        if file.endswith('.tflite') or file.endswith('.txt'):
            src = os.path.join(model_path, file)
            dst = os.path.join(assets_dir, file)
            shutil.copy2(src, dst)
            print(f"Copied {file} to {dst}")
    
    print("Model files copied to assets directory successfully!")

if __name__ == "__main__":
    download_model() 