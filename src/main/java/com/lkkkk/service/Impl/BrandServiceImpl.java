package com.lkkkk.service.Impl;

import com.lkkkk.domain.Brand;
import com.lkkkk.mapper.BrandMapper;
import com.lkkkk.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @program: springboot
 * @description:
 * @author: LKKKK
 * @create: 2023-03-27 21:32
 **/

@Service
public class BrandServiceImpl implements BrandService {

    //注入dao
    @Autowired
    public BrandMapper brandMapper;


    @Override
    public Boolean save(Brand brand) {
        return brandMapper.insert(brand) > 0;
    }

    @Override
    public Boolean delete(Integer id) {
        return brandMapper.deleteById(id) > 0;
    }

    @Override
    public Boolean update(Brand brand) {
        return brandMapper.update(brand) > 0;
    }

    @Override
    public Brand selectById(Integer id) {
        Brand brand = brandMapper.selectById(id);
        return brand;
    }

    @Override
    public List<Brand> selectAll() {
        List<Brand> brands = brandMapper.selectAll();
        return brands;
    }

    @Override
    public List<Brand> selectPage(int start, int number) {
        return brandMapper.selectPage(start,number);
    }

    @Override
    public List<Brand> selectByCondition(Brand brand) {
        return brandMapper.selectByCondition(brand.getBrandName(),brand.getCompanyName());
    }
}
