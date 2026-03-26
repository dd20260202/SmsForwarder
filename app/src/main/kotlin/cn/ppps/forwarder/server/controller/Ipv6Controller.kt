package cn.ppps.forwarder.server.controller

import com.yanzhenjie.andserver.annotation.GetMapping
import com.yanzhenjie.andserver.annotation.RestController
// 👇 确保这里引入的是新包名下的 Ipv6Utils
import cn.ppps.forwarder.utils.Ipv6Utils 

@RestController
class Ipv6Controller {

    @GetMapping("/ipv6/query")
    fun queryIpv6(): String {
        return Ipv6Utils.getIPv6Address()
    }
    
}
