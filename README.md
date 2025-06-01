
<h1 align="center">✨SAME_Tablut✨</h1>

⚫⚪⚫ *Tablut project for [Foundations Of Artificial Intelligence T](https://www.unibo.it/it/studiare/insegnamenti-competenze-trasversali-moocs/insegnamenti/insegnamento/2024/468002)*
📍 *A.Y. 2024/2025 - University of Bologna* ⚪⚫⚪

---

## ♟️ Overview

This project is an AI client for the **Tablut Challenge (Hashton variant)**, developed by **Team SAME**.

We implemented a Java-based client that plays Tablut using:

* 🤖 **Alpha-Beta Pruning** with **Iterative Deepening**
* 🧠 Two heuristics:

  * A **traditional Java-based evaluation function**
  * A **Neural Network-based evaluation function**, trained on historical match data
* 📚 Powered by the [AIMA Java library](https://github.com/aimacode/aima-java)
* 🔗 The trained neural model is embedded into the Java project using the **Deep Java Library (DJL)** framework

---

## 🧠 Neural Network Details

We trained a neural network to assess game states using a dataset of past Tablut matches:

* Each match outcome was labeled:

  * `+1` for a **white** win
  * `-1` for a **black** win
* Previous states in a match were labeled with **attenuated values**, using a **gamma decay function** to reflect their distance from the final outcome

The model was trained using **PyTorch** and later integrated into the Java client using **DJL (Deep Java Library)** for inference at runtime.

---

## 🗂️ Repository Structure

```
Tablut/
├── Java/         # Java source code (client, heuristics, utilities)
├── Pytorch/      # Training scripts, datasets, and neural model
├── jars/         # Executables (server and client launchers)
```

---

## ⚙️ How to Play

1. Move to the `jars/` directory:

   ```bash
   cd jars
   ```

2. Start the Tablut server:

   ```bash
   java -jar Server.jar
   ```

3. Run the player (black or white) with default settings:

   ```bash
   ./runmyplayer black 60 localhost
   ./runmyplayer white 60 localhost
   ```

4. Optional: Use the neural network as heuristic:

   ```bash
   ./runmyplayer white 55 localhost -NN
   ./runmyplayer black 60 localhost -NN
   ```

---

### 🧾 Player Command Structure

```bash
./runmyplayer <role> <timeout-in-seconds> <server-ip> <-NN>
```

* `<role>`: `black` or `white` (mandatory)
* `<timeout-in-seconds>`: time to compute moves (default: 60)
* `<server-ip>`: IP of the server (default: `localhost`)
* `-NN` (optional): use the neural network heuristic

Example:

```bash
./runmyplayer.sh white 55 localhost -NN
./runmyplayer.sh black 60 localhost
```

> ℹ️ If `-NN` is omitted, the client will use the default Java heuristic.

---

## 📚 Resources

* 🏁 [Tablut Competition](https://github.com/AGalassi/TablutCompetition)
* 📊 [Past seasons match dataset](http://ai.unibo.it/games/boardgamecompetition/tablut)
* 🧪 [Pytorch Fundamentals](https://www.learnpytorch.io/)
* 🧠 [Deep Java Library (DJL)](https://github.com/deepjavalibrary)
* 📈 [Kaggle](https://www.kaggle.com/)
* ♻️ [Transposition Tables (for optimization)](https://mediocrechess.blogspot.com/2007/01/guide-transposition-tables.html)

---

## 👥 Team SAME

A team of students passionate about AI and games, blending classic algorithms with modern ML.

> Feel free to fork, learn, contribute, or challenge our agent! 🔥
