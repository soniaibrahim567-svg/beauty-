import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MakeupStore {

    // Product Model
    static class Product {
        int id;
        String name, brand, image, description, category;
        double price;

        Product(int id, String name, String brand, double price, String image, String description, String category) {
            this.id = id;
            this.name = name;
            this.brand = brand;
            this.price = price;
            this.image = image;
            this.description = description;
            this.category = category;
        }
    }

    // Database
    static List<Product> products = new ArrayList<>();
    static Map<Integer, Integer> cart = new HashMap<>(); // productId -> quantity

    static {
        products.add(new Product(1, "Velvet Matte Lipstick", "Maybelline", 12.99, "https://via.placeholder.com/200x200/ff69b4/ffffff?text=Lipstick", "Long-lasting matte finish", "Lips"));
        products.add(new Product(2, "Glow Foundation", "Fenty Beauty", 35.00, "https://via.placeholder.com/200x200/ffb6c1/ffffff?text=Foundation", "Lightweight coverage", "Face"));
        products.add(new Product(3, "Palette Eyeshadow", "Urban Decay", 45.50, "https://via.placeholder.com/200x200/da70d6/ffffff?text=Eyeshadow", "12 pigmented shades", "Eyes"));
        products.add(new Product(4, "Hydrating Primer", "Milk Makeup", 28.00, "https://via.placeholder.com/200x200/ff1493/ffffff?text=Primer", "Grapefruit extract", "Face"));
        products.add(new Product(5, "Volumizing Mascara", "L'Oreal", 9.99, "https://via.placeholder.com/200x200/c71585/ffffff?text=Mascara", "Dramatic volume", "Eyes"));
        products.add(new Product(6, "Setting Spray", "NYX", 15.00, "https://via.placeholder.com/200x200/db7093/ffffff?text=Spray", "All-day hold", "Face"));
    }

    public static void main(String[] args) throws IOException {
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new RequestHandler());
        server.setExecutor(null);
        server.start();
        
        System.out.println("=====================================");
        System.out.println("💄 Makeup Store Started!");
        System.out.println("🌐 Open: http://localhost:" + port);
        System.out.println("=====================================");
    }

    static class RequestHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            String query = exchange.getRequestURI().getQuery();
            String response = "";

            if (path.equals("/")) {
                response = handleHome(query);
            } else if (path.equals("/add")) {
                handleAddToCart(query);
                response = "redirect";
            } else if (path.equals("/cart")) {
                response = handleCart();
            } else if (path.equals("/remove")) {
                handleRemoveFromCart(query);
                response = "redirect";
            } else {
                response = "<h1>404 - Not Found</h1>";
            }

            if (response.equals("redirect")) {
                exchange.getResponseHeaders().set("Location", "/");
                exchange.sendResponseHeaders(302, -1);
            } else {
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, bytes.length);
                OutputStream os = exchange.getResponseBody();
                os.write(bytes);
                os.close();
            }
        }

        String handleHome(String query) {
            String search = "";
            String category = "";
            List<Product> filtered = new ArrayList<>(products);

            if (query != null) {
                if (query.contains("search=")) {
                    search = URLDecoder.decode(query.split("search=")[1].split("&")[0], StandardCharsets.UTF_8).toLowerCase();
                    filtered.removeIf(p -> !p.name.toLowerCase().contains(search) && !p.brand.toLowerCase().contains(search));
                }
                if (query.contains("category=")) {
                    category = URLDecoder.decode(query.split("category=")[1].split("&")[0], StandardCharsets.UTF_8);
                    if (!category.isEmpty()) {
                        filtered.removeIf(p -> !p.category.equals(category));
                    }
                }
            }

            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html><html><head><title>GlowUp Makeup</title>");
            html.append("<style>");
            html.append("body{font-family:Arial,sans-serif;background:#fff0f5;margin:0;padding:20px}");
            html.append("header{text-align:center;color:#d63384;margin-bottom:20px}");
            html.append(".nav{display:flex;justify-content:space-between;align-items:center;margin-bottom:20px}");
            html.append(".cart-btn{background:#d63384;color:white;padding:10px 20px;border-radius:20px;text-decoration:none}");
            html.append(".filters{display:flex;gap:10px;margin-bottom:20px;flex-wrap:wrap}");
            html.append(".filters a{padding:8px 15px;background:white;border:2px solid #ff69b4;border-radius:15px;text-decoration:none;color:#d63384}");
            html.append(".filters a.active{background:#ff69b4;color:white}");
            html.append(".grid{display:flex;flex-wrap:wrap;gap:20px;justify-content:center}");
            html.append(".card{background:white;border-radius:15px;box-shadow:0 4px 8px rgba(0,0,0,0.1);width:250px;overflow:hidden}");
            html.append(".card img{width:100%;height:200px;object-fit:cover}");
            html.append(".card-body{padding:15px;text-align:center}");
            html.append(".price{color:#d63384;font-size:1.3em;font-weight:bold;margin:10px 0}");
            html.append(".btn{background:#ff69b4;color:white;padding:10px 20px;border:none;border-radius:5px;cursor:pointer;text-decoration:none;display:inline-block}");
            html.append(".search{padding:10px;width:300px;border:2px solid #ff69b4;border-radius:20px;outline:none}");
            html.append("</style></head><body>");

            html.append("<header><h1>💄 GlowUp Makeup Store</h1></header>");

            // Navigation
            html.append("<div class='nav'>");
            html.append("<form action='/' method='get' style='flex:1'>");
            html.append("<input type='text' name='search' class='search' placeholder='Search products...' value='").append(search).append("'>");
            html.append("</form>");
            html.append("<a href='/cart' class='cart-btn'>🛒 Cart (").append(getCartCount()).append(")</a>");
            html.append("</div>");

            // Category Filters
            html.append("<div class='filters'>");
            html.append("<a href='/' class='").append(category.isEmpty() ? "active" : "").append("'>All</a>");
            html.append("<a href='/?category=Lips' class='").append(category.equals("Lips") ? "active" : "").append("'>💋 Lips</a>");
            html.append("<a href='/?category=Face' class='").append(category.equals("Face") ? "active" : "").append("'>✨ Face</a>");
            html.append("<a href='/?category=Eyes' class='").append(category.equals("Eyes") ? "active" : "").append("'>👁️ Eyes</a>");
            html.append("</div>");

            // Products
            html.append("<div class='grid'>");
            if (filtered.isEmpty()) {
                html.append("<h2 style='width:100%;text-align:center'>No products found</h2>");
            } else {
                for (Product p : filtered) {
                    html.append("<div class='card'>");
                    html.append("<img src='").append(p.image).append("'>");
                    html.append("<div class='card-body'>");
                    html.append("<h3>").append(p.name).append("</h3>");
                    html.append("<p style='color:gray'>").append(p.brand).append("</p>");
                    html.append("<p>").append(p.description).append("</p>");
                    html.append("<p class='price'>$").append(String.format("%.2f", p.price)).append("</p>");
                    html.append("<a href='/add?id=").append(p.id).append("' class='btn'>Add to Cart 🛒</a>");
                    html.append("</div></div>");
                }
            }
            html.append("</div></body></html>");

            return html.toString();
        }

        void handleAddToCart(String query) {
            if (query != null && query.startsWith("id=")) {
                int id = Integer.parseInt(query.substring(3));
                cart.put(id, cart.getOrDefault(id, 0) + 1);
            }
        }

        void handleRemoveFromCart(String query) {
            if (query != null && query.startsWith("id=")) {
                int id = Integer.parseInt(query.substring(3));
                if (cart.containsKey(id)) {
                    int qty = cart.get(id);
                    if (qty <= 1) {
                        cart.remove(id);
                    } else {
                        cart.put(id, qty - 1);
                    }
                }
            }
        }

        String handleCart() {
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html><html><head><title>Shopping Cart</title>");
            html.append("<style>");
            html.append("body{font-family:Arial,sans-serif;background:#fff0f5;padding:20px}");
            html.append("h1{color:#d63384;text-align:center}");
            html.append(".cart-item{background:white;padding:15px;margin:10px 0;border-radius:10px;display:flex;justify-content:space-between;align-items:center}");
            html.append(".btn{background:#ff69b4;color:white;padding:8px 15px;border:none;border-radius:5px;cursor:pointer;text-decoration:none}");
            html.append(".total{font-size:1.5em;color:#d63384;font-weight:bold;text-align:center;margin:20px 0}");
            html.append("</style></head><body>");
            html.append("<h1>🛒 Shopping Cart</h1>");
            html.append("<a href='/' style='color:#d63384'>← Back to Shop</a>");

            if (cart.isEmpty()) {
                html.append("<h2 style='text-align:center;color:gray'>Your cart is empty</h2>");
            } else {
                double total = 0;
                for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
                    Product p = products.stream().filter(prod -> prod.id == entry.getKey()).findFirst().orElse(null);
                    if (p != null) {
                        double subtotal = p.price * entry.getValue();
                        total += subtotal;
                        html.append("<div class='cart-item'>");
                        html.append("<div>");
                        html.append("<h3>").append(p.name).append("</h3>");
                        html.append("<p>").append(p.brand).append(" - $").append(String.format("%.2f", p.price)).append(" x ").append(entry.getValue()).append("</p>");
                        html.append("</div>");
                        html.append("<div>");
                        html.append("<strong>$").append(String.format("%.2f", subtotal)).append("</strong>");
                        html.append(" <a href='/remove?id=").append(p.id).append("' class='btn'>Remove</a>");
                        html.append("</div>");
                        html.append("</div>");
                    }
                }
                html.append("<div class='total'>Total: $").append(String.format("%.2f", total)).append("</div>");
                html.append("<div style='text-align:center'><a href='/' class='btn' style='background:#d63384;padding:15px 30px;font-size:1.1em'>Checkout 💳</a></div>");
            }

            html.append("</body></html>");
            return html.toString();
        }

        int getCartCount() {
            return cart.values().stream().mapToInt(Integer::intValue).sum();
        }
    }
}