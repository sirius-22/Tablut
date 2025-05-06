public static float[] encodeStateFromGame(char[][] board) {
    float[] input = new float[4 * 9 * 9];

    for (int y = 0; y < 9; y++) {
        for (int x = 0; x < 9; x++) {
            char piece = board[y][x];
            int idx = y * 9 + x;

            switch (piece) {
                case 'B':
                    input[idx] = 1f; // Channel 0
                    break;
                case 'W':
                    input[81 + idx] = 1f; // Channel 1
                    break;
                case 'K':
                    input[162 + idx] = 1f; // Channel 2
                    break;
                case 'O': case 'T':
                    input[243 + idx] = 1f; // Channel 3
                    break;
                default:
                    // niente: lascia 0 (casella vuota implicita)
                    break;
            }
        }
    }

    return input;
}
