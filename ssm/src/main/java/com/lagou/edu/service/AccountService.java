package com.lagou.edu.service;

import com.lagou.edu.pojo.Account;

import java.util.List;

/**
 * @author Jaa
 * @date 2022/1/20 22:40
 */
public interface AccountService {
    public List<Account> queryAccountList() throws Exception;
}
