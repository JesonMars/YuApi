package com.pingo.yuapi.service.impl;

import com.pingo.yuapi.entity.IncomeRecord;
import com.pingo.yuapi.entity.UserAccount;
import com.pingo.yuapi.entity.WithdrawRecord;
import com.pingo.yuapi.service.AccountService;
import com.pingo.yuapi.utils.IdGeneratorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AccountServiceImpl implements AccountService {

    private static final Logger logger = LoggerFactory.getLogger(AccountServiceImpl.class);

    // 模拟存储
    private Map<String, UserAccount> accountStorage = new HashMap<>();
    private Map<String, IncomeRecord> incomeStorage = new HashMap<>();
    private Map<String, WithdrawRecord> withdrawStorage = new HashMap<>();

    @Override
    public UserAccount getUserAccount(String userId) {
        UserAccount account = accountStorage.get(userId);
        if (account == null) {
            // 如果账户不存在，自动创建
            account = createUserAccount(userId);
        }
        return account;
    }

    @Override
    public UserAccount createUserAccount(String userId) {
        UserAccount account = new UserAccount();
        account.setId("account_" + IdGeneratorUtils.generateShortId());
        account.setUserId(userId);
        account.setBalance(BigDecimal.ZERO);
        account.setAvailableBalance(BigDecimal.ZERO);
        account.setFrozenBalance(BigDecimal.ZERO);
        account.setTotalIncome(BigDecimal.ZERO);
        account.setTotalWithdraw(BigDecimal.ZERO);
        account.setCreateTime(LocalDateTime.now());
        account.setUpdateTime(LocalDateTime.now());

        accountStorage.put(userId, account);
        logger.info("创建用户账户: userId={}, accountId={}", userId, account.getId());

        return account;
    }

    @Override
    public IncomeRecord addIncome(String userId, String orderId, BigDecimal amount) {
        try {
            // 1. 创建收入记录
            IncomeRecord income = new IncomeRecord();
            income.setId("income_" + IdGeneratorUtils.generateShortId());
            income.setUserId(userId);
            income.setOrderId(orderId);
            income.setAmount(amount);
            income.setStatus("frozen"); // 初始状态为冻结
            income.setType("order");
            income.setIncomeTime(LocalDateTime.now());
            income.setAvailableTime(LocalDateTime.now().plusDays(1)); // T+1可提现
            income.setCreateTime(LocalDateTime.now());
            income.setUpdateTime(LocalDateTime.now());

            incomeStorage.put(income.getId(), income);

            // 2. 更新账户余额
            UserAccount account = getUserAccount(userId);
            account.setBalance(account.getBalance().add(amount));
            account.setFrozenBalance(account.getFrozenBalance().add(amount));
            account.setTotalIncome(account.getTotalIncome().add(amount));
            account.setUpdateTime(LocalDateTime.now());

            logger.info("添加收入记录成功: userId={}, orderId={}, amount={}", userId, orderId, amount);

            return income;

        } catch (Exception e) {
            logger.error("添加收入记录失败", e);
            return null;
        }
    }

    @Override
    public void updateAvailableBalance() {
        try {
            LocalDateTime now = LocalDateTime.now();

            // 查找所有状态为frozen且已经到可提现时间的收入记录
            List<IncomeRecord> availableIncomes = incomeStorage.values().stream()
                    .filter(income -> "frozen".equals(income.getStatus()))
                    .filter(income -> income.getAvailableTime().isBefore(now))
                    .collect(Collectors.toList());

            // 按用户分组更新
            Map<String, List<IncomeRecord>> userIncomes = availableIncomes.stream()
                    .collect(Collectors.groupingBy(IncomeRecord::getUserId));

            for (Map.Entry<String, List<IncomeRecord>> entry : userIncomes.entrySet()) {
                String userId = entry.getKey();
                List<IncomeRecord> incomes = entry.getValue();

                // 计算该用户可解冻的金额
                BigDecimal unfreezeAmount = incomes.stream()
                        .map(IncomeRecord::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                // 更新账户余额
                UserAccount account = getUserAccount(userId);
                account.setFrozenBalance(account.getFrozenBalance().subtract(unfreezeAmount));
                account.setAvailableBalance(account.getAvailableBalance().add(unfreezeAmount));
                account.setUpdateTime(LocalDateTime.now());

                // 更新收入记录状态
                for (IncomeRecord income : incomes) {
                    income.setStatus("available");
                    income.setUpdateTime(LocalDateTime.now());
                }

                logger.info("更新用户可提现余额: userId={}, unfreezeAmount={}", userId, unfreezeAmount);
            }

        } catch (Exception e) {
            logger.error("更新可提现余额失败", e);
        }
    }

    @Override
    public List<IncomeRecord> getIncomeRecords(String userId, int page, int pageSize) {
        List<IncomeRecord> userIncomes = incomeStorage.values().stream()
                .filter(income -> userId.equals(income.getUserId()))
                .sorted((i1, i2) -> i2.getCreateTime().compareTo(i1.getCreateTime()))
                .collect(Collectors.toList());

        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, userIncomes.size());

        if (start >= userIncomes.size()) {
            return new ArrayList<>();
        }

        return userIncomes.subList(start, end);
    }

    @Override
    public WithdrawRecord applyWithdraw(String userId, BigDecimal amount, String bankAccount, String bankName, String accountName) {
        try {
            // 1. 检查账户余额
            UserAccount account = getUserAccount(userId);
            if (account.getAvailableBalance().compareTo(amount) < 0) {
                logger.error("可提现余额不足: userId={}, available={}, withdraw={}",
                        userId, account.getAvailableBalance(), amount);
                return null;
            }

            // 2. 计算手续费（这里暂时不收手续费）
            BigDecimal fee = BigDecimal.ZERO;
            BigDecimal actualAmount = amount.subtract(fee);

            // 3. 创建提现记录
            WithdrawRecord withdraw = new WithdrawRecord();
            withdraw.setId("withdraw_" + IdGeneratorUtils.generateShortId());
            withdraw.setUserId(userId);
            withdraw.setAmount(amount);
            withdraw.setFee(fee);
            withdraw.setActualAmount(actualAmount);
            withdraw.setStatus("pending");
            withdraw.setBankAccount(bankAccount);
            withdraw.setBankName(bankName);
            withdraw.setAccountName(accountName);
            withdraw.setApplyTime(LocalDateTime.now());
            withdraw.setCreateTime(LocalDateTime.now());
            withdraw.setUpdateTime(LocalDateTime.now());

            withdrawStorage.put(withdraw.getId(), withdraw);

            // 4. 扣除可提现余额
            account.setAvailableBalance(account.getAvailableBalance().subtract(amount));
            account.setUpdateTime(LocalDateTime.now());

            logger.info("提现申请成功: userId={}, withdrawId={}, amount={}", userId, withdraw.getId(), amount);

            return withdraw;

        } catch (Exception e) {
            logger.error("提现申请失败", e);
            return null;
        }
    }

    @Override
    public List<WithdrawRecord> getWithdrawRecords(String userId, int page, int pageSize) {
        List<WithdrawRecord> userWithdraws = withdrawStorage.values().stream()
                .filter(withdraw -> userId.equals(withdraw.getUserId()))
                .sorted((w1, w2) -> w2.getCreateTime().compareTo(w1.getCreateTime()))
                .collect(Collectors.toList());

        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, userWithdraws.size());

        if (start >= userWithdraws.size()) {
            return new ArrayList<>();
        }

        return userWithdraws.subList(start, end);
    }

    @Override
    public boolean processWithdraw(String withdrawId, boolean approved, String remark) {
        try {
            WithdrawRecord withdraw = withdrawStorage.get(withdrawId);
            if (withdraw == null) {
                logger.error("提现记录不存在: withdrawId={}", withdrawId);
                return false;
            }

            if (!"pending".equals(withdraw.getStatus())) {
                logger.error("提现记录状态异常: withdrawId={}, status={}", withdrawId, withdraw.getStatus());
                return false;
            }

            UserAccount account = getUserAccount(withdraw.getUserId());

            if (approved) {
                // 审核通过
                withdraw.setStatus("success");
                withdraw.setCompleteTime(LocalDateTime.now());
                account.setTotalWithdraw(account.getTotalWithdraw().add(withdraw.getAmount()));
                logger.info("提现审核通过: withdrawId={}, amount={}", withdrawId, withdraw.getAmount());
            } else {
                // 审核拒绝，退回余额
                withdraw.setStatus("failed");
                withdraw.setRemark(remark);
                account.setAvailableBalance(account.getAvailableBalance().add(withdraw.getAmount()));
                logger.info("提现审核拒绝: withdrawId={}, amount={}, reason={}", withdrawId, withdraw.getAmount(), remark);
            }

            withdraw.setUpdateTime(LocalDateTime.now());
            account.setUpdateTime(LocalDateTime.now());

            return true;

        } catch (Exception e) {
            logger.error("处理提现失败", e);
            return false;
        }
    }
}
