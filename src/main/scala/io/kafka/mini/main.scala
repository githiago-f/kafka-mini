package io.kafka.mini

import java.net.Socket
import java.nio.ByteBuffer
import java.net.InetSocketAddress
import java.nio.channels.{SocketChannel,ServerSocketChannel}
import java.io.{FileInputStream,IOException,DataOutputStream}

val PORT 		= 9386
val HOST 		= "localhost"
val FILE_SIZE 	= 10485760L
val CACHE_PATH 	= (os.root / "tmp" / "cache-10MB.txt").toString

def handleConnection(listener: ServerSocketChannel) = {
	val dst = ByteBuffer.allocate(4096);
	while(true) {
		val conn = listener.accept();
		println(s"Accepted $conn");
		conn.configureBlocking(true);

		var nread = 0;
		while (nread != -1)  {
			try {
				nread = conn.read(dst);
			} catch {
				case e: IOException => {
					e.printStackTrace()
					nread = -1;
				}
			}
			dst.rewind();
		}
	}
}

def dummyServer() = {
	try {
		val listener = ServerSocketChannel.open();
		val serverSocket = listener.socket()
		serverSocket.setReuseAddress(true)
		serverSocket.bind(InetSocketAddress(PORT))

		handleConnection(listener)
	} catch {
		case e: IOException => println(s"Failed to bind $PORT. ERR: ${e.getMessage}")
	}
}

@main
def main(): Unit = {
	val thread = Thread.ofPlatform.unstarted(() => dummyServer())
	thread.start()

	val socket = Socket(HOST, PORT)
	println(s"Connected with server ${socket.getInetAddress}:${socket.getPort}")

	val inputStream = FileInputStream(CACHE_PATH)
	val outputStream = DataOutputStream(socket.getOutputStream)
	
	val start = System.currentTimeMillis

	var transfering = Array.ofDim[Byte](4096)
	var read = 0;
	var total = 0;
	
	read = inputStream.read(transfering)
	while(read >= 0) {
		outputStream.write(transfering)
		total += read

		read = inputStream.read(transfering)
	}
	
	println(s"Bytes sent ::: $total :: ${System.currentTimeMillis - start}ms")
}
