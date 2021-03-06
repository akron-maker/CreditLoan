package com.akron.CreditLoan.account.service;


import com.akron.CreditLoan.account.common.AccountErrorCode;
import com.akron.CreditLoan.account.entity.Account;
import com.akron.CreditLoan.account.mapper.AccountMapper;
import com.akron.CreditLoan.api.account.model.AccountDTO;
import com.akron.CreditLoan.api.account.model.AccountLoginDTO;
import com.akron.CreditLoan.api.account.model.AccountRegisterDTO;
import com.akron.CreditLoan.common.domain.BusinessException;
import com.akron.CreditLoan.common.domain.RestResponse;
import com.akron.CreditLoan.common.util.PasswordUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hmily.annotation.Hmily;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService {

    @Autowired
    private SmsService smsService;

    @Value("${sms.enable}")
    private Boolean smsEnable;

    @Override
    public RestResponse getSMSCode(String mobile) {
        return smsService.getSMSCode(mobile);
    }

    @Override
    public Integer checkMobile(String mobile, String key, String code) {
        smsService.verifySmsCode(key,code);
        QueryWrapper<Account> wrapper=new QueryWrapper<>();
        //wrapper.eq("mobile",mobile);
        wrapper.lambda().eq(Account::getMobile,mobile);
        int count=count(wrapper);
        return count>0?1:0;
    }

    @Override
    @Hmily(confirmMethod = "confirmRegister",cancelMethod = "cancelRegister")
    public AccountDTO register(AccountRegisterDTO accountRegisterDTO) {
        Account account=new Account();
        account.setUsername(accountRegisterDTO.getUsername());
        account.setMobile(accountRegisterDTO.getMobile());
        account.setPassword(PasswordUtil.generate(accountRegisterDTO.getPassword()));
        if(smsEnable){
            account.setPassword(PasswordUtil.generate(accountRegisterDTO.getMobile()));
        }
        account.setDomain("c");
        if(accountRegisterDTO.getMobile().equals("110")){  //?????????
            throw new RuntimeException("???????????????");
        }
        save(account);
        return convertAccountEntityToDTO(account);
    }

    public void confirmRegister(AccountRegisterDTO registerDTO) {
        log.info("execute confirmRegister");
    }

    public void cancelRegister(AccountRegisterDTO registerDTO) {
        log.info("execute cancelRegister");
        //????????????
        remove(Wrappers.<Account>lambdaQuery().eq(Account::getUsername,
                registerDTO.getUsername()));
    }

    @Override
    public AccountDTO login(AccountLoginDTO accountLoginDTO) {
        //1.??????????????????????????????????????????
        //2.??????????????????????????????????????????????????????
        Account account=null;
        if(accountLoginDTO.getDomain().equalsIgnoreCase("c")){
            //?????????c????????????????????????????????????
            account=getAccountByMobile(accountLoginDTO.getMobile());
        }else{
            //?????????b?????????????????????????????????
            account=getAccountByUsername(accountLoginDTO.getUsername());
        }
        if(account==null){
            throw  new BusinessException(AccountErrorCode.E_130104);
        }

        AccountDTO accountDTO=convertAccountEntityToDTO(account);
        if(smsEnable){ //?????????true,??????????????????????????????????????????????????????
            return accountDTO;
        }

        if(PasswordUtil.verify(accountLoginDTO.getPassword(),account.getPassword())){
            return accountDTO;
        }

        throw  new BusinessException(AccountErrorCode.E_130105);
    }

    private Account getAccountByMobile(String mobile){
        return getOne(new QueryWrapper<Account>().lambda().eq(Account::getMobile,mobile));
    }

    private Account getAccountByUsername(String username){
        return getOne(new QueryWrapper<Account>().lambda().eq(Account::getUsername,username));
    }

    /**
     * entity??????dto
        * @param entity
        * @return
     */
    private AccountDTO convertAccountEntityToDTO(Account entity) {
        if (entity == null) {
            return null;
        }
        AccountDTO dto = new AccountDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}
