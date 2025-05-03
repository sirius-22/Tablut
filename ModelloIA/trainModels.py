import torch
import torch.nn as nn
import torch.optim as optim
from torch.utils.data import DataLoader
from data_loader_with_symmetries import TablutDataset
import pickle
from models import SimpleCNN, DeeperCNN, GlobalPoolingCNN

# Parametri
BATCH_SIZE = 32
NUM_EPOCHS = 5
LEARNING_RATE = 1e-3
MODEL_NAME = "global"  # Cambia a "simple", "deep", "global" per provare modelli diversi

# Caricamento dati
with open("states_dataset.pkl", "rb") as f:
    data = pickle.load(f)
dataset = TablutDataset(data)
dataloader = DataLoader(dataset, batch_size=BATCH_SIZE, shuffle=True)

# Selezione modello
if MODEL_NAME == "simple":
    model = SimpleCNN()
elif MODEL_NAME == "deep":
    model = DeeperCNN()
elif MODEL_NAME == "global":
    model = GlobalPoolingCNN()
else:
    raise ValueError("MODEL_NAME deve essere 'simple', 'deep' o 'global'.")

criterion = nn.MSELoss()
optimizer = optim.Adam(model.parameters(), lr=LEARNING_RATE)

# Training
print(f"Inizio training con modello: {MODEL_NAME}")
for epoch in range(NUM_EPOCHS):
    running_loss = 0.0
    for inputs, targets in dataloader:
        optimizer.zero_grad()
        outputs = model(inputs)
        loss = criterion(outputs, targets)
        loss.backward()
        optimizer.step()
        running_loss += loss.item()
    print(f"Epoch {epoch+1}/{NUM_EPOCHS}, Loss: {running_loss/len(dataloader):.4f}")

# Salvataggio
torch.save(model.state_dict(), f"{MODEL_NAME}_cnn_model.pth")
print(f"Training completato e modello '{MODEL_NAME}' salvato.")