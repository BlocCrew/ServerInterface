package com.norway240.serverinterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DecimalFormat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ServerInterfaceServer {

	private ServerSocket server;
    private int PORT;
	
	public ServerInterfaceServer(int p){
		PORT = p;
	}
	
	public void receive(){
		new Thread(new Runnable() {
	        public void run(){
	        	try {
	                server = new ServerSocket(PORT);
	                System.out.println("[ServerInterface] Server active on port: " + PORT);
	                while (true) {
	                    new ThreadSocket(server.accept());
	                }
	            }catch(Exception e){
	            	System.out.println("[ServerInterface] " + e.getStackTrace());
	            }
	       }
	    }).start();
	}
	
	public void close(){
		try {
			server.close();
		} catch (IOException e) {
        	System.out.println("[ServerInterface] " + e.getStackTrace());
		}
	}
	
	public void interpretData(String post){
    	System.out.println("[ServerInterface] " + post);
	}
	
	public String getOnlinePlayers(){
		String players = "<ul>";
		DecimalFormat df = new DecimalFormat("#.00");
		for(Player p : Bukkit.getServer().getOnlinePlayers()){
			String n = p.getName();
			players += "<li><img src=\"https://minotar.net/avatar/"+n+"\" height=\"64px\">"+n+": Ⓑ"+df.format(ServerInterface.econ.getBalance(p))+"</li>";
		}
		players += "</ul>";
		return players;
	}
}

class ThreadSocket extends Thread {
    private Socket insocket;
    ThreadSocket(Socket insocket) {
        this.insocket = insocket;
        this.start();
    }
    @Override
    public void run() {
        try {
            InputStream is = insocket.getInputStream();
            PrintWriter out = new PrintWriter(insocket.getOutputStream());
            BufferedReader in = new BufferedReader(new InputStreamReader(is));
            String line;
            line = in.readLine();
            //String request_method = line;
            line = "";
            // looks for post data
            int postDataI = -1;
            while ((line = in.readLine()) != null && (line.length() != 0)) {
                if (line.indexOf("Content-Length:") > -1) {
                    postDataI = new Integer(
                            line.substring(
                                    line.indexOf("Content-Length:") + 16,
                                    line.length())).intValue();
                }
            }
            String postData = "";
            // read the post data
            if (postDataI > 0) {
                char[] charArray = new char[postDataI];
                in.read(charArray, 0, postDataI);
                postData = new String(charArray);
            }
            out.println("HTTP/1.0 200 OK");
            out.println("Content-Type: text/html; charset=utf-8");
            out.println("Server: MINISERVER");
            // this blank line signals the end of the headers
            out.println("");
            
            // Send the HTML page
            out.println("<html><head>"
            		+ "<title>Server Interface</title>"
            		+ "<style type=\"text/css\">"
            		+ "body{margin:0;}"
            		+ "iframe{float:right;width:50%;height:100%;border:0;}"
            		+ "#left{float:left;width:50%;height:100%;}"
            		+ "</style>"
            		+ "</head><body>");
            
            out.println("<div id=\"left\""
            		+ "<h1>Players Online:</h1>"
            		+ ServerInterface.server.getOnlinePlayers()
            		+ "</div>");
            
            out.println("<iframe src=\""+ServerInterface.dynmap+"\" />");

            out.println("</body></html>");
            out.close();
            insocket.close();
            ServerInterface.server.interpretData(postData);
        } catch (IOException e) {
        	System.out.println("[ServerInterface] " + e.getStackTrace());
        }
    }
}