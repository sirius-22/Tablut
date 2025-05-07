
#from data_loader_with_symmetries import load_dataset_from_folder, TablutDataset
from torch.utils.data import DataLoader
import pickle
from pathlib import Path

# Percorso alla cartella contenente i file delle partite
DATASET_PATH = "/kaggle/input/predataset/PreDataset/"  # Modifica questo percorso se necessario
#percorso salvataggio dati 
OUTPUTDIR_PATH = "/kaggle/working/processed_data/"
OUTPUT_PATH = OUTPUTDIR_PATH + "states_dataset.pkl"

# Create a directory if it is not there, so we can save files and results in it
Path(f"{OUTPUTDIR_PATH}").mkdir(parents=True, exist_ok=True)

# Caricamento dei dati da tutti i file
print("Caricamento dei dati...")
data = load_dataset_from_folder(DATASET_PATH)
print(f"Totale stati caricati: {len(data)}")

# salvataggio 
print(f"Salvataggio del dataset pre-elaborato in: {OUTPUT_PATH}")
with open(OUTPUT_PATH, "wb") as f:
    pickle.dump(data, f)
    
# Creazione del dataset personalizzato (di controllo)
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
