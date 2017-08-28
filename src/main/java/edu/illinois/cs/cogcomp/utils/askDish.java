/**
 * 
 */
package edu.illinois.cs.cogcomp.utils;

// askDish.java - simple DB connector to dishYago server
// CVS $Id: askDish.java 4627 2009-01-16 00:03:56Z paulmcq $

// This is a simple client for the dishYago database server.
// It is designed to serve as an example of how to access
// the dishYago server from your Java application.
// See http://erasmus.redlands.edu/yago for exact query format.

// Copyright (c) 2009 Paul McQuesten
//   No rights reserved: this code is placed into the public domain.
import java.io.*;
import java.net.*;

public class askDish
{
  private BufferedWriter toServer;
  private BufferedReader fromServer;
  private Socket sock;
  private int serial = 0;
  public  boolean echo = true;       // false; for release
  public  String host;
  public  int port;

  public static void main (String[] args) throws Exception {
    String host = "erasmus.redlands.edu";
    // host = "galton";
    if (args.length == 1) {
       host = args[0];
    }
    BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
    askDish yago= new askDish( host, 4761);
    try {
      while (true) {
        String query= stdin.readLine();
        if (query.length() <= 1) break;           // if empty line, quit
        yago.ask( query);
      }
      yago.close();
    }catch (IOException oops) {
      System.err.println( oops.toString());
    }
  }

  // ctor establishes a connection to the server
  public askDish (String host, int port) throws IOException {
    this.host = host;
    this.port = port;
    sock = new Socket(host, port);
    fromServer = new BufferedReader( new InputStreamReader( sock.getInputStream()));
    toServer   = new BufferedWriter( new OutputStreamWriter( sock.getOutputStream()));
    System.out.println("++Connected to " + host + ":" +port);
  }

  // close should try for a clean shutdown
  public void close () throws IOException {
    sock.close();
  }

  // ask sends tuples to the server, and knows when to wait for results
  public void ask (String query) throws IOException {
    if (query.length() <= 1 || query.charAt(0) == '#')
       return;
    // ensure that query is terminated with a newline
    if (query.charAt( query.length()-1) != '\n') {
       query += '\n';
    }
    serial += 1;
    if (echo) {
       System.err.printf("%04d %s", serial, query);
    }
    toServer.write( query);            // send or write???
    toServer.flush();
    if (query.charAt(0) == ':') {
       return;                         // no response expected
    }
    if (query.charAt( query.length()-2) == ';') {
      // multi-line query finished--now await response
      while (true) {
        // reply does not contain a newline
        String reply = fromServer.readLine();
        System.out.println( reply);
        if (reply.charAt(0) == '#' || reply.charAt(0) == '-') break;
      }
    }
  }
}
