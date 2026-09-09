package com.example.blog_backend.service.impl;

import com.example.blog_backend.entity.AboutMe;
import com.example.blog_backend.mapper.AboutMeMapper;
import com.example.blog_backend.service.IAboutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AboutServiceImpl implements IAboutService {

    private static final int ROW_ID = 1;

    @Autowired
    private AboutMeMapper aboutMeMapper;

    @Override
    public String getContent() {
        AboutMe row = aboutMeMapper.selectById(ROW_ID);
        return row == null || row.getContent() == null ? "" : row.getContent();
    }

    @Override
    public void saveContent(String content) {
        AboutMe row = aboutMeMapper.selectById(ROW_ID);
        String value = content == null ? "" : content;
        if (row == null) {
            row = new AboutMe();
            row.setId(ROW_ID);
            row.setContent(value);
            aboutMeMapper.insert(row);
        } else {
            row.setContent(value);
            aboutMeMapper.updateById(row);
        }
    }
}
