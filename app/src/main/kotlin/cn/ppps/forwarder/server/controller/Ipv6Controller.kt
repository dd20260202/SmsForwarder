package cn.ppps.forwarder.server.controller

import com.yanzhenjie.andserver.annotation.GetMapping
import com.yanzhenjie.andserver.annotation.RestController
import cn.ppps.forwarder.utils.Ipv6Utils

/**
 * 自定义 IPv6 查询接口
 * 供云端 Node.js / Cloudflare Worker 调用
 */
@RestController
class Ipv6Controller {

    @GetMapping("/ipv6/query")
    fun queryIpv6(): String {
        // 直接调用我们之前写好的硬核查询逻辑，并返回 IP 字符串
        return Ipv6Utils.getIPv6Address()
    }
    
}
