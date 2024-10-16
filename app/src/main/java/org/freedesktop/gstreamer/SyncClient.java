package org.freedesktop.gstreamer;

import android.os.StrictMode;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class SyncClient {
  private static final int PORT = 20001;
  private static final String MULTICAST_ADDRESS = "224.0.0.2";


  public static void startListeningForSync(Player player) {
    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
    StrictMode.setThreadPolicy(policy);
    new Thread(() -> {
      try {
//                InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
        DatagramSocket socket = new DatagramSocket(PORT, InetAddress.getByName("0.0.0.0"));
        socket.setBroadcast(true);
        byte[] buffer = new byte[1024];

        while (true) {
          // Receive the sync message
          DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
          socket.receive(packet);
          String syncMessage = new String(packet.getData(), 0, packet.getLength());

          // Parse and process the sync message
          if (syncMessage.startsWith("SYNC:")) {
            Log.d("SyncClient", "Received sync message: " + syncMessage);
            String[] parts = syncMessage.split(":");
            int port = Integer.parseInt(parts[1]);
            long base = Long.parseLong(parts[2]);
            String ip = parts[3];
            long currentPosition = Long.parseLong(parts[4]);
            if (player.isMaster) {
              ip = "127.0.0.1";
            }
            player.setTimeAndPlay(port, base, ip, currentPosition);

//
//                        // Calculate the elapsed time since the message was sent
//                        long currentTimestamp = System.currentTimeMillis();
//                        long elapsed = currentTimestamp - masterTimestamp;
//
//                        // Calculate the adjusted position the master is now at
//                        int adjustedMasterPosition = masterPosition + (int) elapsed;
//
//                        // Get the current position of the client
//                        int currentPosition = mediaPlayer.getCurrentPosition();
//
//                        // Calculate drift
//                        int drift = adjustedMasterPosition - currentPosition;
//
//                        // If the drift exceeds a certain threshold, adjust using seekTo
//                        int threshold = 500; // Acceptable drift threshold in milliseconds
//                        if (Math.abs(drift) > threshold) {
//                            mediaPlayer.seekTo(adjustedMasterPosition);
//                            System.out.println("Adjusted playback position to: " + adjustedMasterPosition);
//                        }
          }
        }
      } catch (Exception e) {
        Log.e("SyncClient", "Error receiving sync message", e);
      }
    }).start();
  }
}
