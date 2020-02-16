package com.distributedtx.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.distributedtx.domain.ProviderTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ProviderTaskMapper extends BaseMapper<ProviderTask> {

    @Update({
            "<script>",
            "update provider_task set status=2 where id in ",
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</script>"
    })
    int updateStatus(@Param("ids") String[] ids);

    @Update("update provider_task set status=1,process_time=now() where id = #{id}")
    int updateDone(@Param("id") String id);

    @Update("update provider_task set try_times=try_times+1 where id = #{id}")
    int updateFail(@Param("id") String id);
}
