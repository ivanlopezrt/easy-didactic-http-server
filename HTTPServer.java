import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;

public class HTTPServer {

	private static final int TAM_BUFFER=1024;
	private static String wwwroot;
	private static String respuestaNOTFOUND="<html><body><p>ERROR 404 - NOT FOUND</p></body></html>";
	
	public static void main(String args[]) throws IOException {
		
			if(args.length!=2)
				throw new IllegalArgumentException("Parametros: <puerto servidor> <ruta raiz>");
			
			int puertoServidor=Integer.parseInt(args[0]);
			
			ServerSocket servidor=new ServerSocket(puertoServidor);
			wwwroot=args[1];
			
			System.out.println("Servidor arrancado en el puerto "+puertoServidor);
			
			
			while(true) {
				Socket cliente=servidor.accept();
				new Handler(cliente).start();
			}
	}
		
	public static class Handler extends Thread{

		int tamMensaje;
		byte[] byteBuffer=new byte[TAM_BUFFER];
		
		Socket mCliente;
		
		Handler(Socket cliente){
			mCliente=cliente;
		}
		
		@Override
		public void run() {
			String url="";
			try {
				System.out.println("Cliente conectado desde "+mCliente.getInetAddress() + 
						" al puerto " + mCliente.getLocalPort() + " desde el puerto " + mCliente.getPort());
				InputStream in=mCliente.getInputStream();
				OutputStream out=mCliente.getOutputStream();
				
				tamMensaje=in.read(byteBuffer); 
				String request=new String(byteBuffer);
				String lineas[]=request.split("\n");
				if(lineas.length>0) {
					String tokens[]=lineas[0].split(" ");
					
					//EL REQUEST DE TIPO GET LLEVA GET url HTTP/1.1
					if(tokens.length==3) {
						System.out.println(tokens[0]+" "+tokens[1]);
						if(tokens[0].equals("GET")) {
							url=tokens[1];
							sirve(url,out);
						}
						else
							out.write("HTTP/1.1 405 Method Not Allowed\n\n".getBytes());
					}
					else
						out.write("HTTP/1.1 400 Bad Request\n\n".getBytes());
				}
			
				
				out.close();
				mCliente.close();
				System.out.println(url + " cerrado");
				
			}catch(IOException io) {
				io.printStackTrace();
			}
			
		}
		
		void sirve(String url,OutputStream out) throws IOException {
			String respuestaOK="HTTP/1.1 200 OK\n\n";
			
			if(url.equals("/"))
				url=HTTPServer.wwwroot+"/index.html";
			else
				url=HTTPServer.wwwroot+url;
			
			File fichero=new File(url);
			if(fichero.exists()) {
				System.out.println("... Sirviendo "+url);
				byte [] bytes=Files.readAllBytes(fichero.toPath());
				out.write(respuestaOK.getBytes());
				out.write(bytes);
				out.flush();
			}
			else
				out.write(("HTTP/1.1 404 Not Found\n\n"+HTTPServer.respuestaNOTFOUND).getBytes());
		}
		
		
	}

}
