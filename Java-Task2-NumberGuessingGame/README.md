# 🎯 Number Guessing Game

A Java Swing based Number Guessing Game developed as part of the **OASIS Infobyte Java Development Internship – Task 2**.

## 📌 Objective

Build a Java GUI-based game where the computer generates a random number and the user attempts to guess it, receiving higher/lower hints until the correct number is found.

## ✨ Features

- 🎲 Random number generation
- ⬆️ Too High hint
- ⬇️ Too Low hint
- ✅ Correct result
- 🔢 Visible attempt counter
- 🚫 Maximum attempt limit
- ❌ You Lost message when the attempt limit is reached
- 🔍 Correct number revealed after losing
- 🔄 Play Again option
- 🏆 Score tracking across multiple rounds
- 🟢 Win tracking
- 🔴 Loss tracking
- 📊 Round history and summary
- 🎚️ Easy, Medium and Hard difficulty levels
- 🔊 Sound effects
- 🖥️ Modern Java Swing GUI
- ⌨️ Input validation

## 🎮 Difficulty Levels

| Difficulty | Number Range | Maximum Attempts |
|------------|--------------|------------------:|
| Easy | 1–50 | 10 |
| Medium | 1–100 | 7 |
| Hard | 1–200 | 5 |

## 🏆 Scoring System

The game awards points based on the number of attempts used. More points are awarded when the player guesses the correct number using fewer attempts.

The game tracks:

- Score
- Wins
- Losses
- Round History

## 🎯 How the Game Works

1. Select a difficulty level.
2. The computer generates a random number within the selected range.
3. Enter your guess.
4. The game provides a **Too High**, **Too Low**, or **Correct** hint.
5. The attempt counter is updated after every valid guess.
6. If the maximum attempts are reached, the player loses and the correct number is revealed.
7. Click **Play Again** to start another round.
8. Score, wins, losses and round history are maintained across rounds.

## 🛠️ Technologies Used

- Java
- Java Swing
- Java AWT
- java.util.Random
- Java Sound API

## 🎥 Project Demonstration

Watch the complete demonstration of the Number Guessing Game on LinkedIn:

▶️ **[Watch Demo Video on LinkedIn](https://lnkd.in/p/dsW_e4An)**

## 📂 Project Structure

```text
Java-Task2-NumberGuessingGame/
│
├── src/
│   └── NumberGuessingGame.java
│
└── README.md