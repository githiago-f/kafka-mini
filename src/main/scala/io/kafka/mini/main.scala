package io.kafka.mini

import java.nio.ByteBuffer
import java.net.InetSocketAddress
import java.io.{FileInputStream,IOException}
import java.nio.channels.{SocketChannel,ServerSocketChannel}

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
	Thread.ofPlatform.unstarted(() => dummyServer()).start();

	val socket = SocketChannel.open()
	socket.connect(InetSocketAddress(HOST, PORT))
	socket.configureBlocking(true)

	println(s"Sending transferTo system call from $CACHE_PATH to socket")

	val fileChannel = FileInputStream(CACHE_PATH).getChannel
	val start = System.currentTimeMillis

	var bytesSent = fileChannel.transferTo(0, FILE_SIZE, socket)
	println(s"Bytes sent ::: $bytesSent :: ${System.currentTimeMillis - start}ms")
}
