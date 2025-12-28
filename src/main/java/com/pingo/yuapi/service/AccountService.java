package com.pingo.yuapi.service;

import com.pingo.yuapi.entity.IncomeRecord;
import com.pingo.yuapi.entity.UserAccount;
import com.pingo.yuapi.entity.WithdrawRecord;

import java.math.BigDecimal;
import java.util.List;

public interface AccountService {

    /**
     * 获取用户账户信息
     */
    UserAccount getUserAccount(String userId);

    /**
     * 创建用户账户
     */
    UserAccount createUserAccount(String userId);

    /**
     * 添加收入记录（支付成功后调用）
     */
    IncomeRecord addIncome(String userId, String orderId, BigDecimal amount);

    /**
     * 更新账户可提现余额（定时任务调用，将超过1天的收入从冻结余额转到可提现余额）
     */
    void updateAvailableBalance();

    /**
     * 获取收入记录列表
     */
    List<IncomeRecord> getIncomeRecords(String userId, int page, int pageSize);

    /**
     * 申请提现
     */
    WithdrawRecord applyWithdraw(String userId, BigDecimal amount, String bankAccount, String bankName, String accountName);

    /**
     * 获取提现记录列表
     */
    List<WithdrawRecord> getWithdrawRecords(String userId, int page, int pageSize);

    /**
     * 处理提现（管理员审核）
     */
    boolean processWithdraw(String withdrawId, boolean approved, String remark);
}
