package com.pingo.yuapi.controller;

import com.pingo.yuapi.common.Result;
import com.pingo.yuapi.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "*")
public class MessageController {

    @Autowired
    private MessageService messageService;

    /**
     * 获取消息会话列表
     */
    @GetMapping("/sessions")
    public Result<List<Map<String, Object>>> getMessageSessions() {
        try {
            String userId = getCurrentUserId();
            List<Map<String, Object>> sessions = messageService.getMessageSessions(userId);
            return Result.success(sessions);
        } catch (Exception e) {
            return Result.error("获取消息列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取聊天室信息
     */
    @GetMapping("/chatroom/{tripId}")
    public Result<Map<String, Object>> getChatRoom(@PathVariable String tripId) {
        try {
            String userId = getCurrentUserId();
            Map<String, Object> chatRoom = messageService.getChatRoom(tripId, userId);
            return Result.success(chatRoom);
        } catch (Exception e) {
            return Result.error("获取聊天室信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取聊天历史
     */
    @GetMapping("/history/{chatRoomId}")
    public Result<List<Map<String, Object>>> getChatHistory(
            @PathVariable String chatRoomId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        try {
            String userId = getCurrentUserId();
            List<Map<String, Object>> messages = messageService.getChatHistory(chatRoomId, userId, page, pageSize);
            return Result.success(messages);
        } catch (Exception e) {
            return Result.error("获取聊天记录失败: " + e.getMessage());
        }
    }

    /**
     * 发送消息
     */
    @PostMapping("/send")
    public Result<Map<String, Object>> sendMessage(@RequestBody Map<String, Object> messageData) {
        try {
            String userId = getCurrentUserId();
            messageData.put("senderId", userId);
            
            Map<String, Object> message = messageService.sendMessage(messageData);
            return Result.success(message);
        } catch (Exception e) {
            return Result.error("发送消息失败: " + e.getMessage());
        }
    }

    /**
     * 标记消息为已读
     */
    @PostMapping("/read")
    public Result<Boolean> markMessagesAsRead(@RequestBody Map<String, String> request) {
        try {
            String userId = getCurrentUserId();
            String chatRoomId = request.get("chatRoomId");
            
            boolean success = messageService.markMessagesAsRead(chatRoomId, userId);
            return Result.success(success);
        } catch (Exception e) {
            return Result.error("标记消息失败: " + e.getMessage());
        }
    }

    /**
     * 上传聊天图片
     */
    @PostMapping("/upload/image")
    public Result<String> uploadChatImage(@RequestParam("image") MultipartFile file) {
        try {
            String userId = getCurrentUserId();
            String imageUrl = messageService.uploadChatImage(userId, file);
            return Result.success(imageUrl);
        } catch (Exception e) {
            return Result.error("上传图片失败: " + e.getMessage());
        }
    }

    /**
     * 发送位置信息
     */
    @PostMapping("/location")
    public Result<Map<String, Object>> sendLocation(@RequestBody Map<String, Object> locationData) {
        try {
            String userId = getCurrentUserId();
            locationData.put("senderId", userId);
            
            Map<String, Object> message = messageService.sendLocationMessage(locationData);
            return Result.success(message);
        } catch (Exception e) {
            return Result.error("发送位置失败: " + e.getMessage());
        }
    }

    /**
     * 获取未读消息数量
     */
    @GetMapping("/unread/count")
    public Result<Integer> getUnreadCount() {
        try {
            String userId = getCurrentUserId();
            int count = messageService.getUnreadMessageCount(userId);
            return Result.success(count);
        } catch (Exception e) {
            return Result.error("获取未读消息数量失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前用户ID
     */
    private String getCurrentUserId() {
        // 从JWT token或session中获取用户ID
        // 这里返回模拟的用户ID
        return "user_001";
    }
}