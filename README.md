# 🤖 ViG — Personal AI Companion & Autonomous Agent for Android

> **"One command. ViG understands, plans, executes, verifies and reports."**

ViG is an autonomous personal AI companion built for Android using Kotlin, Jetpack Compose, MVVM, Clean Architecture, and Coroutines. It features a complete native **Voice Subsystem**, **Multi-Model Intelligence** (Google Gemini, OpenAI GPT-4o, Anthropic Claude 3.5 Sonnet), **Chain-of-Thought System Prompting**, and a **Beige Claymorphism & Glowing Dark Teal** UI design system.

---

## 🎨 UI/UX Visual Identity
- **Beige & Glowing Dark Teal Design System**: Built with warm beige backgrounds (`#E8DCC8`), light cream clay surfaces (`#F4EBDD`), dark teal accent cards (`#0D3834`), and electric teal lighting (`#00D2B4`).
- **Tactile 3D Claymorphism**: Soft diffuse dual-shadows and light highlights giving components a 3D molded feel.
- **Clean Voice Assistant Interface**: Real-time conversational bubbles, animated ViG AI core, and audible speech playback.

---

## 🧠 Multi-Model AI Engine & Reasoning
- 🔵 **Google Gemini 1.5 Flash / 2.0 Flash / 1.5 Pro**: Built-in default integration with support for fast, low-latency reasoning.
- 🟢 **OpenAI GPT-4o**: Pluggable provider for multi-step tasks.
- 🟣 **Anthropic Claude 3.5 Sonnet**: Advanced document understanding and deep analysis.
- ⚡ **Chain-of-Thought System Prompting**: Instructs models to perform step-by-step reasoning before outputting structured answers.

---

## 🎙️ Native Voice Subsystem
- **Speech-To-Text (STT)**: Native Android `SpeechRecognizer` integration with real-time transcription.
- **Text-To-Speech (TTS)**: Android `TextToSpeech` engine for audible task execution reports.
- **Push-To-Talk (PTT) & Tap-To-Talk**: Physical tactile microphone button with real-time state animations.

---

## 🛠️ Tech Stack & Architecture
- **Language**: Kotlin 100%
- **UI**: Jetpack Compose + Custom Material 3 Clay Modifiers
- **Architecture**: MVVM + Clean Architecture + StateFlow
- **Async & Concurrency**: Kotlin Coroutines + Mutex State Machine (`AgentOrchestrator`)
- **Network & Security**: Retrofit 2 + OkHttp3 + EncryptedSharedPreferences (`KeyStoreManager`)

---

## 🚀 Getting Started

### 1. Build & Install Android APK
```bash
git clone https://github.com/Vignesh-SVK24/Vig-Ai.git
cd Vig-Ai
./gradlew installDebug
```

### 2. Interactive Web Demo
Open the included Web Demo in your browser:
- Open `web_demo/index.html` in your browser.
- Or run `python -m http.server 8080` inside `web_demo/` and open `http://localhost:8080`.

---

## ⚙️ Configuration
1. Open **ViG** ➔ **Settings**.
2. Select your AI Provider (**Google Gemini**, **OpenAI GPT-4o**, or **Claude 3.5**).
3. Paste your API Key:
   - **Gemini**: `AIzaSy...` or `AQ.Ab8...` (from [Google AI Studio](https://aistudio.google.com/))
   - **OpenAI**: `sk-proj-...` (from [OpenAI Platform](https://platform.openai.com/api-keys))
   - **Claude**: `sk-ant-...` (from [Anthropic Console](https://console.anthropic.com/))
4. Tap **Save Config**!
