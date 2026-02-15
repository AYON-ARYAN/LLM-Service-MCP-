# RAGMCP: Retrieval-Augmented Generation with Spring Boot & Groq

A powerful Spring Boot application that implements Retrieval-Augmented Generation (RAG) using the Groq API (Llama 3) for high-speed inference. This project demonstrates how to build an agentic RAG system with document ingestion, vector search, and intelligent query routing.

## 🚀 Features

- **Document Ingestion**: Upload PDF and text documents via API.
- **Intelligent Chunking**: Automatically splits documents into manageable chunks with overlap for better context preservation.
- **Vector Search**: Implements cosine similarity search to find the most relevant document sections.
- **Agentic Reasoning**: Uses an internal agent to classify user queries into categories:
  - **SUMMARY**: Generates a full document summary.
  - **RAG**: answers specific questions using retrieved context.
  - **GENERAL**: Handles conversational queries without document context.
- **Groq Integration**: Leverages the blazing fast Groq API (Llama 3 70b) for inference.
- **In-Memory Vector Store**: Simple and efficient in-memory storage for embeddings (with PostgreSQL pgvector support available via Docker).

## 🛠️ Tech Stack

- **Language**: Java 17
- **Framework**: Spring Boot 3.4.2
- **AI/LLM**: Groq API (Llama 3.3 70b Versatile)
- **Document Parsing**: Apache Tika
- **Build Tool**: Maven
- **Containerization**: Docker & Docker Compose (for PostgreSQL/pgvector)

## 📋 Prerequisites

- Java 17 or higher
- Maven
- A [Groq API Key](https://console.groq.com/)

## ⚙️ Configuration

The application is configured to use environment variables for sensitive information. You need to set the following variables before running:

| Variable       | Description                                         | Default (if applicable) |
| :------------- | :-------------------------------------------------- | :---------------------- |
| `GROQ_API_KEY` | **Required**. Your Groq API key.                    | -                       |
| `DB_PASSWORD`  | **Required**. Password for the PostgreSQL database. | -                       |
| `DB_USERNAME`  | Username for the database.                          | `ayon`                  |

### Environment Setup

You can export these in your terminal:

```bash
export GROQ_API_KEY="gsk_..."
export DB_PASSWORD="secret_password"
```

## 🏃‍♂️ How to Run

1.  **Clone the repository**:

    ```bash
    git clone https://github.com/yourusername/ragmcp.git
    cd ragmcp
    ```

2.  **Build the project**:

    ```bash
    mvn clean install
    ```

3.  **Run the application**:
    ```bash
    mvn spring-boot:run
    ```
    The server will start on `http://localhost:8084`.

## 🔌 API Endpoints

### 1. Upload Document

Uploads a file (PDF or Text) to be processed and indexed.

- **URL**: `POST /docs/upload`
- **Body**: `multipart/form-data`
  - `file`: The file to upload.

**Example (cURL)**:

```bash
curl -F "file=@/path/to/document.pdf" http://localhost:8084/docs/upload
```

### 2. Ask Question

Ask a question about the uploaded documents. The agent will decide whether to summarize, search, or chat casually.

- **URL**: `POST /ask`
- **Body**: Raw text (the question).

**Example (cURL)**:

```bash
curl -X POST -H "Content-Type: text/plain" -d "What is the main conclusion of the document?" http://localhost:8084/ask
```

### 3. List Documents

Returns a list of all uploaded document names.

- **URL**: `GET /docs`

## 🏗️ Architecture

1.  **Upload**: Files are parsed by Apache Tika and split into chunks (1000 chars, 200 overlap).
2.  **Embedding**: Each chunk is converted into a vector embedding (currently simulated/simplified or using an external service depending on `EmbeddingService` implementation).
3.  **Storage**: Chunks and embeddings are stored in an in-memory `VectorDBService`.
4.  **Querying**:
    - The `AgentService` analyzes the intent of the incoming question.
    - If **RAG** is selected, `ChunkService` performs a cosine similarity search against stored embeddings.
    - Top 5 relevant chunks are retrieved and fed into `ReasoningService` to construct a prompt.
    - `GroqService` sends the final prompt to the LLM for the answer.
