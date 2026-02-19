import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;

public class StaticFileServer {
  public static void main(String[] args) throws Exception {
    int port = 8000;
    Path root = Paths.get(".").toRealPath();
    HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
    server.createContext("/", exchange -> {
      try {
        URI uri = exchange.getRequestURI();
        String pathStr = uri.getPath();
        if (pathStr.equals("/")) pathStr = "/catalog.html"; // default file
        Path path = root.resolve(pathStr.substring(1)).normalize();
        if (!path.startsWith(root) || Files.isDirectory(path) || !Files.exists(path)) {
          byte[] notFound = "404 Not Found".getBytes();
          exchange.sendResponseHeaders(404, notFound.length);
          exchange.getResponseBody().write(notFound);
          exchange.close();
          return;
        }
        byte[] bytes = Files.readAllBytes(path);
        String type = Files.probeContentType(path);
        if (type != null) exchange.getResponseHeaders().add("Content-Type", type);
        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
      } catch (Exception e) {
        byte[] err = ("500 " + e.getMessage()).getBytes();
        exchange.sendResponseHeaders(500, err.length);
        exchange.getResponseBody().write(err);
        exchange.close();
      }
    });
    server.setExecutor(null);
    server.start();
    System.out.println("Serving " + root + " on http://localhost:" + port);
  }
}
