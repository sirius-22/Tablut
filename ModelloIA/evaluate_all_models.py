# evaluate_all_models.py

import torch
import torch.nn as nn
from torch.utils.data import DataLoader, random_split
import pickle
from pathlib import Path
import matplotlib.pyplot as plt
import os
import time


# Importa i modelli e il dataset
#from models import SimpleCNN, DeeperCNN, GlobalPoolingCNN
#from data_loader_with_symmetries import TablutDataset
#from trainModels import test_loader 
# Percorsi
MODELS_DIR = "/kaggle/working/models"
DATA_PATH = "/kaggle/working/processed_data/states_dataset.pkl"

# Device
device = "cuda" if torch.cuda.is_available() else "cpu"
print(f"Using device: {device}")

# Carica dataset
with open(DATA_PATH, "rb") as f:
    data = pickle.load(f)

# Loss function
criterion = nn.MSELoss()

# Mappa nomi file → classi modello
model_classes = {
    "simple": SimpleCNN,
    "deep": DeeperCNN,
    "global": GlobalPoolingCNN,
}

# Valutazione modelli
results = {}

for model_file in os.listdir(MODELS_DIR):
    if not model_file.endswith(".pth"):
        continue

    model_key = model_file.split("_")[0]  # "simple", "deep", "global"
    model_class = model_classes.get(model_key)

    if model_class is None:
        print(f"Modello non riconosciuto: {model_file}")
        continue

    model = model_class().to(device)
    model.load_state_dict(torch.load(os.path.join(MODELS_DIR, model_file), weights_only=True))

    model.eval()

    total_loss = 0.0
    with torch.no_grad():
        for xb, yb in test_loader:
            xb, yb = xb.to(device), yb.to(device)
            preds = model(xb)
            loss = criterion(preds, yb)
            total_loss += loss.item()

    avg_loss = total_loss / len(test_loader)
    results[model_file] = avg_loss
    print(f"{model_file}: Test Loss = {avg_loss:.4f}")

timestamp = time.strftime("%Y-%m-%d_%H-%M")

# Plot
plt.figure(figsize=(10, 6))
plt.bar(results.keys(), results.values(), color='skyblue')
plt.xticks(rotation=45)
plt.ylabel("MSE Loss")
plt.title("Performance dei modelli sul test set")
plt.tight_layout()
plt.savefig(f"/kaggle/working/model_performance_{timestamp}.png")
plt.show()

# Modello migliore
best_model = min(results, key=results.get)
print(f"\nModello migliore: {best_model} con Loss = {results[best_model]:.4f}")
