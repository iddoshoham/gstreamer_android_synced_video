package org.freedesktop.gstreamer;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.StrictMode;
import android.util.Log;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class SyncServer {
  private static final int PORT = 20001;
  private static final String MULTICAST_ADDRESS = "224.0.0.2";
  private static Context context;
  private static Player player;
  private final int SYNC_INTERVAL = 2000; // Send sync message every 2 seconds


  public static void initOnce(Context context) {
    if (SyncServer.context != null) {
      return;
    }
    SyncServer.context = context;
    Thread t = new Thread(() -> {
      while (true) {
        if (player != null && player.isMaster) {
          try {
            SyncServer.sendSyncMessage(50000, player.baseTime, player.myIPAddress);
          } catch (Exception e) {
            Log.e("SyncServer", "Failed to send sync message", e);
          }
          try {
            Thread.sleep(10000);
          } catch (InterruptedException e) {
            Log.e("SyncServer", "Failed to sleep", e);
          }
        }
      }

    });
    t.start();
  }


  public static final int byteArrayToInt(byte[] arr, int offset) {
    if (arr == null || arr.length - offset < 4)
      return -1;

    int r0 = (arr[offset] & 0xFF) << 24;
    int r1 = (arr[offset + 1] & 0xFF) << 16;
    int r2 = (arr[offset + 2] & 0xFF) << 8;
    int r3 = arr[offset + 3] & 0xFF;
    return r0 + r1 + r2 + r3;
  }

  public static NetworkInterface getWifiNetworkInterface(WifiManager manager) {

    Enumeration<NetworkInterface> interfaces = null;
    try {
      interfaces = NetworkInterface.getNetworkInterfaces();
    } catch (SocketException e) {
      return null;
    }

    int wifiIP = manager.getConnectionInfo().getIpAddress();
    int reverseWifiIP = Integer.reverseBytes(wifiIP);

    while (interfaces.hasMoreElements()) {

      NetworkInterface iface = interfaces.nextElement();
      Enumeration<InetAddress> inetAddresses = iface.getInetAddresses();
      while (inetAddresses.hasMoreElements()) {
        InetAddress nextElement = inetAddresses.nextElement();
        int byteArrayToInt = byteArrayToInt(nextElement.getAddress(), 0);

        if (byteArrayToInt == wifiIP || byteArrayToInt == reverseWifiIP) {
          return iface;
        }
      }
    }

    return null;
  }

//  public static InetAddress getBroadcastAddress() throws SocketException {
//
//    NetworkInterface networkInterface = getWifiNetworkInterface((WifiManager) context.getSystemService(Context.WIFI_SERVICE));
//    List<InterfaceAddress> interfaceAddresses = networkInterface.getInterfaceAddresses();
//    for (InterfaceAddress interfaceAddress : interfaceAddresses) {
//      if (interfaceAddress.getBroadcast() != null) {
//        return interfaceAddress.getBroadcast();
//        // log.info("broadcast address found: " +
//        // broadcastAddress.toString());
//        // break;
//      }
//    }
//
//    List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
//    for (NetworkInterface intf : interfaces) {
//
//      List<InterfaceAddress> interfaceAddresses2 = intf.getInterfaceAddresses();
//      for (InterfaceAddress interfaceAddress : interfaceAddresses2) {
//        if (interfaceAddress.getBroadcast() != null) {
//          return interfaceAddress.getBroadcast();
//        }
//      }
//
//    }
//
//    return null;
//  }

  static InetAddress getBroadcastAddress() throws IOException {
    WifiManager wifi = (WifiManager)
      context.getSystemService(Context.WIFI_SERVICE);
    DhcpInfo dhcp = wifi.getDhcpInfo();


    int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
    byte[] quads = new byte[4];
    for (int k = 0; k < 4; k++)
      quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
    return InetAddress.getByAddress(quads);
  }

  public static void sendSyncMessage(int port, long base, String ipAddress) throws IOException {
    DatagramSocket socket = new DatagramSocket();
    socket.setBroadcast(true);
    String syncMessage = "SYNC:" + port + ":" + base + ":" + ipAddress;


    // Send the sync message to the multicast group
    byte[] buffer = syncMessage.getBytes();
    InetAddress group = getBroadcastAddress();
    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
    socket.send(packet);

    // Log for debugging
    System.out.println("Sent sync message: " + syncMessage);


  }

  public static void setPlayer(Player player) {

    SyncServer.player = player;
  }
//    public void startSyncing() {
//        new Thread(() -> {
//            try {
////                InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
//                MulticastSocket socket = new MulticastSocket();
//                socket.setTimeToLive(1); // Set TTL to 1 to keep multicast within the local network
//
//                while (true) {
//                    // Get the current playback position
//
//                    long timestamp = System.currentTimeMillis();
//                    String syncMessage = "SYNC:" + currentPosition + ":" + timestamp;
//
//                    // Send the sync message to the multicast group
//                    byte[] buffer = syncMessage.getBytes();
//                    InetAddress group = getBroadcastAddress();
//                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
//                    socket.send(packet);
//
//                    // Log for debugging
//                    System.out.println("Sent sync message: " + syncMessage);
//
//                    // Wait for the sync interval
//                    Thread.sleep(SYNC_INTERVAL);
//                }
//            } catch (IOException | InterruptedException e) {
//                e.printStackTrace();
//            }
//        }).start();
//    }
}
