# usato gpu t4 x2 di kaggle

import torch
import torch.nn as nn
import torch.optim as optim
from torch.utils.data import DataLoader, random_split
from torch.utils.data import Dataset
import pickle
from pathlib import Path
#from data_loader_with_symmetries import TablutDataset
#from models import SimpleCNN, DeeperCNN, GlobalPoolingCNN

# Parametri
torch.manual_seed(42)

BATCH_SIZE = 32
NUM_EPOCHS = 5
LEARNING_RATE = 1e-3
MODEL_NAME = "deep"  # Cambia a "simple", "deep", "global" per provare modelli diversi
finalDevice = "cpu"

#percorso salvataggio dati
OUTPUT_DATA_PATH = "/kaggle/working/processed_data/states_dataset.pkl"
OUTPUT_MODELS_PATH = "/kaggle/working/models"

# Create a directory if it is not there, so we can save files and results in it
Path(f"{OUTPUT_MODELS_PATH}").mkdir(parents=True, exist_ok=True)

# Setup device-agnostic code 
if torch.cuda.is_available():
    device = "cuda" # NVIDIA GPU
elif torch.backends.mps.is_available():
    device = "mps" # Apple GPU
else:
    device = "cpu" # Defaults to CPU if NVIDIA GPU/Apple GPU aren't available

print(f"Using device: {device}")

# Caricamento dati
with open(f"{OUTPUT_DATA_PATH}", "rb") as f:
    data = pickle.load(f)

full_dataset = TablutDataset(data)

# Suddivisione in train/test set (80/20)
train_size = int(0.8 * len(full_dataset))
test_size = len(full_dataset) - train_size
train_dataset, test_dataset = random_split(full_dataset, [train_size, test_size])

print(f"Train set: {len(train_dataset)}")
print(f"Test set: {len(test_dataset)}")

# DataLoader
train_loader = DataLoader(train_dataset, batch_size=BATCH_SIZE, shuffle=True)
test_loader = DataLoader(test_dataset, batch_size=BATCH_SIZE)

# Selezione modello
if MODEL_NAME == "simple":
    model = SimpleCNN()
elif MODEL_NAME == "deep":
    model = DeeperCNN()
elif MODEL_NAME == "global":
    model = GlobalPoolingCNN()
else:
    raise ValueError("MODEL_NAME deve essere 'simple', 'deep' o 'global'.")


# Put model on the available device
# With this, an error will happen (the model is not on target device)
model = model.to(device)

#funzione di ottimizzazione e loss
criterion = nn.MSELoss()
optimizer = optim.Adam(model.parameters(), lr=LEARNING_RATE)

# Training
print(f"Inizio training con modello: {MODEL_NAME}")
for epoch in range(NUM_EPOCHS):
    model.train()
    running_loss = 0.0

    for inputs, targets in train_loader:
        inputs, targets = inputs.to(device), targets.to(device)

        optimizer.zero_grad()
        outputs = model(inputs)
        loss = criterion(outputs, targets)
        loss.backward()
        optimizer.step()
        running_loss += loss.item()

    avg_loss = running_loss / len(train_loader)
    print(f"Epoch {epoch+1}/{NUM_EPOCHS}, Loss: {avg_loss:.4f}")

# Valutazione finale
model.eval()
with torch.no_grad():
    total_loss = 0.0
    for xb, yb in test_loader:
        xb, yb = xb.to(device), yb.to(device)
        preds = model(xb)
        loss = criterion(preds, yb)
        total_loss += loss.item()
    avg_test_loss = total_loss / len(test_loader)

print(f"Test Loss: {avg_test_loss:.4f}")

# Salvataggio modello
MODEL_SAVE_PATH = f"{OUTPUT_MODELS_PATH}/{MODEL_NAME}_cnn_model.pth"
torch.save(model.state_dict(), MODEL_SAVE_PATH)
print(f"Modello '{MODEL_NAME}' salvato in: {MODEL_SAVE_PATH}")

# salvataggio in formato torchscript
SCRIPTED_PATH = f"{OUTPUT_MODELS_PATH}/{MODEL_NAME}_epoch{NUM_EPOCHS}_cnn_scripted.pt"
scripted_model = torch.jit.script(model)
scripted_model = scripted_model.to(finalDevice)
scripted_model.save(SCRIPTED_PATH)
print(f"Modello salvato in TorchScript a: {SCRIPTED_PATH}")
