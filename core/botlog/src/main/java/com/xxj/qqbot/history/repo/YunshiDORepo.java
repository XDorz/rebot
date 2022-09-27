package com.xxj.qqbot.history.repo;

import com.xxj.qqbot.history.entity.YunshiDO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface YunshiDORepo extends JpaRepository<YunshiDO, Long> {

    List<YunshiDO> findAllByExpireTimeAfter(Date nowTime);

    YunshiDO findAllByQqIdAndExpireTimeAfter(Long qqId,Date nowTime);

    List<YunshiDO> findAllByQqIdAndExpireTimeBefore(Long qqId,Date nowTime);
}
