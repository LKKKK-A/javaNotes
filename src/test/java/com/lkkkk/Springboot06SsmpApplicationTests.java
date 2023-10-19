package com.lkkkk;

import com.lkkkk.domain.Brand;
import com.lkkkk.mapper.BrandMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.awt.print.Book;
import java.util.List;

@SpringBootTest
class Springboot06SsmpApplicationTests {

    @Autowired
    BrandMapper brandMapper;


    @Test
    void test1() {
        Brand brand = brandMapper.selectById(4);
        System.out.println("测试====="+brand);
    }

    @Test
    void tes2() {
        List<Brand> brandList = brandMapper.selectAll();
        for (Brand brand : brandList) {
            System.out.println(brand);
        }

    }

    @Test
    void test3() {
        brandMapper.deleteById(4);
    }

    @Test
    void test4() {
        Brand brand = new Brand();
        brand.setBrandName("北京信息科技大学");
        brand.setCompanyName("北京科技");
        brand.setDescription("建最好的大学");
        brand.setOrdered(1000);
        brand.setStatus(0);
        brandMapper.update(brand);
    }
    @Test
    void test5() {
        Brand brand = new Brand();
        brand.setBrandName("北京信息科技大学");
        brand.setCompanyName("北京科技");
        brand.setDescription("建最好的大学");
        brand.setOrdered(1000);
        brand.setStatus(0);
        brandMapper.insert(brand);
    }

    @Test
    public void test6(){
        List<Brand> brands = brandMapper.selectPage(2, 5);
        for (Brand brand : brands) {
            System.out.println(brand);
        }
    }

    @Test
    public void test7(){
        System.out.println("mmmmmmmmm");
        List<Brand> brands = brandMapper.selectByCondition("小米","小米");
        System.out.println("size===>"+brands.size());
        for (Brand brand : brands) {
            System.out.println("条件查询===>"+brand);
        }
    }



}