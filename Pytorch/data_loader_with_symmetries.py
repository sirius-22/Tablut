#data_loader_with_symmetries
import os
import torch
from torch.utils.data import Dataset

# Codifica board in 4 canali
def board_to_tensor(board_lines):
    piece_map = {'B': 0, 'W': 1, 'K': 2, 'O': 3, 'T': 3}
    tensor = torch.zeros((4, 9, 9), dtype=torch.float32)

    for i, row in enumerate(board_lines):
        for j, cell in enumerate(row):
            if cell in piece_map:
                tensor[piece_map[cell], i, j] = 1.0
    return tensor

# Augmenting con rotazioni e riflessioni
def augment_symmetries(tensor):
    augmented = []
    for k in range(4):
        rot = torch.rot90(tensor, k, dims=(1, 2))
        augmented.append(rot)
        augmented.append(torch.flip(rot, dims=[1]))
    return augmented

# Propagazione del reward a ritroso (decadimento gamma)
def generate_discounted_labels(num_moves, winner, gamma=0.99):
    reward = 1.0 if winner == 'W' else -1.0
    return [reward * (gamma ** (num_moves - 1 - i)) for i in range(num_moves)]

# Parsing di una singola partita
def parse_structured_match_file(filepath, augment=True, discount=True, gamma=0.99):
    all_boards = []
    current_board = []
    reading_board = False
    result = None

    with open(filepath, 'r') as f:
        lines = f.read().splitlines()

    for line in lines:
        if 'FINE: Stato:' in line:
            reading_board = True
            current_board = []
        elif reading_board:
            if line.strip() == '-':
                continue
            elif line.strip() in {'W', 'B'}:
                all_boards.append(current_board)
                reading_board = False
            elif len(line.strip()) == 9:
                current_board.append(line.strip())
        elif 'FINE: Bianco vince' in line or 'WW' in line:
            result = 'W'
        elif 'FINE: Nero vince' in line or 'BW' in line:
            result = 'B'
        elif 'D' in line:
            result = 'D'

    if result not in {'W', 'B'}:
        return []

    # Etichette con propagazione o fissa
    if discount:
        labels = generate_discounted_labels(len(all_boards), result, gamma)
    else:
        value = 1.0 if result == 'W' else -1.0
        labels = [value for _ in all_boards]

    data = []
    for board, value in zip(all_boards, labels):
        tensor = board_to_tensor(board)
        if augment:
            augmented = augment_symmetries(tensor)
            data.extend((aug, torch.tensor(value, dtype=torch.float32)) for aug in augmented)
        else:
            data.append((tensor, torch.tensor(value, dtype=torch.float32)))

    return data

# Caricamento dataset intero da cartella
def load_dataset_from_folder(folder_path, augment=True, discount=True, gamma=0.99):
    dataset = []
    for filename in os.listdir(folder_path):
        if filename.endswith(".txt"):
            full_path = os.path.join(folder_path, filename)
            dataset.extend(parse_structured_match_file(full_path, augment, discount, gamma))
    return dataset

# Dataset PyTorch
class TablutDataset(Dataset):
    def __init__(self, data):
        self.data = data

    def __len__(self):
        return len(self.data)

    def __getitem__(self, idx):
        board_tensor, value = self.data[idx]
        return board_tensor, value
