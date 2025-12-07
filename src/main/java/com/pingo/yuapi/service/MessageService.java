package com.pingo.yuapi.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface MessageService {
    
    /**
     * 获取用户的消息会话列表
     */
    List<Map<String, Object>> getMessageSessions(String userId);
    
    /**
     * 获取聊天室信息
     */
    Map<String, Object> getChatRoom(String tripId, String userId);
    
    /**
     * 获取聊天历史记录
     */
    List<Map<String, Object>> getChatHistory(String chatRoomId, String userId, int page, int pageSize);
    
    /**
     * 发送消息
     */
    Map<String, Object> sendMessage(Map<String, Object> messageData);
    
    /**
     * 发送位置消息
     */
    Map<String, Object> sendLocationMessage(Map<String, Object> locationData);
    
    /**
     * 标记消息为已读
     */
    boolean markMessagesAsRead(String chatRoomId, String userId);
    
    /**
     * 上传聊天图片
     */
    String uploadChatImage(String userId, MultipartFile file);
    
    /**
     * 获取未读消息数量
     */
    int getUnreadMessageCount(String userId);
}