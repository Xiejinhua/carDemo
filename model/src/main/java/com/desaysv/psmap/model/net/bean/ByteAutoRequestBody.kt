package com.desaysv.psmap.model.net.bean

data class ByteAutoRequestBody(
    var query: String, //提问
    var chat_id: String, //会话ID，由请求方生成并维护，需全局唯一，需保证一段完整的会话使用同一个 chat_id
    var question_id: String? = null, //默认需传入前置请求生成的question_id
    var intent_type: String, //意图，不同意图会调用不同的插件，不同插件返回的数据会不一样
    var intent_log_id: String, //意图请求的唯一标识，会在意图识别接口中返回，获取logid
    var plugin_name: String ="", //要调用的插件名称，如果传了值则直接调用此插件，同时也需要传入意图的结果
    var limit: Int? = null, //返回结果的数量限制，默认5
    var character: String? = null, //人设，需要大模型总结的插件可指定人设
    var history:String? = null, //历史对话记录，格式为json字符串，包含用户和助手的对话内容，最多100条，超过100条会被截断
    var car_info: String?= null, //车辆信息、状态信息等
    var plugin_params: String? = null, //见下方自定义参数说明公版-座舱大模型接口访问指南
    var extra: String? = null, //按需传递
)
