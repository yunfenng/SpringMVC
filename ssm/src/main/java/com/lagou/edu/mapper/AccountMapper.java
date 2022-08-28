package com.lagou.edu.mapper;

import com.lagou.edu.pojo.Account;

import java.util.List;

/**
 * @author Jaa
 * @date 2022/1/20 22:32
 */
public interface AccountMapper {

    // 定义dao层接⼝⽅法--> 查询account表所有数据
    public List<Account> queryAccountList() throws Exception;
}
