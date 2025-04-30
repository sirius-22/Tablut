
import os
import torch
from torch.utils.data import Dataset

# Codifica la board in 4 canali: [Nero, Bianco, Re, Vuoto]
def board_to_tensor(board_lines):
    piece_map = {'B': 0, 'W': 1, 'K': 2, 'O': 3, 'T': 3} #trono trattato come vuoto
    tensor = torch.zeros((4, 9, 9), dtype=torch.float32)
    for i, row in enumerate(board_lines):
        for j, cell in enumerate(row):
            if cell in piece_map:
                tensor[piece_map[cell], i, j] = 1.0
    return tensor

# Genera 8 versioni simmetriche di un tensore board (4, 9, 9)
def augment_symmetries(tensor):
    augmented = []
    for k in range(4):  # rotazioni 0°, 90°, 180°, 270°
        rot = torch.rot90(tensor, k, dims=(1, 2))
        augmented.append(rot)
        augmented.append(torch.flip(rot, dims=[1]))  # riflessione orizzontale
    return augmented

# Parsing di una singola partita
def parse_match_file(filepath):
    boards = []
    current_board = []
    reading_board = False
    winner = None

    with open(filepath, 'r') as f:
        lines = f.read().splitlines()

    for i, line in enumerate(lines):
        if "FINE: Stato:" in line:
            reading_board = True
            current_board = []
        elif reading_board:
            if line.strip() == '-':
                continue
            elif line.strip() in {'W', 'B'}:
                if len(current_board) == 9:
                    boards.append(current_board)
                reading_board = False
            elif len(line.strip()) == 9:
                current_board.append(line.strip())
        elif 'WW' in line:
            winner = 'W'
        elif 'BW' in line:
            winner = 'B'
        elif 'D' in line:
            winner = 'D'

    if winner == 'W':
        label = 1.0
    elif winner == 'B':
        label = -1.0
    elif winner == 'D':
        label = 0.0
    else:
        return []

    all_examples = []
    for board_lines in boards:
        base_tensor = board_to_tensor(board_lines)
        symmetries = augment_symmetries(base_tensor)
        all_examples.extend([(aug, torch.tensor(label, dtype=torch.float32)) for aug in symmetries])

    return all_examples

# Caricamento dataset da una cartella
def load_dataset_from_folder(folder_path):
    dataset = []
    for filename in os.listdir(folder_path):
        if filename.endswith(".txt"):
            full_path = os.path.join(folder_path, filename)
            examples = parse_match_file(full_path)
            dataset.extend(examples)
    return dataset

# Dataset PyTorch
class TablutStateDataset(Dataset):
    def __init__(self, examples):
        self.examples = examples

    def __len__(self):
        return len(self.examples)

    def __getitem__(self, idx):
        return self.examples[idx]
