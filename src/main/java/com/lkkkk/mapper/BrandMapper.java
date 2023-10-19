package com.lkkkk.mapper;


import com.lkkkk.domain.Brand;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;


import java.util.List;

@Mapper//该注解是mybatis自身的注解，在和springboot整合时，扫描该注解就标志着这个接口要生成一个mapper代理类，交给ioc容器管理
public interface BrandMapper {



    //根据id查询
    @Select("select * from tb_brand where id = #{id}")
    @Results({
            @Result(column = "company_name",property = "companyName"),
            @Result(column = "brand_name",property = "brandName"),

    })
    public Brand selectById(Integer id);

    //查询所有
    @Select("select * from tb_brand")
    @Results({

            @Result(column = "company_name",property = "companyName"),
            @Result(column = "brand_name",property = "brandName")
    })
    public List<Brand> selectAll();


    //根据id删除
    @Delete("delete from tb_brand where id = #{id}")
    public int deleteById(Integer id);


    @Update("update tb_brand set brand_name = #{brandName},company_name = #{companyName},ordered = #{ordered},status = #{status},description=#{description} where id = #{id}")
    @Results({
            @Result(column = "company_name",property = "companyName"),
            @Result(column = "brand_name",property = "brandName")
    })
    public int update(Brand brand);

    //添加数据
    @Insert("insert into tb_brand values(null,#{brandName},#{companyName},#{ordered},#{description},#{status})")
    public int insert(Brand brand);

    //分页查询

//    @Select("select * from tb_brand limit #{start},#{number }")
    //使用子查询查询和覆盖索引对limit进行优化
    @Select("select * from tb_brand where id >= (select id from tb_brand limit #{start},1) limit #{number}")
    @Results({
            @Result(column = "company_name",property = "companyName"),
            @Result(column = "brand_name",property = "brandName")
    })

    public List<Brand> selectPage(int start,int number);

    //多条件查询

    public List<Brand> selectByCondition(@Param("brandName")String brandName,@Param("companyName")String companyName);

}
