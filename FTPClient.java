import java.io.*;
import java.net.*;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//	name:	Max Hudnell
//	pid:	7203 10449
public class FTPClient {
	static int port_num=8080;
	static int file_num=0;
	static boolean connect_lck=true, quit_lck=false;		// CONNECT and QUIT "locks"
	
	public static void main(String args[]) throws Exception, SocketException {
        Socket FTPcontrol = new Socket();
//        Socket FTPdata = new Socket();
        ServerSocket welcomeSocket = new ServerSocket();
        if(args.length > 0) {
        	port_num=Integer.parseInt(args[0]);
        }
        
        final String EOLstrng=System.getProperty("line.separator");
		
		// Create (buffered) input stream using standard input
        Scanner s = new Scanner(System.in).useDelimiter("(?<=\r\n)|(?<=\r)(?=[^\n])|(?<=\n)");	// terminated by CR, LF, or CRLF
		
		// While loop to read and handle multiple input lines
        while (s.hasNextLine()) {			// FOR SCANNER
        	String line = s.next();			// FOR SCANNER
//============================================================================================================================================================================
        	String g1, g2, g3, g21, g22, p_string, port_string, trimmed_input, parse_result;
    		int port_num1, port_num2, server_port;
    		
    		Pattern p = Pattern.compile("(.+?)(?:(\\s+)(.+?)?)?$");
    		Matcher m = p.matcher(line);
    		trimmed_input = line.replaceAll("[\n\r]", "");
    		if (!quit_lck) {
    			System.out.println(trimmed_input);	//print back the command
    			if (m.find()) {
    				g1 = m.group(1);
    		        g2 = m.group(2);
    		        g3 = m.group(3);
    		        if (g1 != null && g1.matches("(?i)connect|get|quit")) {	// valid command name?
    		        	if (g1.matches("(?i)connect")) {			// CONNECT ================================================================================================
    		        		if (g3 == null) {
    		        			if (g2 != null && g2.matches("^[\r\n]+$")) {			// contains no spaces between command and eof
    		        				System.out.println("ERROR -- request");
    		        			} else {
    		        				System.out.println("ERROR -- server-host");
    		        			}
    		        		} else {
    		        			Pattern p2 = Pattern.compile("^(.+?)\\s+(.+?)$");
    							Matcher m2 = p2.matcher(g3);
    							if (m2.find()) {
    								g21 = m2.group(1);	// <server-host>
    								g22 = m2.group(2);	// <server-port>
    								if (g21 != null && g22 != null && g21.matches("^([A-Za-z][A-Za-z0-9]+\\.?)*?[A-Za-z][A-Za-z0-9]+$")) {
    									try {	
    										server_port = Integer.parseInt(g22);
    										if (server_port >= 0 & server_port < 65536) {		// IS VALID COMMAND
    											//===================================================================================================
    											try {
													FTPcontrol.close();
													FTPcontrol = new Socket(g21, server_port);
													//FTPcontrol.setSoTimeout(10000);
													System.out.print("CONNECT accepted for FTP server at host "+g21+" and port "+g22+"\r\n");
    											} catch (ConnectException | UnknownHostException e1) {
    												System.out.print("CONNECT failed\r\n");
    												continue;
    											}
    											
    											try {
    												// Create output stream attached to socket
        											DataOutputStream outToServer = new DataOutputStream(FTPcontrol.getOutputStream());
        											// Create (buffered) input stream attached to socket
        											BufferedReader inFromServer = new BufferedReader(new InputStreamReader(FTPcontrol.getInputStream()));
        											
        											parse_result = parse(inFromServer.readLine()+"\r\n");	// READ INITIAL MESSAGE FROM SERVER
        		    								if (parse_result == null || Character.toString(parse_result.charAt(0)) == "4" || Character.toString(parse_result.charAt(0)) == "5") {
        		    									continue;
        		    								}
        											
        											System.out.print("USER anonymous\r\n");				// ECHO COMMAND TO STD OUTPUT
        											outToServer.writeBytes("USER anonymous\r\n");		// SEND COMMAND TO SERVER
        											parse_result = parse(inFromServer.readLine()+"\r\n");	// PARSE SERVER RESPONSE
        		    								if (parse_result == null || Character.toString(parse_result.charAt(0)) == "4" || Character.toString(parse_result.charAt(0)) == "5") {
        		    									continue;
        		    								}
        											
        											System.out.print("PASS guest@\r\n");				// REPEAT..
        											outToServer.writeBytes("PASS guest@\r\n");
        											parse_result = parse(inFromServer.readLine()+"\r\n");	// PARSE SERVER RESPONSE
        		    								if (parse_result == null || Character.toString(parse_result.charAt(0)) == "4" || Character.toString(parse_result.charAt(0)) == "5") {
        		    									continue;
        		    								}
        											
        											System.out.print("SYST\r\n");
        											outToServer.writeBytes("SYST\r\n");
        											parse_result = parse(inFromServer.readLine()+"\r\n");	// PARSE SERVER RESPONSE
        		    								if (parse_result == null || Character.toString(parse_result.charAt(0)) == "4" || Character.toString(parse_result.charAt(0)) == "5") {
        		    									continue;
        		    								}
        											
        											System.out.print("TYPE I\r\n");
        											outToServer.writeBytes("TYPE I\r\n");
        											parse_result = parse(inFromServer.readLine()+"\r\n");	// PARSE SERVER RESPONSE
        		    								if (parse_result == null || Character.toString(parse_result.charAt(0)) == "4" || Character.toString(parse_result.charAt(0)) == "5") {
        		    									continue;
        		    								}
        											
        											connect_lck=false;
    											} catch(Exception e1) {
    												continue;
    											}
    											//===================================================================================================
    						        		} else {
    						        			System.out.println("ERROR -- server-port");
    						        		}
    									} catch (NumberFormatException e) {	// catch if server_port is not an integer
    										System.out.println("ERROR -- server-port");
    									}
    								} else {
    									System.out.println("ERROR -- server-host");
    								}
    							} else {	//<server-host><sp+><server-port> doesn't match expected format
    								if (g3.matches("^.+?\\s+$")) {	// <server-port> is missing
    									System.out.println("ERROR -- server-port");
    								} else {
    									System.out.println("ERROR -- server-host");
    								}
    							}
    		        		}
    					}
    		        	if (g1.matches("(?i)get")) {				// GET ====================================================================================================
    						if (g3 != null && g3.matches("^[\\x00-\\x7F]*$")) {	// IS VALID COMMAND
    							if (connect_lck) {
    								System.out.println("ERROR -- expecting CONNECT");
    							} else {							// IS ALLOWED IN SEQUENCE
    								p_string = g3.replaceAll("^(\\\\|/)","");
    								p_string = g3;
    								
    								String myIP;
    								InetAddress myInet;
    								myInet = InetAddress.getLocalHost();
    								myIP = myInet.getHostAddress();
    								port_num1 = port_num / 256;
    								port_num2 = port_num - (256 * port_num1);
    								port_string = myIP.replace(".",",")+","+port_num1+","+port_num2;
    								//===================================================================================================
    								DataOutputStream outToServer = new DataOutputStream(FTPcontrol.getOutputStream());
									BufferedReader inFromServer = new BufferedReader(new InputStreamReader(FTPcontrol.getInputStream()));
									
    								try {
										welcomeSocket.close();
    									welcomeSocket = new ServerSocket(port_num);				// CREATE "WELCOME" SOCKET
    									System.out.println("GET accepted for "+p_string);
    								} catch(IOException e1) {
    									System.out.print("GET failed, FTP-data port not allocated.\r\n");
    									continue;
    								}
    								
    								try {
    									System.out.print("PORT "+port_string+"\r\n");			// ECHO COMMAND TO STD OUTPUT
        								outToServer.writeBytes("PORT "+port_string+"\r\n");		// SEND COMMAND TO SERVER
										port_num++;
//        								parse_result = parse(inFromServer.readLine()+"\r\n");	// PARSE SERVER RESPONSE
//        								if (parse_result == null || Character.toString(parse_result.charAt(0)) == "4" || Character.toString(parse_result.charAt(0)) == "5") {
//        									continue;
//        								}
        								//------THIS CODE IS HERE BC OF THE STUPIDEST BUG EVER--THE SERVER SEEMS TO RANDOMLY SEND A BLANK RESPONSE HERE, NO IDEA WHY-------------
        								String server_response = inFromServer.readLine();
        								if(!server_response.startsWith("200")) {	// THIS JUST IGNORES BLANK RESPONSE AND TRIES AGAIN
        									parse_result = parse(inFromServer.readLine()+"\r\n");	// PARSE SERVER RESPONSE
            								if (parse_result == null || Character.toString(parse_result.charAt(0)) == "4" || Character.toString(parse_result.charAt(0)) == "5") {
            									System.out.println(parse_result);
            									continue;
            								}
        								} else {	
        									parse_result = parse(server_response+"\r\n");	// PARSE SERVER RESPONSE
            								if (parse_result == null || Character.toString(parse_result.charAt(0)) == "4" || Character.toString(parse_result.charAt(0)) == "5") {
            									System.out.println(parse_result);
            									continue;
            								}
        								}
        								//------END OF STUPID CODE---------------------------------------------------------------------------------------------------------------
        								System.out.print("RETR "+p_string+"\r\n");				// REPEAT
        								outToServer.writeBytes("RETR "+p_string+"\r\n");
        								parse_result = parse(inFromServer.readLine()+"\r\n");	// TWICE because RETR gives 2 responses
        								if (parse_result == null || Character.toString(parse_result.charAt(0)).equals("4") || Character.toString(parse_result.charAt(0)).equals("5")) {
        									continue;
        								}					
        								parse_result = parse(inFromServer.readLine()+"\r\n");	// PARSE SERVER RESPONSE
        								if (parse_result == null || Character.toString(parse_result.charAt(0)).equals("4") || Character.toString(parse_result.charAt(0)).equals("5")) {
        									continue;
        								}
    								} catch(Exception e1) {
    									continue;
    								}
    								
    								Socket FTPdata = null;
    						        InputStream in = null;
    						        OutputStream out = null;
    								try {
    									FTPdata = welcomeSocket.accept();
    						        } catch (IOException ex) {
//    						            System.out.println("Can't accept client connection. ");
    						        	continue;
    						        }
    								try {
    						            in = FTPdata.getInputStream();
    						        } catch (IOException ex) {
//    						            System.out.println("Can't get socket input stream. ");
    						            FTPdata.close();
    						        	continue;
    						        }
    						        try {
    						        	file_num++;
    						        	String strng_file_num;
    						        	if (file_num < 10) {
    						        		strng_file_num = "00"+file_num;
    						        	} else if (file_num < 100) {
    						        		strng_file_num = "0"+file_num;
    						        	} else {
    						        		strng_file_num = Integer.toString(file_num);
    						        	}
    						            out = new FileOutputStream("retr_files/file"+strng_file_num);
    						        } catch (FileNotFoundException ex) {
//    						            System.out.println("File not found. ");
    						            in.close();
    						            FTPdata.close();
    						        	continue;
    						        }

    						        byte[] bytes = new byte[16*1024];

    						        int count;
    						        while ((count = in.read(bytes)) > 0) {
    						            out.write(bytes, 0, count);
    						        }

    						        out.close();
    						        in.close();
    						        FTPdata.close();
    						        welcomeSocket.close();
    								
    								//===================================================================================================
    							}
    						} else {
    							if (g2 != null && g2.matches("^[\r\n]+$")) {			// contains no spaces between command and eof
    		        				System.out.println("ERROR -- request");
    		        			} else {
    		        				System.out.println("ERROR -- pathname");
    		        			}
    						}
    					}
    		        	if (g1.matches("(?i)quit")) {				// QUIT ====================================================================================================
    						if (g2 != null && g2.matches("^[\r\n]+$")) {			// contains no spaces between command and eof
    							if (connect_lck) {					// IS VALID COMMAND
    								System.out.println("ERROR -- expecting CONNECT");
    							} else {							// IS ALLOWED IN SEQUENCE
    								System.out.println("QUIT accepted, terminating FTP client");
    								//===================================================================================================
    								try {
    									DataOutputStream outToServer = new DataOutputStream(FTPcontrol.getOutputStream());
    									BufferedReader inFromServer = new BufferedReader(new InputStreamReader(FTPcontrol.getInputStream()));
    									
        								System.out.print("QUIT\r\n");							// ECHO COMMAND TO STD OUTPUT
        								outToServer.writeBytes("QUIT\r\n");						// SEND COMMAND TO SERVER
//        								parse_result = parse(inFromServer.readLine()+"\r\n");	// PARSE SERVER RESPONSE
//        								if (parse_result == null || Character.toString(parse_result.charAt(0)) == "4" || Character.toString(parse_result.charAt(0)) == "5") {
//        									continue;
//        								}
        								//------THIS CODE IS HERE BC OF THE STUPIDEST BUG EVER--THE SERVER SEEMS TO RANDOMLY SEND A BLANK RESPONSE HERE, NO IDEA WHY-------------
        								String server_response = inFromServer.readLine();
        								if(!server_response.startsWith("221")) {	// THIS JUST IGNORES BLANK RESPONSE AND TRIES AGAIN
        									parse_result = parse(inFromServer.readLine()+"\r\n");	// PARSE SERVER RESPONSE
            								if (parse_result == null || Character.toString(parse_result.charAt(0)) == "4" || Character.toString(parse_result.charAt(0)) == "5") {
            									System.out.println(parse_result);
            									break;
            								}
        								} else {	
        									parse_result = parse(server_response+"\r\n");	// PARSE SERVER RESPONSE
            								if (parse_result == null || Character.toString(parse_result.charAt(0)) == "4" || Character.toString(parse_result.charAt(0)) == "5") {
            									System.out.println(parse_result);
            									break;
            								}
        								}
        								//------END OF STUPID CODE---------------------------------------------------------------------------------------------------------------
        								quit_lck = true;
										break;
    								} catch(Exception e1) {
    									continue;
    								}
    								//===================================================================================================
    							}
    						} else {
    							System.out.println("ERROR -- request");
    						}
    					}
    				} else {	// invalid command
    					System.out.println("ERROR -- request");
    				}
    			} else {	// didn't match regex pattern, should never occur
    				System.out.println("ERROR -- request");
    			}
    		}	// end if(!quit_lck)
		}	// end while
		FTPcontrol.close();
        s.close();
	}
	static String parse(String inputline) throws NoSuchElementException{
		String g1 = null, g2, g3;
		
//		System.out.print("PARSING SERVER REPONSE: "+inputline);//DELETE THIS
		Pattern p = Pattern.compile("(.+?)(?:(\\s+?)(.+?)?)?$");
		Matcher m = p.matcher(inputline);
		if (m.find()) {
			g1 = m.group(1);
	        g2 = m.group(2);
	        g3 = m.group(3);
	        if (g1 != null && g1.matches("^([1-5][0-9][0-9])$")) {	// Checking to see if <reply-code> is in range 100-599
	        	if (g2 != null && g2.matches("[ ]+$")) {				// checks <sp+> token to see if it only contains spaces
					if (g3 != null && g3.matches("^[\\x00-\\x7F]*$")) {	// checks ascii in <reply-text> token
						if (!inputline.endsWith("\r\n")) {				// ends with CRLF?
							System.out.println("ERROR -- <CRLF>");
							return g1;
						} else {
							System.out.println("FTP reply "+g1+" accepted. Text is: "+g3);
							return g1;
						}
					} else {
						System.out.println("ERROR -- reply-text");
						return g1;
					}
				} else {
					if (g2.matches("^[\n\r]+")) {		// <sp+> has no spaces and \r or \n
						System.out.println("ERROR -- reply-code");
						return g1;
					} else {							// <sp+> has spaces and \r or \n
						System.out.println("ERROR -- reply-text");
						return g1;
					}
				}
			} else {	// invalid <reply-code>
				System.out.println("ERROR -- reply-code");
				return g1;
			}
		} else {	// didn't match regex pattern, should never occur
			System.out.println("ERROR -- reply-code");
			return g1;
		}
	}
}
