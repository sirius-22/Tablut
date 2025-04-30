import torch
import torch.nn as nn
import torch.optim as optim
from torch.utils.data import DataLoader
from data_loader import TablutDataset  # Assicura che il file data_loader.py sia nello stesso percorso
import pickle

# Parametri
BATCH_SIZE = 32
NUM_EPOCHS = 5
LEARNING_RATE = 1e-3

# Caricamento dati
with open("states_dataset.pkl", "rb") as f:
    data = pickle.load(f)
dataset = TablutDataset(data)
dataloader = DataLoader(dataset, batch_size=BATCH_SIZE, shuffle=True)

# Definizione della rete neurale
class CNNRegressor(nn.Module):
    def __init__(self):
        super(CNNRegressor, self).__init__()
        self.net = nn.Sequential(
            nn.Conv2d(4, 32, kernel_size=3, padding=1),
            nn.ReLU(),
            nn.Conv2d(32, 64, kernel_size=3, padding=1),
            nn.ReLU(),
            nn.AdaptiveAvgPool2d((1, 1)),
            nn.Flatten(),
            nn.Linear(64, 1),
            nn.Tanh()  # Per mappare l'output in [-1, 1]
        )

    def forward(self, x):
        return self.net(x).squeeze(1)

# Istanziamento modello e setup ottimizzazione
model = CNNRegressor()
criterion = nn.MSELoss()
optimizer = optim.Adam(model.parameters(), lr=LEARNING_RATE)

# Training loop
print("Inizio training...")
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

# Salvataggio del modello
torch.save(model.state_dict(), "cnn_regressor.pth")
print("Training completato e modello salvato.")