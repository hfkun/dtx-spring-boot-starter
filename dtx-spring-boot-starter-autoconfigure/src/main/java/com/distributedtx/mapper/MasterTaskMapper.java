package com.distributedtx.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.distributedtx.domain.MasterTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface MasterTaskMapper extends BaseMapper<MasterTask> {

    @Update({
            "<script>",
            "update master_task set status=1,process_time=now() where id in ",
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</script>"
    })
    int updateStatus(@Param("ids") String[] ids);
}
