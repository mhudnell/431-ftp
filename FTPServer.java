import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//	name:	Max Hudnell
//	pid:	7203 10449
public class FTPServer {
	static boolean pass_lck=false, retr_enable=false, user_lck=true, quit_lck=false;		// PASS, RETR, and USER "locks"
	
	public static void main(String args[]) throws Exception {
		String clientSentence;
		String g1, g2, g3, g21, g22, g23, g24, g25, g26, p_string;
		int port_num1, port_num2, converted_port_num, ctrl_port=17332;
		String get_host = "";
		int get_port = 0;
		if(args.length > 0){
			ctrl_port = Integer.parseInt(args[0]);
		}

		// Create "welcoming" socket
		ServerSocket welcomeSocket = new ServerSocket(ctrl_port);

		// While loop to handle arbitrary sequence of clients making requests
		while(true) {
			
			// Waits for a client to connect and creates new socket for connection
			Socket connectionSocket = welcomeSocket.accept();
			
			// Create (buffered) input stream attached to connection socket
//			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
			Scanner inFromClient = new Scanner(new InputStreamReader(connectionSocket.getInputStream())).useDelimiter("(?<=\r\n)|(?<=\r)(?=[^\n])|(?<=\n)");

			// Create output stream attached to connection socket
			DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());

			// Write output line to socket
			outToClient.writeBytes("220 COMP 431 FTP server ready.\r\n");
			
			while (inFromClient.hasNextLine()) {
				clientSentence = inFromClient.next();
//============================================================================================================================================================================
				Pattern p = Pattern.compile("(.+?)(?:(\\s+)(.+?)?)?$");
				Matcher m = p.matcher(clientSentence);
				if (!quit_lck) {
					if (m.find()) {
						g1 = m.group(1);
				        g2 = m.group(2);
				        g3 = m.group(3);
						System.out.print(clientSentence);	//print back the command //System.out.print(clientSentence); USED TO BE PRINT NOT PRINTLN
				        if (g1.matches("(?i)user|pass|type|syst|noop|quit|port|retr")) {	// valid command name?
				        	if (g1.matches("(?i)quit")) {				// QUIT ------------------------------------------------------------------
								if (g2 != null && g2.matches("\r\n")) {	// IS VALID COMMAND AND ALLOWED IN SEQUENCE
									System.out.print("221 Goodbye.\r\n");
									outToClient.writeBytes("221 Goodbye.\r\n");
									//quit_lck=true;
									break;
								} else {
									System.out.print("501 Syntax error in parameter.\r\n");
									outToClient.writeBytes("501 Syntax error in parameter.\r\n");
								}
							}
							if (g1.matches("(?i)noop")) {				// NOOP ------------------------------------------------------------------
								if (g2 != null && g2.matches("\r\n")) {
									if (user_lck) {
										System.out.print("530 Not logged in.\r\n");
										outToClient.writeBytes("530 Not logged in.\r\n");
									} else if (pass_lck){
										System.out.print("503 Bad sequence of commands.\r\n");
										outToClient.writeBytes("503 Bad sequence of commands.\r\n");
									} else {		// IS VALID COMMAND AND ALLOWED IN SEQUENCE
										System.out.print("200 Command OK.\r\n");
										outToClient.writeBytes("200 Command OK.\r\n");
									}
								} else {
									System.out.print("501 Syntax error in parameter.\r\n");
									outToClient.writeBytes("501 Syntax error in parameter.\r\n");
								}
							}
							if (g1.matches("(?i)syst")) {				// SYST ------------------------------------------------------------------
								if (g2 != null && g2.matches("\r\n")) {
									if (user_lck) {
										System.out.print("530 Not logged in.\r\n");
										outToClient.writeBytes("530 Not logged in.\r\n");
									} else if (pass_lck){
										System.out.print("503 Bad sequence of commands.\r\n");
										outToClient.writeBytes("503 Bad sequence of commands.\r\n");
									} else {		// IS VALID COMMAND AND ALLOWED IN SEQUENCE
										System.out.print("215 UNIX Type: L8.\r\n");
										outToClient.writeBytes("215 UNIX Type: L8.\r\n");
									}
								} else {
									System.out.print("501 Syntax error in parameter.\r\n");
									outToClient.writeBytes("501 Syntax error in parameter.\r\n");
								}
							}
							if (g1.matches("(?i)user")) {				// USER -----------------------------------------------------------------------------------------------
								if (g2.matches("[ ]+$")) {				// checks <sp+> token to see if it only contains spaces
									if (g3.matches("^[\\x00-\\x7F]*$")) {	// checks ascii in <username> token
										if (!clientSentence.endsWith("\r\n")) {				// ends with CRLF?
											System.out.print("501 Syntax error in parameter.\r\n");
											outToClient.writeBytes("501 Syntax error in parameter.\r\n");
										} else {
											if(!pass_lck) {		// IS VALID COMMAND AND ALLOWED IN SEQUENCE
												System.out.print("331 Guest access OK, send password.\r\n");
												outToClient.writeBytes("331 Guest access OK, send password.\r\n");
												pass_lck=true;
												user_lck=false;
											} else {
												System.out.print("503 Bad sequence of commands.\r\n");
												outToClient.writeBytes("503 Bad sequence of commands.\r\n");
											}
										}
									} else {
										System.out.print("501 Syntax error in parameter.\r\n");
										outToClient.writeBytes("501 Syntax error in parameter.\r\n");
									}
								} else {
									if (g2.matches("^[\n\r]+")) {		// <sp+> has no spaces and \r or \n
										System.out.print("500 Syntax error, command unrecognized.\r\n");
										outToClient.writeBytes("500 Syntax error, command unrecognized.\r\n");
									} else {							// <sp+> has spaces and \r or \n
										System.out.print("501 Syntax error in parameter.\r\n");
										outToClient.writeBytes("501 Syntax error in parameter.\r\n");
									}
								}
							}
							if (g1.matches("(?i)pass")) {				// PASS -----------------------------------------------------------------------------------------------
								if (g2 != null && g2.matches("[ ]+$")) {				// checks <sp+> token to see if it only contains spaces
									if (g3 != null && g3.matches("^[\\x00-\\x7F]*$")) {	// checks ascii in <password> token
										if (!clientSentence.endsWith("\r\n")) {				// ends with CRLF?
											System.out.print("501 Syntax error in parameter.\r\n");
											outToClient.writeBytes("501 Syntax error in parameter.\r\n");
										} else {
											if (user_lck) {
												System.out.print("530 Not logged in.\r\n");
												outToClient.writeBytes("530 Not logged in.\r\n");
											} else if (pass_lck){	// IS VALID COMMAND AND ALLOWED IN SEQUENCE
												System.out.print("230 Guest login OK.\r\n");
												outToClient.writeBytes("230 Guest login OK.\r\n");
												pass_lck=false;
											} else {
												System.out.print("503 Bad sequence of commands.\r\n");
												outToClient.writeBytes("503 Bad sequence of commands.\r\n");
											}
										}
									} else {
										System.out.print("501 Syntax error in parameter.\r\n");
										outToClient.writeBytes("501 Syntax error in parameter.\r\n");
									}
								} else {
									if (g2.matches("^[\n\r]+")) {		// <sp+> has no spaces and \r or \n
										System.out.print("500 Syntax error, command unrecognized.\r\n");
										outToClient.writeBytes("500 Syntax error, command unrecognized.\r\n");
									} else {							// <sp+> has spaces and \r or \n
										System.out.print("501 Syntax error in parameter.\r\n");
										outToClient.writeBytes("501 Syntax error in parameter.\r\n");
									}
								}
							}
							if (g1.matches("(?i)type")) {				// TYPE ------------------------------------------------------------------
								if (g2 != null && g2.matches("[ ]+$")) {				// checks <sp+> token to see if it only contains spaces
									if (g3 != null && g3.equals("A") | g3.equals("I")) {	// <type-code> is "A" or "I"
										if (!clientSentence.endsWith("\r\n")) {				// ends with CRLF?
											System.out.print("501 Syntax error in parameter.\r\n");
											outToClient.writeBytes("501 Syntax error in parameter.\r\n");
										} else {
											if (user_lck) {
												System.out.print("530 Not logged in.\r\n");
												outToClient.writeBytes("530 Not logged in.\r\n");
											} else if (pass_lck){
												System.out.print("503 Bad sequence of commands.\r\n");
												outToClient.writeBytes("503 Bad sequence of commands.\r\n");
											} else {		// IS VALID COMMAND AND ALLOWED IN SEQUENCE
												if (g3.equals("A")) {
													System.out.print("200 Type set to A.\r\n");
													outToClient.writeBytes("200 Type set to A.\r\n");
												} else {
													System.out.print("200 Type set to I.\r\n");
													outToClient.writeBytes("200 Type set to I.\r\n");
												}
											}
										}
									} else {
										System.out.print("501 Syntax error in parameter.\r\n");
										outToClient.writeBytes("501 Syntax error in parameter.\r\n");
									}
								} else {
									if (g2.matches("^[\n\r]+")) {		// <sp+> has no spaces and \r or \n
										System.out.print("500 Syntax error, command unrecognized.\r\n");
										outToClient.writeBytes("500 Syntax error, command unrecognized.\r\n");
									} else {							// <sp+> has spaces and \r or \n
										System.out.print("501 Syntax error in parameter.\r\n");
										outToClient.writeBytes("501 Syntax error in parameter.\r\n");
									}
								}
							}
							if (g1.matches("(?i)port")) {				// PORT ------------------------------------------------------------------
								if (g2 != null && g2.matches("[ ]+$")) {				// checks <sp+> token to see if it only contains spaces
									if (g3 != null && g3.matches("^([01]?[0-9]?[0-9],|2[0-4][0-9],|25[0-5],){5}+([01]?[0-9]?[0-9]|2[0-4][0-9]|25[0-5])$")) {	// checks port number
										if (!clientSentence.endsWith("\r\n")) {				// ends with CRLF?
											System.out.print("501 Syntax error in parameter.\r\n");
											outToClient.writeBytes("501 Syntax error in parameter.\r\n");
										} else {
											Pattern p2 = Pattern.compile("^([01]?[0-9]?[0-9]|2[0-4][0-9]|25[0-5]),"
																		+ "([01]?[0-9]?[0-9]|2[0-4][0-9]|25[0-5]),"
																		+ "([01]?[0-9]?[0-9]|2[0-4][0-9]|25[0-5]),"
																		+ "([01]?[0-9]?[0-9]|2[0-4][0-9]|25[0-5]),"
																		+ "([01]?[0-9]?[0-9]|2[0-4][0-9]|25[0-5]),"
																		+ "([01]?[0-9]?[0-9]|2[0-4][0-9]|25[0-5])$");
											Matcher m2 = p2.matcher(g3);
											if (m2.find()) {
												if (user_lck) {
													System.out.print("530 Not logged in.\r\n");
													outToClient.writeBytes("530 Not logged in.\r\n");
												} else if (pass_lck){
													System.out.print("503 Bad sequence of commands.\r\n");
													outToClient.writeBytes("503 Bad sequence of commands.\r\n");
												} else {	// IS VALID COMMAND AND ALLOWED IN SEQUENCE
													g21 = m2.group(1);
												    g22 = m2.group(2);
												    g23 = m2.group(3);
												    g24 = m2.group(4);
												    g25 = m2.group(5);
												    g26 = m2.group(6);
												    port_num1 = Integer.parseInt(g25);
												    port_num2 = Integer.parseInt(g26);
												    converted_port_num = (port_num1 * 256) + port_num2;
												    get_host = g21+"."+g22+"."+g23+"."+g24;
												    get_port = converted_port_num;
												    System.out.print("200 Port command successful ("+g21+"."+g22+"."+g23+"."+g24+","+converted_port_num+").\r\n");
												    outToClient.writeBytes("200 Port command successful ("+g21+"."+g22+"."+g23+"."+g24+","+converted_port_num+").\r\n");
												    retr_enable=true;
												}
											} else {
												System.out.print("501 Syntax error in parameter.\r\n");
												outToClient.writeBytes("501 Syntax error in parameter.\r\n");
											}
										}
									} else {
										System.out.print("501 Syntax error in parameter.\r\n");
										outToClient.writeBytes("501 Syntax error in parameter.\r\n");
									}
								} else {
									if (g2.matches("^[\n\r]+")) {		// <sp+> has no spaces and \r or \n
										System.out.print("500 Syntax error, command unrecognized.\r\n");
										outToClient.writeBytes("500 Syntax error, command unrecognized.\r\n");
									} else {							// <sp+> has spaces and \r or \n
										System.out.print("501 Syntax error in parameter.\r\n");
										outToClient.writeBytes("501 Syntax error in parameter.\r\n");
									}
								}
							}
							if (g1.matches("(?i)retr")) {				// RETR ------------------------------------------------------------------
								if (g2 != null && g2.matches("[ ]+$")) {				// checks <sp+> token to see if it only contains spaces
									if (g3 != null && g3.matches("^[\\x00-\\x7F]*$")) {	// checks ascii in <pathname> token
										if (!clientSentence.endsWith("\r\n")) {				// ends with CRLF?
											System.out.print("501 Syntax error in parameter.\r\n");
											outToClient.writeBytes("501 Syntax error in parameter.\r\n");
										} else {	
											if (user_lck) {
												System.out.print("530 Not logged in.\r\n");
												outToClient.writeBytes("530 Not logged in.\r\n");
											} else if (!pass_lck & retr_enable){	// IS VALID COMMAND AND ALLOWED IN SEQUENCE
												p_string = g3.replaceAll("^(\\\\|/)","");
												Path src = Paths.get(p_string);
												if (Files.exists(src)) {
													System.out.print("150 File status okay.\r\n");
													outToClient.writeBytes("150 File status okay.\r\n");
													//======================================================================================================================
													Socket FTPdata = null;
													try {
														FTPdata = new Socket(get_host, get_port);
				    								} catch(IOException e1) {
				    									System.out.print("425 Can not open data connection.\r\n");
														outToClient.writeBytes("425 Can not open data connection.\r\n");
				    									continue;
				    								}
													
													byte[] bytes = new byte[16 * 1024];
													InputStream in = new FileInputStream(new File(p_string));
											        OutputStream out = FTPdata.getOutputStream();
											        
											        int count;
											        while ((count = in.read(bytes)) > 0) {
											            out.write(bytes, 0, count);
											        }
											        
											        System.out.print("250 Requested file action completed.\r\n");
													outToClient.writeBytes("250 Requested file action completed.\r\n");
													retr_enable=false;
											        out.close();
											        in.close();
											        FTPdata.close();
											      //======================================================================================================================
												} else {
													System.out.print("550 File not found or access denied.\r\n");
													outToClient.writeBytes("550 File not found or access denied.\r\n");
												}
											} else {
												System.out.print("503 Bad sequence of commands.\r\n");
												outToClient.writeBytes("503 Bad sequence of commands.\r\n");
											}
										}
									} else {
										System.out.print("501 Syntax error in parameter.\r\n");
										outToClient.writeBytes("501 Syntax error in parameter.\r\n");
									}
								} else {
									if (g2.matches("^[\n\r]+")) {		// <sp+> has no spaces and \r or \n
										System.out.print("500 Syntax error, command unrecognized.\r\n");
										outToClient.writeBytes("500 Syntax error, command unrecognized.\r\n");
									} else {							// <sp+> has spaces and \r or \n
										System.out.print("501 Syntax error in parameter.\r\n");
										outToClient.writeBytes("501 Syntax error in parameter.\r\n");
									}
								}
							}
						} else if (g1.length()==3 || g1.length()==4) {
							System.out.print("502 Command not implemented.\r\n");
							outToClient.writeBytes("502 Command not implemented.\r\n");
						} else {	// invalid command
							System.out.print("500 Syntax error, command unrecognized.\r\n");
							outToClient.writeBytes("500 Syntax error, command unrecognized.\r\n");
						}
					} else {	// didn't match regex pattern, should never occur
						System.out.print("500 Syntax error, command unrecognized.\r\n");
						outToClient.writeBytes("500 Syntax error, command unrecognized.\r\n");
					}
				}
//============================================================================================================================================================================
			}	// end while(has.nextline())
			inFromClient.close();
			connectionSocket.close();
		}
	}
}

