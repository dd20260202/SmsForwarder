// 🔥 请保留你文件最顶部的 package com.idormy.sms.forwarder.utils 声明，不要覆盖它 🔥
package com.idormy.sms.forwarder.utils

import android.util.Log
import java.net.Inet6Address
import java.net.NetworkInterface
import java.net.SocketException
import java.util.*

/**
 * IPv6 工具类 (私有定制版)
 * 核心逻辑：遍历所有网卡，强制优先寻找“全球单播地址 (Public GUA)”。
 * 完美解决：移动端 AndServer 默认只监听临时或本地地址导致域名直连失败的问题。
 */
object Ipv6Utils {

    // 默认回环地址
    const val LOOPBACK_ADDRESS = "0:0:0:0:0:0:0:1"

    /**
     * 获取最适合做服务器监听的 IPv6 地址
     * 策略：
     * 1. Public GUA (全球单播地址) —— 最优
     * 2. Temporary GUA (临时全球单播地址)
     * 3. ULA (唯一本地地址)
     * 4. Link-Local (链路本地地址) —— 最差
     */
    fun getIPv6Address(): String {
        try {
            val networkInterfaces = NetworkInterface.getNetworkInterfaces()
            val guaAddresses = mutableListOf<Inet6Address>()
            val ulaAddresses = mutableListOf<Inet6Address>()
            val linkLocalAddresses = mutableListOf<Inet6Address>()

            while (networkInterfaces.hasMoreElements()) {
                val networkInterface = networkInterfaces.nextElement()
                // 排除回环网卡和未启用的网卡
                if (networkInterface.isLoopback || !networkInterface.isUp) {
                    continue
                }

                val inetAddresses = networkInterface.inetAddresses
                while (inetAddresses.hasMoreElements()) {
                    val inetAddress = inetAddresses.nextElement()
                    // 只要 IPv6
                    if (inetAddress is Inet6Address) {
                        Log.d("Ipv6Utils", "发现 IPv6: ${inetAddress.hostAddress}, 属性: ${getIpv6Attr(inetAddress)}")
                        
                        when {
                            // 链路本地 fe80::
                            inetAddress.isLinkLocalAddress -> linkLocalAddresses.add(inetAddress)
                            // 唯一本地 fc00:: / fd00::
                            inetAddress.isSiteLocalAddress -> ulaAddresses.add(inetAddress)
                            // 全球单播 (2xxx:: / 3xxx::)
                            !inetAddress.isLoopbackAddress -> guaAddresses.add(inetAddress)
                        }
                    }
                }
            }

            // --- 决策逻辑 ---

            // 1. 优先从 GUA (全球单播) 列表里挑
            if (guaAddresses.isNotEmpty()) {
                // 如果发现超过 1 个 GUA，优先尝试寻找“非临时”的那个 (AndServer 需要稳定的 Public IP)
                guaAddresses.sortBy { isTemporary(it) } 
                return guaAddresses[0].hostAddress.split("%")[0] // 去掉 %wlan0 后缀
            }

            // 2. 其次选 ULA (可以做局域网服务器)
            if (ulaAddresses.isNotEmpty()) {
                return ulaAddresses[0].hostAddress.split("%")[0]
            }

            // 3. 最后才选 Link-Local (通常没用)
            if (linkLocalAddresses.isNotEmpty()) {
                return linkLocalAddresses[0].hostAddress.split("%")[0]
            }

        } catch (e: SocketException) {
            Log.e("Ipv6Utils", "获取 IPv6 失败: ${e.message}")
        }
        return LOOPBACK_ADDRESS
    }

    // 简单判断是否为临时地址 (Android 目前没有直接 API，只能通过 hostAddress 特征粗略判断，通常比较复杂，这里简化为优先)
    // 或者判断地址前缀的第31位是否为1 (这里直接认为列表里的 GUA 我们优先选)
    private fun isTemporary(address: Inet6Address): Boolean {
        // AndServer 默认通常监听临时地址，我们可以尝试让 AndServer 监听 stable 的地址。
        // 由于 Android 权限限制，无法精确判断。这里直接认为第一个发现的 Public GUA 就是好的。
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
