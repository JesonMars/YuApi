package com.pingo.yuapi.controller;

import com.pingo.yuapi.common.Result;
import com.pingo.yuapi.entity.IncomeRecord;
import com.pingo.yuapi.entity.UserAccount;
import com.pingo.yuapi.entity.WithdrawRecord;
import com.pingo.yuapi.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/account")
@CrossOrigin(origins = "*")
public class AccountController {

    @Autowired
    private AccountService accountService;

    /**
     * 获取用户账户信息
     */
    @GetMapping("/info")
    public Result<UserAccount> getAccountInfo() {
        try {
            String userId = getCurrentUserId();
            UserAccount account = accountService.getUserAccount(userId);
            return Result.success(account);
        } catch (Exception e) {
            return Result.error("获取账户信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取收入记录列表
     */
    @GetMapping("/income")
    public Result<List<IncomeRecord>> getIncomeRecords(@RequestParam(defaultValue = "1") int page,
                                                         @RequestParam(defaultValue = "10") int pageSize) {
        try {
            String userId = getCurrentUserId();
            List<IncomeRecord> records = accountService.getIncomeRecords(userId, page, pageSize);
            return Result.success(records);
        } catch (Exception e) {
            return Result.error("获取收入记录失败: " + e.getMessage());
        }
    }

    /**
     * 申请提现
     */
    @PostMapping("/withdraw/apply")
    public Result<WithdrawRecord> applyWithdraw(@RequestBody Map<String, Object> request) {
        try {
            String userId = getCurrentUserId();
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            String bankAccount = (String) request.get("bankAccount");
            String bankName = (String) request.get("bankName");
            String accountName = (String) request.get("accountName");

            WithdrawRecord record = accountService.applyWithdraw(userId, amount, bankAccount, bankName, accountName);

            if (record != null) {
                return Result.success(record);
            } else {
                return Result.error("提现申请失败，可能余额不足");
            }
        } catch (Exception e) {
            return Result.error("提现申请失败: " + e.getMessage());
        }
    }

    /**
     * 获取提现记录列表
     */
    @GetMapping("/withdraw/records")
    public Result<List<WithdrawRecord>> getWithdrawRecords(@RequestParam(defaultValue = "1") int page,
                                                            @RequestParam(defaultValue = "10") int pageSize) {
        try {
            String userId = getCurrentUserId();
            List<WithdrawRecord> records = accountService.getWithdrawRecords(userId, page, pageSize);
            return Result.success(records);
        } catch (Exception e) {
            return Result.error("获取提现记录失败: " + e.getMessage());
        }
    }

    /**
     * 处理提现审核（管理员）
     */
    @PostMapping("/withdraw/process")
    public Result<Boolean> processWithdraw(@RequestBody Map<String, Object> request) {
        try {
            String withdrawId = (String) request.get("withdrawId");
            Boolean approved = (Boolean) request.get("approved");
            String remark = (String) request.get("remark");

            boolean success = accountService.processWithdraw(withdrawId, approved, remark != null ? remark : "");
            return Result.success(success);
        } catch (Exception e) {
            return Result.error("处理提现失败: " + e.getMessage());
        }
    }

    /**
     * 手动触发更新可提现余额（测试用，生产环境应该用定时任务）
     */
    @PostMapping("/update-available-balance")
    public Result<String> updateAvailableBalance() {
        try {
            accountService.updateAvailableBalance();
            return Result.success("更新成功");
        } catch (Exception e) {
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前用户ID（实际项目中应该从JWT token或session中获取）
     */
    private String getCurrentUserId() {
        // 这里应该从认证信息中获取真实的用户ID
        // 为了演示，返回一个固定的用户ID
        return "user_001";
    }
}
