package com.pingo.yuapi.service.impl;

import com.pingo.yuapi.service.MessageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class MessageServiceImpl implements MessageService {

    // 模拟消息存储
    private Map<String, List<Map<String, Object>>> messageStorage = new HashMap<>();
    
    // 模拟会话存储
    private Map<String, Map<String, Object>> sessionStorage = new HashMap<>();
    
    // 模拟聊天室存储
    private Map<String, Map<String, Object>> chatRoomStorage = new HashMap<>();

    public MessageServiceImpl() {
        initializeMockData();
    }

    @Override
    public List<Map<String, Object>> getMessageSessions(String userId) {
        List<Map<String, Object>> sessions = new ArrayList<>();
        
        // 模拟会话数据
        Map<String, Object> session1 = new HashMap<>();
        session1.put("id", "session_001");
        session1.put("type", "interaction");
        session1.put("title", "互动消息");
        session1.put("lastMessage", "您有新的拼车邀请");
        session1.put("timestamp", LocalDateTime.now().minusMinutes(2).toString());
        session1.put("unreadCount", 1);
        sessions.add(session1);

        Map<String, Object> session2 = new HashMap<>();
        session2.put("id", "session_002");
        session2.put("type", "trip_chat");
        session2.put("title", "行程聊天");
        session2.put("lastMessage", "司机：我已经到达上车点了");
        session2.put("timestamp", LocalDateTime.now().minusMinutes(10).toString());
        session2.put("unreadCount", 2);
        
        Map<String, Object> tripInfo = new HashMap<>();
        tripInfo.put("date", "今天");
        tripInfo.put("route", "荣盛阿尔卡迪亚 → 建国门");
        tripInfo.put("status", "行程进行中");
        session2.put("tripInfo", tripInfo);
        sessions.add(session2);

        Map<String, Object> session3 = new HashMap<>();
        session3.put("id", "session_003");
        session3.put("type", "group_chat");
        session3.put("title", "群群");
        session3.put("avatar", "/static/group-avatar.png");
        session3.put("lastMessage", "大家注意安全");
        session3.put("timestamp", LocalDateTime.now().minusMinutes(30).toString());
        session3.put("unreadCount", 0);
        sessions.add(session3);

        return sessions;
    }

    @Override
    public Map<String, Object> getChatRoom(String tripId, String userId) {
        Map<String, Object> chatRoom = chatRoomStorage.get(tripId);
        if (chatRoom == null) {
            // 创建新的聊天室
            chatRoom = new HashMap<>();
            chatRoom.put("id", tripId);
            chatRoom.put("tripId", tripId);
            
            Map<String, Object> tripInfo = new HashMap<>();
            tripInfo.put("route", "荣盛阿尔卡迪亚 → 建国门");
            tripInfo.put("departureTime", "今天 20:30");
            tripInfo.put("plateNumber", "京O88888");
            chatRoom.put("tripInfo", tripInfo);
            
            List<Map<String, Object>> participants = new ArrayList<>();
            Map<String, Object> driver = new HashMap<>();
            driver.put("id", "driver_001");
            driver.put("name", "张师傅");
            driver.put("avatar", "/static/driver-avatar.png");
            driver.put("role", "driver");
            participants.add(driver);
            
            Map<String, Object> passenger = new HashMap<>();
            passenger.put("id", userId);
            passenger.put("name", "乘客");
            passenger.put("avatar", "/static/passenger-avatar.png");
            passenger.put("role", "passenger");
            participants.add(passenger);
            
            chatRoom.put("participants", participants);
            chatRoom.put("unreadCount", 0);
            chatRoom.put("isActive", true);
            
            chatRoomStorage.put(tripId, chatRoom);
        }
        
        return chatRoom;
    }

    @Override
    public List<Map<String, Object>> getChatHistory(String chatRoomId, String userId, int page, int pageSize) {
        List<Map<String, Object>> messages = messageStorage.get(chatRoomId);
        if (messages == null) {
            messages = generateMockChatHistory(chatRoomId, userId);
            messageStorage.put(chatRoomId, messages);
        }
        
        // 分页处理
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, messages.size());
        
        if (start >= messages.size()) {
            return new ArrayList<>();
        }
        
        return new ArrayList<>(messages.subList(start, end));
    }

    @Override
    public Map<String, Object> sendMessage(Map<String, Object> messageData) {
        String chatRoomId = (String) messageData.get("chatRoomId");
        String senderId = (String) messageData.get("senderId");
        String content = (String) messageData.get("content");
        String type = (String) messageData.getOrDefault("type", "text");
        
        Map<String, Object> message = new HashMap<>();
        message.put("id", "msg_" + System.currentTimeMillis());
        message.put("chatRoomId", chatRoomId);
        message.put("senderId", senderId);
        message.put("senderName", "用户" + senderId.substring(senderId.length() - 3));
        message.put("senderAvatar", "/static/user-avatar.png");
        message.put("content", content);
        message.put("type", type);
        message.put("timestamp", LocalDateTime.now().toString());
        message.put("isRead", false);
        
        // 存储消息
        List<Map<String, Object>> messages = messageStorage.computeIfAbsent(chatRoomId, k -> new ArrayList<>());
        messages.add(message);
        
        return message;
    }

    @Override
    public Map<String, Object> sendLocationMessage(Map<String, Object> locationData) {
        String chatRoomId = (String) locationData.get("chatRoomId");
        String senderId = (String) locationData.get("senderId");
        Map<String, Object> location = (Map<String, Object>) locationData.get("location");
        
        String locationText = String.format("位置: %s (%.6f, %.6f)", 
                                          location.get("address"),
                                          (Double) location.get("latitude"), 
                                          (Double) location.get("longitude"));
        
        Map<String, Object> messageData = new HashMap<>();
        messageData.put("chatRoomId", chatRoomId);
        messageData.put("senderId", senderId);
        messageData.put("content", locationText);
        messageData.put("type", "location");
        
        return sendMessage(messageData);
    }

    @Override
    public boolean markMessagesAsRead(String chatRoomId, String userId) {
        List<Map<String, Object>> messages = messageStorage.get(chatRoomId);
        if (messages != null) {
            for (Map<String, Object> message : messages) {
                if (!userId.equals(message.get("senderId"))) {
                    message.put("isRead", true);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public String uploadChatImage(String userId, MultipartFile file) {
        try {
            // 创建上传目录
            String uploadDir = "uploads/chat/images/";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = userId + "_" + System.currentTimeMillis() + extension;
            
            // 保存文件
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath);
            
            // 返回文件URL
            return "/uploads/chat/images/" + filename;
            
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败", e);
        }
    }

    @Override
    public int getUnreadMessageCount(String userId) {
        int totalUnread = 0;
        
        for (List<Map<String, Object>> messages : messageStorage.values()) {
            for (Map<String, Object> message : messages) {
                if (!userId.equals(message.get("senderId")) && 
                    !(Boolean) message.getOrDefault("isRead", false)) {
                    totalUnread++;
                }
            }
        }
        
        return totalUnread;
    }

    /**
     * 初始化模拟数据
     */
    private void initializeMockData() {
        // 初始化一些模拟的聊天记录
        generateMockChatHistory("session_002", "user_001");
    }

    /**
     * 生成模拟聊天记录
     */
    private List<Map<String, Object>> generateMockChatHistory(String chatRoomId, String userId) {
        List<Map<String, Object>> messages = new ArrayList<>();
        LocalDateTime baseTime = LocalDateTime.now().minusHours(2);
        
        // 系统消息
        Map<String, Object> systemMsg = new HashMap<>();
        systemMsg.put("id", "msg_system_001");
        systemMsg.put("chatRoomId", chatRoomId);
        systemMsg.put("senderId", "system");
        systemMsg.put("senderName", "系统");
        systemMsg.put("senderAvatar", "");
        systemMsg.put("content", "行程开始，请保持联系");
        systemMsg.put("type", "system");
        systemMsg.put("timestamp", baseTime.toString());
        systemMsg.put("isRead", true);
        messages.add(systemMsg);

        // 司机消息
        Map<String, Object> driverMsg = new HashMap<>();
        driverMsg.put("id", "msg_driver_001");
        driverMsg.put("chatRoomId", chatRoomId);
        driverMsg.put("senderId", "driver_001");
        driverMsg.put("senderName", "张师傅");
        driverMsg.put("senderAvatar", "/static/driver-avatar.png");
        driverMsg.put("content", "大家好，我是今天的司机，车牌号京O88888");
        driverMsg.put("type", "text");
        driverMsg.put("timestamp", baseTime.plusMinutes(5).toString());
        driverMsg.put("isRead", true);
        messages.add(driverMsg);

        // 用户消息
        Map<String, Object> userMsg = new HashMap<>();
        userMsg.put("id", "msg_user_001");
        userMsg.put("chatRoomId", chatRoomId);
        userMsg.put("senderId", userId);
        userMsg.put("senderName", "我");
        userMsg.put("senderAvatar", "/static/user-avatar.png");
        userMsg.put("content", "好的，我在小区门口等您");
        userMsg.put("type", "text");
        userMsg.put("timestamp", baseTime.plusMinutes(10).toString());
        userMsg.put("isRead", true);
        messages.add(userMsg);

        return messages;
    }
}