// 👇 第一行必须是这个新的门牌号，不能是 com.idormy...
package cn.ppps.forwarder.utils

import android.util.Log
import java.net.Inet6Address
import java.net.NetworkInterface
import java.net.SocketException
import java.util.*

object Ipv6Utils {

    const val LOOPBACK_ADDRESS = "0:0:0:0:0:0:0:1"

    fun getIPv6Address(): String {
        try {
            val networkInterfaces = NetworkInterface.getNetworkInterfaces()
            val guaAddresses = mutableListOf<Inet6Address>()
            val ulaAddresses = mutableListOf<Inet6Address>()
            val linkLocalAddresses = mutableListOf<Inet6Address>()

            while (networkInterfaces.hasMoreElements()) {
                val networkInterface = networkInterfaces.nextElement()
                if (networkInterface.isLoopback || !networkInterface.isUp) {
                    continue
                }

                val inetAddresses = networkInterface.inetAddresses
                while (inetAddresses.hasMoreElements()) {
                    val inetAddress = inetAddresses.nextElement()
                    if (inetAddress is Inet6Address) {
                        Log.d("Ipv6Utils", "发现 IPv6: ${inetAddress.hostAddress}, 属性: ${getIpv6Attr(inetAddress)}")
                        
                        when {
                            inetAddress.isLinkLocalAddress -> linkLocalAddresses.add(inetAddress)
                            inetAddress.isSiteLocalAddress -> ulaAddresses.add(inetAddress)
                            !inetAddress.isLoopbackAddress -> guaAddresses.add(inetAddress)
                        }
                    }
                }
            }

            if (guaAddresses.isNotEmpty()) {
                guaAddresses.sortBy { isTemporary(it) } 
                // 👇 这里就是之前修复过缺空格的地方
                return guaAddresses[0].hostAddress.split("%")[0] 
            }

            if (ulaAddresses.isNotEmpty()) {
                return ulaAddresses[0].hostAddress.split("%")[0]
            }

            if (linkLocalAddresses.isNotEmpty()) {
                return linkLocalAddresses[0].hostAddress.split("%")[0]
            }

        } catch (e: SocketException) {
            Log.e("Ipv6Utils", "获取 IPv6 失败: ${e.message}")
        }
        return LOOPBACK_ADDRESS
    }

    private fun isTemporary(address: Inet6Address): Boolean {
        return false 
    }

    private fun getIpv6Attr(addr: Inet6Address): String {
        return when {
            addr.isLinkLocalAddress -> "Link-Local"
            addr.isSiteLocalAddress -> "ULA/Site-Local"
            addr.isMulticastAddress -> "Multicast"
            addr.isLoopbackAddress -> "Loopback"
            else -> "Global Unicast (Public)"
        }
    }
}
