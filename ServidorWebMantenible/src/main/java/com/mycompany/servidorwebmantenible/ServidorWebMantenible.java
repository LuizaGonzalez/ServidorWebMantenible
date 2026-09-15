package com.mycompany.servidorwebmantenible;

import java.io.IOException;
import java.util.logging.Logger;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 *
 * @author luiza.gonzalez-v
 */
public class ServidorWebMantenible {
    
    static Map <String, WebService> webservices = new HashMap();

    public static void get(String route, WebService ws)
    {    
        webservices.put(route,ws);
    }
    public static String invoke(String route)
    {
        WebService routeFind = webservices.get(route);
        if (routeFind == null)
        {
            return null;
        }
        return routeFind.call();
    }
    public static void start()
    {
        String[] args = {};
        try{
            HttpServer.main(args); 
        } catch (IOException ex){
            Logger.getLogger(ServidorWebMantenible.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
