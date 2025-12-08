# 微信小程序集成指南

## 概述
本文档详细说明如何在YuGo拼车小程序中集成微信登录授权功能。

## 前端集成步骤

### 1. 微信登录流程

在小程序首次启动时，进行微信登录：

```javascript
// 在 app.js 或登录页面中
async function wechatLogin() {
  try {
    // 1. 调用微信登录API获取code
    const loginRes = await wx.login();
    if (!loginRes.code) {
      throw new Error('微信登录失败');
    }

    // 2. 将code发送到后端
    const response = await uni.request({
      url: 'http://localhost:8081/api/wechat/login',
      method: 'POST',
      data: {
        code: loginRes.code
      }
    });

    if (response.data.code === 200) {
      const { openid, sessionKey, unionid, isNewUser, userInfo } = response.data.data;
      
      // 保存登录信息
      uni.setStorageSync('openid', openid);
      uni.setStorageSync('sessionKey', sessionKey);
      uni.setStorageSync('unionid', unionid);

      if (isNewUser) {
        // 新用户需要授权
        return { needAuth: true, openid, sessionKey };
      } else {
        // 老用户直接登录成功
        uni.setStorageSync('userInfo', userInfo);
        return { needAuth: false, userInfo };
      }
    }
  } catch (error) {
    console.error('登录失败:', error);
    throw error;
  }
}
```

### 2. 用户信息授权

当用户点击授权按钮时，获取用户信息：

```javascript
// 在授权页面中
async function getUserInfo() {
  try {
    const openid = uni.getStorageSync('openid');
    const sessionKey = uni.getStorageSync('sessionKey');

    // 1. 获取用户信息
    const userInfoRes = await wx.getUserProfile({
      desc: '用于完善用户资料'
    });

    if (userInfoRes.errMsg === 'getUserProfile:ok') {
      // 2. 发送加密数据到后端解密
      const response = await uni.request({
        url: 'http://localhost:8081/api/wechat/auth/userinfo',
        method: 'POST',
        data: {
          openid: openid,
          encryptedData: userInfoRes.encryptedData,
          iv: userInfoRes.iv,
          sessionKey: sessionKey
        }
      });

      if (response.data.code === 200) {
        const { userId, userInfo } = response.data.data;
        
        // 保存用户信息
        uni.setStorageSync('userId', userId);
        uni.setStorageSync('userInfo', userInfo);
        
        return userInfo;
      }
    }
  } catch (error) {
    console.error('获取用户信息失败:', error);
    throw error;
  }
}
```

### 3. 手机号授权

当用户授权手机号时：

```javascript
// 在需要手机号的页面中
async function getPhoneNumber(e) {
  try {
    const openid = uni.getStorageSync('openid');
    const sessionKey = uni.getStorageSync('sessionKey');

    if (e.detail.errMsg === 'getPhoneNumber:ok') {
      // 发送加密的手机号数据到后端
      const response = await uni.request({
        url: 'http://localhost:8081/api/wechat/auth/phone',
        method: 'POST',
        data: {
          openid: openid,
          encryptedData: e.detail.encryptedData,
          iv: e.detail.iv,
          sessionKey: sessionKey
        }
      });

      if (response.data.code === 200) {
        const { phoneNumber } = response.data.data;
        
        // 更新本地存储的用户信息
        const userInfo = uni.getStorageSync('userInfo');
        userInfo.phone = phoneNumber;
        uni.setStorageSync('userInfo', userInfo);
        
        uni.showToast({
          title: '授权成功',
          icon: 'success'
        });
        
        return phoneNumber;
      }
    } else {
      throw new Error('用户拒绝授权手机号');
    }
  } catch (error) {
    console.error('获取手机号失败:', error);
    uni.showToast({
      title: '授权失败',
      icon: 'none'
    });
  }
}
```

### 4. 完整的授权流程示例

在小程序入口页面或授权页面：

```vue
<template>
  <view class="auth-container">
    <view class="logo-section">
      <image src="/static/logo.png" class="logo"></image>
      <text class="app-name">YuGo拼车</text>
    </view>

    <view class="auth-section" v-if="!isAuthorized">
      <view class="auth-tip">
        <text>请授权登录以享受完整服务</text>
      </view>
      
      <button class="auth-btn" @click="authorizeUserInfo" v-if="!hasUserInfo">
        <text>授权基本信息</text>
      </button>
      
      <button class="auth-btn" @click="authorizePhone" v-if="hasUserInfo && !hasPhone" 
              open-type="getPhoneNumber" @getphonenumber="getPhoneNumber">
        <text>授权手机号</text>
      </button>
      
      <button class="complete-btn" @click="completeAuth" v-if="hasUserInfo && hasPhone">
        <text>完成授权</text>
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue';

const isAuthorized = ref(false);
const hasUserInfo = ref(false);
const hasPhone = ref(false);
const openid = ref('');
const sessionKey = ref('');

onMounted(async () => {
  try {
    // 检查是否已经登录
    const loginResult = await wechatLogin();
    
    if (!loginResult.needAuth) {
      // 已经授权过的用户
      isAuthorized.value = true;
      navigateToMain();
    } else {
      // 新用户需要授权
      openid.value = loginResult.openid;
      sessionKey.value = loginResult.sessionKey;
      
      // 检查授权状态
      await checkAuthStatus();
    }
  } catch (error) {
    console.error('初始化失败:', error);
  }
});

const checkAuthStatus = async () => {
  try {
    const response = await uni.request({
      url: `http://localhost:8081/api/wechat/auth/status/${openid.value}`,
      method: 'GET'
    });

    if (response.data.code === 200) {
      const { hasUserInfo: hasUser, hasPhone: hasPhoneNum } = response.data.data;
      hasUserInfo.value = hasUser;
      hasPhone.value = hasPhoneNum;
      
      if (hasUser && hasPhoneNum) {
        isAuthorized.value = true;
        navigateToMain();
      }
    }
  } catch (error) {
    console.error('检查授权状态失败:', error);
  }
};

const authorizeUserInfo = async () => {
  try {
    await getUserInfo();
    hasUserInfo.value = true;
  } catch (error) {
    uni.showToast({
      title: '授权失败',
      icon: 'none'
    });
  }
};

const authorizePhone = () => {
  // 这个方法会触发button的open-type="getPhoneNumber"
};

const getPhoneNumber = async (e) => {
  try {
    await getPhoneNumber(e);
    hasPhone.value = true;
  } catch (error) {
    console.error('手机号授权失败:', error);
  }
};

const completeAuth = () => {
  isAuthorized.value = true;
  navigateToMain();
};

const navigateToMain = () => {
  uni.reLaunch({
    url: '/pages/index/index-new'
  });
};

// 这里引入之前定义的wechatLogin, getUserInfo等函数
// ...
</script>
```

## 后端配置

### 1. 微信小程序配置

在 `application.yml` 中配置真实的微信小程序信息：

```yaml
wechat:
  miniprogram:
    appid: wx1234567890abcdef  # 替换为你的小程序AppID
    secret: your-secret-key     # 替换为你的小程序Secret
```

### 2. 依赖添加

在 `pom.xml` 中添加必要的依赖：

```xml
<!-- 微信相关依赖 -->
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-codec</artifactId>
    <version>1.15</version>
</dependency>

<!-- Jackson JSON处理 -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>

<!-- HTTP客户端 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

## 数据库设计

### 微信用户表 (wechat_users)

```sql
CREATE TABLE wechat_users (
    id VARCHAR(50) PRIMARY KEY,
    openid VARCHAR(100) UNIQUE NOT NULL,
    unionid VARCHAR(100),
    session_key VARCHAR(100),
    nick_name VARCHAR(100),
    avatar_url VARCHAR(500),
    gender TINYINT,
    city VARCHAR(50),
    province VARCHAR(50),
    country VARCHAR(50),
    language VARCHAR(10),
    phone_number VARCHAR(20),
    pure_phone_number VARCHAR(20),
    country_code VARCHAR(10),
    internal_user_id VARCHAR(50),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login_time TIMESTAMP
);

-- 添加索引
CREATE INDEX idx_openid ON wechat_users(openid);
CREATE INDEX idx_unionid ON wechat_users(unionid);
CREATE INDEX idx_internal_user_id ON wechat_users(internal_user_id);
```

### 用户表更新

为现有的users表添加微信相关字段：

```sql
ALTER TABLE users ADD COLUMN wechat_openid VARCHAR(100);
ALTER TABLE users ADD COLUMN wechat_unionid VARCHAR(100);
ALTER TABLE users ADD INDEX idx_wechat_openid (wechat_openid);
```

## 安全建议

1. **Session管理**: sessionKey应该定期更新，建议存储在Redis中并设置过期时间。

2. **数据加密**: 敏感信息如手机号应该在数据库中加密存储。

3. **API安全**: 所有微信相关API应该验证openid和sessionKey的有效性。

4. **错误处理**: 完善的错误处理机制，避免敏感信息泄露。

5. **日志记录**: 记录关键操作日志，便于问题排查。

## 测试建议

1. **单元测试**: 为每个微信API编写单元测试。

2. **集成测试**: 测试完整的授权流程。

3. **边界测试**: 测试各种异常情况的处理。

4. **性能测试**: 确保授权流程在高并发下的性能。

## 常见问题

### Q1: 如何处理session_key过期？
A: 可以在API调用失败时重新调用wx.login()获取新的code，然后重新登录。

### Q2: 如何处理用户拒绝授权？
A: 提供友好的引导，说明授权的必要性，并提供重新授权的入口。

### Q3: 如何确保数据安全？
A: 使用HTTPS传输，对敏感数据进行加密存储，定期更新密钥。

### Q4: 如何处理微信API调用频率限制？
A: 实现请求频率限制，使用缓存减少不必要的API调用。