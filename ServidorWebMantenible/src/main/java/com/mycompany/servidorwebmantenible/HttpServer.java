package com.mycompany.servidorwebmantenible;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author luiza.gonzalez-v
 */
public class HttpServer {

    private static final int DEFAULT_PORT = 35000;
    private static final String RESOURCE_ROOT = "/public";

    /** Puerto de entrada del servidor, resuelve el puerto que se va a usar y 
     *  acepta conexiones
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
        int port = resolvePort(args);
        runServer(port);
    }
    
    /** Abre el puerto y mantiene las conexiones de clientes
     * 
     * @param args
     * @return 
     */
    private static void runServer(int port) throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Ready to receive on port " + port + "...");
        
        boolean isReceiveConection = true;
        while(isReceiveConection){
            try (Socket clientSocket = serverSocket.accept()){
                handleRequest(clientSocket);
            }catch (IOException e) {
                System.out.println("Error atendiendo una solicitud: " + e.getMessage());
            }
        }
    }
    
    /**Determina el puerto que usara el servidor, siguiendo la prioridad de
     * argumento en linea de comandos, variable de entorno y por defecto
     * @param args argumentos de línea de comandos recibidos 
     * @return el número de puerto a usar
     */
    private static int resolvePort(String[] args) 
    {
        Integer fromArgs = parsePortArg(args);
        if(fromArgs != null) {
            return fromArgs;
        }
        Integer fromEnv = parsePortEnv();
        if(fromEnv != null) {
            return fromEnv;
        }
        return DEFAULT_PORT;
    }
    
    private static Integer parsePortArg(String[] args){
        if(args.length == 0) {
            return null;
        }
        try {
            return Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.out.println("Argumento de puerto inválido, usando valor por defecto " + DEFAULT_PORT);
            return null;
        }
    }
    
    private static Integer parsePortEnv() {
        String envPort = System.getenv("PORT");
        if (envPort == null) {
            return null;
        }
        try {
            return Integer.parseInt(envPort);
        } catch (NumberFormatException e) {
            System.out.println("Variable de entorno PORT inválida, usando valor por defecto " + DEFAULT_PORT);
            return null;
        }
    }

    private static void handleRequest(Socket clientSocket) throws IOException {
        BufferedReader in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()));
        OutputStream rawOut = new BufferedOutputStream(clientSocket.getOutputStream());

        String requestLine = in.readLine();
        if (requestLine == null || requestLine.isEmpty()) {
            return;
        }
        System.out.println("Request: " + requestLine);

        // Consume el resto de encabezados hasta la línea en blanco (no nos interesa su contenido aquí)
        String headerLine;
        while ((headerLine = in.readLine()) != null && !headerLine.isEmpty()) {
            // ignorado intencionalmente
        }

        String[] parts = requestLine.split(" ");
        if (parts.length < 2) {
            sendError(rawOut, 400, "Bad Request");
            return;
        }
        String method = parts[0];
        String rawUri = parts[1];

        if (!method.equals("GET")) {
            sendError(rawOut, 405, "Method Not Allowed");
            return;
        }

        URI reqURI;
        try {
            reqURI = new URI(rawUri);
        } catch (URISyntaxException e) {
            sendError(rawOut, 400, "Bad Request");
            return;
        }

        String path = reqURI.getPath();
        String query = reqURI.getQuery();
        
        String result = ServidorWebMantenible.invoke(path);
        
        if (result != null)
        {
            sendText(rawOut, result);
        }else
        {
            handleStaticResource(rawOut, path);
        }
    }
    
    // Recursos estáticos

    private static void handleStaticResource(OutputStream out, String path) throws IOException {
        if (path == null || path.equals("/")) {
            path = "/index.html";
        }

        // Normaliza y rechaza cualquier intento de salir del área pública (path traversal)
        String normalized = java.nio.file.Paths.get(path).normalize().toString().replace("\\", "/");
        if (normalized.contains("..") || !normalized.startsWith("/")) {
            sendError(out, 400, "Ruta inválida");
            return;
        }

        String resourcePath = RESOURCE_ROOT + normalized;
        try (InputStream resourceStream = HttpServer.class.getResourceAsStream(resourcePath)) {
            if (resourceStream == null) {
                sendError(out, 404, "No encontrado: " + normalized);
                return;
            }
            byte[] content = resourceStream.readAllBytes();
            String contentType = contentTypeFor(normalized);
            sendBytes(out, 200, "OK", contentType, content);
        }
    }

    static String contentTypeFor(String path) {
        String lower = path.toLowerCase();
        if (lower.endsWith(".html")) return "text/html; charset=UTF-8";
        if (lower.endsWith(".js")) return "application/javascript; charset=UTF-8";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        return "application/octet-stream";
    }

    //Utilidades de respuesta 

    private static void sendText(OutputStream out, String body) throws IOException {
        sendBytes(out, 200, "OK", "text/plain; charset=UTF-8",
                body.getBytes(StandardCharsets.UTF_8));
    }

    private static void sendError(OutputStream out, int code, String message) throws IOException {
        sendBytes(out, code, message, "text/plain; charset=UTF-8",
                message.getBytes(StandardCharsets.UTF_8));
    }

    private static void sendBytes(OutputStream out, int statusCode, String statusText,
                                   String contentType, byte[] body) throws IOException {
        String header = "HTTP/1.1 " + statusCode + " " + statusText + "\r\n"
                + "Content-Type: " + contentType + "\r\n"
                + "Content-Length: " + body.length + "\r\n"
                + "\r\n";
        out.write(header.getBytes(StandardCharsets.UTF_8));
        out.write(body);
        out.flush();
    }

    static Map<String, String> parseQuery(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isEmpty()) {
            return params;
        }
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
            String value = kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "";
            params.put(key, value);
        }
        return params;
    }
}
