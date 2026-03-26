package cn.ppps.forwarder.server.controller

import com.yanzhenjie.andserver.annotation.GetMapping
import com.yanzhenjie.andserver.annotation.PostMapping // 👈 新增：引入 POST 支持
import com.yanzhenjie.andserver.annotation.RestController
import cn.ppps.forwarder.utils.Ipv6Utils

@RestController
class Ipv6Controller {

    // 1. 支持浏览器直接访问测试 (GET 请求)
    @GetMapping("/ipv6/query")
    fun queryIpv6Get(): String {
        return Ipv6Utils.getIPv6Address()
    }

    // 2. 支持脚本自动化调用 (POST 请求)
    @PostMapping("/ipv6/query")
    fun queryIpv6Post(): String {
        return Ipv6Utils.getIPv6Address()
    }
    
}
