package banking.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Bridge service for communicating with the local Ollama LLM API.
 * Sends prompts to http://localhost:11434/api/generate and returns responses.
 * The LLM has NO direct database access — all banking data must be injected into prompts.
 */
public class LLMService {

    private static final String OLLAMA_URL = "http://localhost:11434/api/generate";
    private static final String DEFAULT_MODEL = "phi3";
    private static final int TIMEOUT_MS = 60000; // 60 seconds

    private String model;

    public LLMService() {
        this.model = DEFAULT_MODEL;
    }

    public LLMService(String model) {
        this.model = model;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    /**
     * System prompt that constrains the LLM to a professional fintech advisor role.
     */
    private static final String SYSTEM_PROMPT =
        "You are \"Aegis\", a professional AI Banking Advisor for SecureBank desktop application.\n\n" +
        "Your role: Analyze financial data, explain transactions, and guide users through banking operations.\n\n" +
        "RESPONSE FORMAT - STRICT RULES:\n" +
        "1. NEVER write paragraphs. Use short sentences.\n" +
        "2. ALWAYS use bullet points (•) for lists.\n" +
        "3. Use HEADINGS in UPPERCASE for sections.\n" +
        "4. Highlight KEY NUMBERS with **double asterisks**.\n" +
        "5. Keep responses under 200 words unless user asks for detail.\n" +
        "6. When comparing: use simple tables with | separators.\n\n" +
        "STRUCTURED OUTPUT EXAMPLE:\n" +
        "HEADING\n" +
        "• Point one\n" +
        "• Point two\n" +
        "• **$100** is the key amount\n\n" +
        "CRITICAL RULES:\n" +
        "- You are READ-ONLY. Never execute transactions.\n" +
        "- Do not access databases. Use only provided context.\n" +
        "- If asked to transfer/deposit: explain the process, then direct to Dashboard.\n" +
        "- Never say 'consult an advisor' - you ARE the advisor.\n" +
        "- Calculate EMI, interest, or impact when numbers are provided.\n\n" +
        "TONE: Professional, confident, concise, analytical.";

    /**
     * Sends a prompt to the Ollama API synchronously and returns the full response.
     * This should be called from a background thread (SwingWorker).
     */
    public String generate(String userMessage, String bankingContext) {
        try {
            String fullPrompt = buildPrompt(userMessage, bankingContext);
            String requestBody = buildRequestJson(fullPrompt);

            URL url = URI.create(OLLAMA_URL).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(TIMEOUT_MS);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(requestBody.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }

            int status = conn.getResponseCode();
            if (status != 200) {
                return "[Error] Ollama returned HTTP " + status + ". Is the model '" + model + "' pulled and running?";
            }

            // Ollama streams JSON lines with {"response":"..."} fragments
            StringBuilder fullResponse = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String token = extractResponseToken(line);
                    if (token != null) {
                        fullResponse.append(token);
                    }
                }
            }

            conn.disconnect();
            String result = fullResponse.toString().trim();
            return result.isEmpty() ? "[No response from model]" : result;

        } catch (java.net.ConnectException e) {
            return "[Offline] Cannot connect to Ollama at localhost:11434.\n" +
                   "Please ensure Ollama is running: open a terminal and run 'ollama serve'";
        } catch (java.net.SocketTimeoutException e) {
            return "[Timeout] The model took too long to respond. Try a shorter question.";
        } catch (Exception e) {
            return "[Error] " + e.getClass().getSimpleName() + ": " + e.getMessage();
        }
    }

    /**
     * Sends a prompt asynchronously, calling the callback with the result on completion.
     */
    public CompletableFuture<String> generateAsync(String userMessage, String bankingContext) {
        return CompletableFuture.supplyAsync(() -> generate(userMessage, bankingContext));
    }

    /**
     * Checks if the Ollama server is reachable.
     */
    public boolean isAvailable() {
        try {
            URL url = URI.create("http://localhost:11434/api/tags").toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            int status = conn.getResponseCode();
            conn.disconnect();
            return status == 200;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Builds the full prompt by combining system prompt, banking context, and user message.
     */
    private String buildPrompt(String userMessage, String bankingContext) {
        StringBuilder prompt = new StringBuilder();
        prompt.append(SYSTEM_PROMPT).append("\n\n");

        if (bankingContext != null && !bankingContext.isEmpty()) {
            prompt.append("Banking Context (read-only facts):\n");
            prompt.append(bankingContext);
            prompt.append("\n\n");
        }

        prompt.append("User Request:\n").append(userMessage == null ? "" : userMessage);
        return prompt.toString();
    }

    /**
     * Builds the JSON request body for the Ollama API.
     */
    private String buildRequestJson(String prompt) {
        // Manual JSON construction to avoid external dependencies
        return "{" +
            "\"model\":\"" + escapeJson(model) + "\"," +
            "\"prompt\":\"" + escapeJson(prompt) + "\"," +
            "\"stream\":true," +
            "\"options\":{\"temperature\":0.5,\"num_predict\":1024}" +
            "}";
    }

    /**
     * Extracts the "response" field from a streaming JSON line.
     * Each line looks like: {"model":"phi3","response":"token","done":false}
     */
    private String extractResponseToken(String jsonLine) {
        if (jsonLine == null || jsonLine.isEmpty()) return null;

        // Find "response":"..." in the JSON
        int idx = jsonLine.indexOf("\"response\":\"");
        if (idx == -1) return null;

        int start = idx + 12; // length of "response":"
        StringBuilder token = new StringBuilder();
        boolean escaped = false;
        for (int i = start; i < jsonLine.length(); i++) {
            char c = jsonLine.charAt(i);
            if (escaped) {
                switch (c) {
                    case 'n': token.append('\n'); break;
                    case 't': token.append('\t'); break;
                    case '\\': token.append('\\'); break;
                    case '"': token.append('"'); break;
                    case '/': token.append('/'); break;
                    default: token.append('\\').append(c); break;
                }
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else if (c == '"') {
                break; // end of response string
            } else {
                token.append(c);
            }
        }
        return token.toString();
    }

    /**
     * Escapes a string for safe inclusion in JSON.
     */
    private String escapeJson(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                    break;
            }
        }
        return sb.toString();
    }
}
