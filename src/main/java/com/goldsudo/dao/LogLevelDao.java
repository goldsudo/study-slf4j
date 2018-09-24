package com.goldsudo.dao;

import com.goldsudo.domain.LogLevel;

public interface LogLevelDao {
    int deleteByPrimaryKey(Integer id);

    int insert(LogLevel record);

    int insertSelective(LogLevel record);

    LogLevel selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(LogLevel record);

    int updateByPrimaryKey(LogLevel record);
}