# train_setup.py (versione aggiornata per log con struttura nuova)

#from data_loader import load_all_structured_matches, TablutDataset
from torch.utils.data import DataLoader

# Percorso alla cartella contenente i file delle partite
DATASET_PATH = "/kaggle/input/predataset/PreDataset/"  # Modifica questo percorso se necessario

# Caricamento dei dati da tutti i file
print("Caricamento dei dati...")
data = load_dataset_from_folder(DATASET_PATH)
print(f"Totale stati caricati: {len(data)}")

# Creazione del dataset personalizzato
dataset = TablutDataset(data)

# Creazione del DataLoader per batch
BATCH_SIZE = 32
dataloader = DataLoader(dataset, batch_size=BATCH_SIZE, shuffle=True)

# Esplorazione di un batch per verifica
print("Visualizzazione di un batch di esempio:")
for xb, yb in dataloader:
    print(f"Shape del batch di input: {xb.shape}")   # Atteso: (batch_size, 4, 9, 9)
    print(f"Target (valutazioni): {yb}")             # Valori: +1.0 o -1.0
    break
