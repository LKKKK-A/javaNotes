package com.lkkkk.service;

import com.lkkkk.domain.Brand;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * @program: springboot
 * @description:
 * @author:
 * @create: 2023-03-27 21:53
 **/

@SpringBootTest
class BrandServiceTest {

    @Autowired
    BrandService brandService;

    @Test
    void test1() {
        Brand brand = brandService.selectById(4);
        System.out.println("测试=====" + brand);
    }

    @Test
    void tes2() {
        List<Brand> brandList = brandService.selectAll();
        for (Brand brand : brandList) {
            System.out.println(brand);
        }

    }

    @Test
    void test3() {
        brandService.delete(4);
    }

    @Test
    void test4() {
        Brand brand = new Brand();
        brand.setBrandName("北京信息科技大学");
        brand.setCompanyName("北京科技");
        brand.setDescription("建最好的大学");
        brand.setOrdered(1000);
        brand.setStatus(0);
        brandService.update(brand);
    }

    @Test
    void test5() {
        Brand brand = new Brand();
        brand.setBrandName("北京信息科技大学");
        brand.setCompanyName("北京科技");
        brand.setDescription("建最好的大学");
        brand.setOrdered(1000);
        brand.setStatus(0);
        brandService.update(brand);
    }

    @Test
    void test6() {
        List<Brand> brands = brandService.selectPage(2, 5);
        for (Brand brand : brands) {
            System.out.println(brand);
        }
    }

    @Test
    void test7() {
        Brand brand = new Brand();
        brand.setBrandName("小米");
        brand.setCompanyName("小米");
        brand.setDescription("小米");
        brand.setOrdered(1000);
        brand.setStatus(0);
        System.out.println("mmmmmmmmm");
        List<Brand> brands = brandService.selectByCondition(brand);
        System.out.println("size===>" + brands.size());
        for (Brand b : brands) {
            System.out.println("条件查询===>" + b);
        }
    }
}

