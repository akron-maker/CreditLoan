package com.akron.CreditLoan.repayment.service;

import com.akron.CreditLoan.api.depository.model.RepaymentRequest;
import com.akron.CreditLoan.api.repayment.model.ProjectWithTendersDTO;
import com.akron.CreditLoan.repayment.entity.RepaymentDetail;
import com.akron.CreditLoan.repayment.entity.RepaymentPlan;

import java.util.List;

public interface RepaymentService {

    /**
     * 启动还款
     * @param projectWithTendersDTO
     * @return
     */
    String startRepayment(ProjectWithTendersDTO projectWithTendersDTO);

    /**
     * 查询所有到期的还款计划
     * @param  date 格式：yyyy-MM-dd
     * @return
     */
    List<RepaymentPlan> selectDueRepayment(String date);
    List<RepaymentPlan> selectDueRepayment(String date,int shardingCount,int shardingItem);

    /**
     * 根据还款计划生成还款明细并保存
     * @param repaymentPlan
     * @return
     */
    RepaymentDetail saveRepaymentDetail(RepaymentPlan repaymentPlan);

    /**
     * 执行用户还款
     */
    //void executeRepayment(String date);
    void executeRepayment(String date,int shardingCount,int shardingItem);

    /**
         * 还款预处理：冻结借款人应还金额
        * @param repaymentPlan
        * @param preRequestNo
        * @return
        */
    Boolean preRepayment(RepaymentPlan repaymentPlan, String preRequestNo);

    /**
     * 确认还款处理
     * @param repaymentPlan
     * @param repaymentRequest
     * @return
     */
    Boolean  confirmRepayment(RepaymentPlan repaymentPlan, RepaymentRequest
            repaymentRequest);


    /**
     * 远程调用确认还款接口
     * @param repaymentPlan
     * @param repaymentRequest
     */
    void invokeConfirmRepayment(RepaymentPlan repaymentPlan, RepaymentRequest
            repaymentRequest);

    /**
     * 查询还款人相关信息，并调用发送短信接口进行还款提醒
     */
    void sendRepaymentNotify(String date);
}