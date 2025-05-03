import os
import torch
from torch.utils.data import Dataset

# Codifica della board in 4 canali: [Nero, Bianco, Re, Vuoto]
def board_to_tensor(board_lines):
    piece_map = {'B': 0, 'W': 1, 'K': 2, 'O': 3, 'T': 3}  # T = trono trattato come vuoto
    tensor = torch.zeros((4, 9, 9), dtype=torch.float32)

    for i, row in enumerate(board_lines):
        for j, cell in enumerate(row):
            if cell in piece_map:
                tensor[piece_map[cell], i, j] = 1.0
    return tensor

def augment_symmetries(tensor):
    augmented = []
    for k in range(4):  # rotazioni 0°, 90°, 180°, 270°
        rot = torch.rot90(tensor, k, dims=(1, 2))
        augmented.append(rot)
        augmented.append(torch.flip(rot, dims=[1]))  # riflessione orizzontale
    return augmented

# Parsing di una singola partita da file strutturato
def parse_structured_match_file(filepath):
    boards = []
    current_board = []
    reading_board = False
    result = None

    with open(filepath, 'r') as f:
        lines = f.read().splitlines()

    for i, line in enumerate(lines):
        if 'FINE: Stato:' in line:
            reading_board = True
            current_board = []

        elif reading_board:
            if line.strip() == '-':
                continue  # separatore inutile
            elif line.strip() in {'W', 'B'}:
                boards.append(current_board)
                reading_board = False
            elif len(line.strip()) == 9:
                current_board.append(line.strip())

        elif 'FINE: Bianco vince' in line or 'WW' in line:
            result = 'W'
        elif 'FINE: Nero vince' in line or 'BW' in line:
            result = 'B'
        elif 'D' in line:
            result = 'D'

    # Converti in tensori
    if result not in {'W', 'B'}:
        return []  # partita incompleta o pareggio, scartiamo
    label = 1.0 if result == 'W' else -1.0

     #tensors = [board_to_tensor(b) for b in boards]
    #targets = [torch.tensor(label, dtype=torch.float32) for _ in tensors]

    #Modifica
    data = []
    for b in boards:
        tensor = board_to_tensor(b)
        augmented_versions = augment_symmetries(tensor)
        data.extend([(aug_tensor, torch.tensor(label, dtype=torch.float32)) for aug_tensor in augmented_versions])

    return data

# Caricamento di tutte le partite strutturate da una cartella
def load_all_structured_matches(folder_path):
    dataset = []
    for filename in os.listdir(folder_path):
        if filename.endswith(".txt"):
            full_path = os.path.join(folder_path, filename)
            dataset.extend(parse_structured_match_file(full_path))
    return dataset

# Dataset PyTorch personalizzato
class TablutDataset(Dataset):
    def __init__(self, data):
        self.data = data

    def __len__(self):
        return len(self.data)

    def __getitem__(self, idx):
        board_tensor, value = self.data[idx]
        return board_tensor, value
