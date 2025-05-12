# salva il formato pt in cpu
import torch
import os
from pathlib import Path

SAVED_PT_PATH = "/kaggle/input/modelsintorchscriptformat"
SCRIPTED_DIR = "/kaggle/working/scriptToCPU"
finalDevice = "cpu"

Path(SCRIPTED_DIR).mkdir(parents=True, exist_ok=True)

device = "cuda" if torch.cuda.is_available() else "cpu"
print(f"Using device for loading: {device}")
print(f"Final device for saving: {finalDevice}")

for model_file in os.listdir(SAVED_PT_PATH):
    if not model_file.endswith(".pt"):
        continue

    model_path = os.path.join(SAVED_PT_PATH, model_file)

    try:
        # Load as TorchScript model if already scripted
        model = torch.jit.load(model_path, map_location=finalDevice)
        model = model.to(finalDevice)
        model.eval()

        # Clean name (remove existing '_scripted' if present)
        base_name = os.path.splitext(model_file)[0].replace("_scripted", "")
        scripted_path = os.path.join(SCRIPTED_DIR, f"{base_name}.pt")

        # Save again on finalDevice (CPU)
        model.save(scripted_path)
        print(f"Modello '{model_file}' salvato in TorchScript a: {scripted_path}")

    except Exception as e:
        print(f"Errore con il modello '{model_file}': {e}")
