package com.lkkkk.controller;

import com.lkkkk.domain.Brand;
import com.lkkkk.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @program: springboot
 * @description:
 * @author: LKKKK
 * @create: 2023-03-28 09:05
 **/

//@RestController
@RequestMapping("/books")
public class BookController2 {

    @Autowired
    BrandService brandService;

    @GetMapping
    public List<Brand> selectAll(){
        System.out.println("selectAll测试");
        List<Brand> brands = brandService.selectAll();
        return brands;
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable int id){
        return brandService.delete(id);
    }
    @PostMapping
    public Boolean save(@RequestBody Brand brand){
        System.out.println("save测试====>"+brand);
        return brandService.save(brand);
    }

    @PutMapping
    public Boolean update(@RequestBody Brand brand){
        return brandService.update(brand);
    }

    @GetMapping("/{id}")
    public Brand selectById(@PathVariable int id){
        return brandService.selectById(id);
    }

    @GetMapping("/{start}/{number}")
    public List<Brand> selectPage(@PathVariable int start, @PathVariable int number){
        List<Brand> brands = brandService.selectPage(start, number);

        return brands;
    }


}
