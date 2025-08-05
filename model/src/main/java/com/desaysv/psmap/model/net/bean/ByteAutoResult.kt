package com.desaysv.psmap.model.net.bean

data class ByteAutoResult(
    val error_code: Int? = null, // 错误码，非0为错误
    val error_message: String, // 错误时的消息
    val tts_text: String? = null, // TTS使用的文本
    val content: String? = null, // 上屏显示的自然语言，每条拼一起即为完整响应
    val reasoning_content: String? = null, // 思维链内容，非推理阶段此字段不返回。
    val stage: String? = null, // 请求阶段
    val plugin_name: String? = null, // 插件名，和observation成对出现，用于确定ovservation的类型
    val observation: String? = null, // 结构化数据，不同插件结构不同，比如视频插件的数据会放在这里
    val intent_type: String? = null, // 意图结果，一般只出现在一步接口里
    val origin_intent_type: String? = null, // 原始意图，有些定制逻辑会改写意图结果
    val keywords: String? = null, // 对模型回复的总结提炼，需要主动配置才有，回复大于50个字才总结
    val signals: String? = null, // 信号筛选结果
    val rejection: String? = null, // 拒识结果，只出现在一步接口里
//    val debug_info: String? = null, // 调试信息
)

data class Observation(
    val has_more: Boolean? = null, // 是否有更多数据
    val list: List<Item>? = null, // 数据列表
    val next_offset: Int? = null, // 下一个偏移量，用于分页
    val schema: String? = null // 数据的schema，描述数据结构
)

data class Item(
    val auth_info: AuthInfo? = null,
    val cover_uri: String? = null,
    val cover_url: String? = null,
    val description: String? = null,
    val detail_url: String? = null,
    val duration: Int? = null,
    val from_app: String? = null,
    val hints: Any? = null,
    val item_id: String? = null,
    val item_type: String? = null,
    val play_url: String? = null,
    val schema: String? = null,
    val source: String? = null,
    val statistics: Statistics? = null,
    val title: String? = null,
    val video_id: String? = null
)

data class Statistics(
    val like_count: Int? = null, // 点赞数
)

data class AuthInfo(
    val avatar: String? = null,
    val user_id: String? = null,
    val user_name: String? = null
)