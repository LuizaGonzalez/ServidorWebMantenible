package com.mycompany.servidorwebmantenible;

import com.mycompany.servidorwebmantenible.ServidorWebMantenible;
import static com.mycompany.servidorwebmantenible.ServidorWebMantenible.*;

/**
 *
 * @author luiza.gonzalez-v
 */

public class Application {

    public static void main(String[] args) throws Exception {

        /**staticfiles("/webroot");

        get("/hello", (req, resp) -> {
            String name = req.getValue("name");

            if (name == null || name.isBlank()) {
                name = "world";
            }

            return "Hello " + name;
        });**/

        get("/pi", () -> String.valueOf(Math.PI));
        get("/e", () -> String.valueOf(Math.E));
        get("/hello", () -> {
            String message = "Hello world";
            return message;
        });
        System.out.println(ServidorWebMantenible.invoke("/pi"));
        System.out.println(ServidorWebMantenible.invoke("/e"));
        System.out.println(ServidorWebMantenible.invoke("/"));
        start();
    }
}
