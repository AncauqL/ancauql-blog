<template>
  <section class="about-me">
    <div class="about-content">
      <p class="intro">
        你好，我是 <strong>李金诺</strong>。
      </p>

      <p class="identity">
        大连理工大学 2024 级本科生，来自计算机科学与技术学院。
      </p>

      <div class="info-block">
        <p>
          这个博客是我的 Java 课程设计项目，也是一个准备用来长期维护的个人博客雏形。
        </p>
        <p>
          当前项目围绕文章、分类和后台管理展开，主要使用
          <strong>Spring Boot</strong>、<strong>MyBatis-Plus</strong>、
          <strong>MySQL</strong>、<strong>Vue</strong> 和
          <strong>Element UI</strong> 完成前后端开发。
        </p>
        <p>
          我希望它不只是一次作业，而是一个可以继续记录学习、整理想法、沉淀代码的地方。
        </p>
      </div>

      <p class="slogan">少一分浮躁，多一分沉淀。</p>
    </div>
  </section>
</template>

<style scoped>
.about-me {
  min-height: calc(100vh - 120px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 24px;
  color: #1f1e33;
}

.about-content {
  max-width: 780px;
  width: 100%;
  text-align: center;
  font-family: "Noto Serif SC", "Songti SC", "SimSun", "Times New Roman", serif;
}

.intro {
  margin: 0;
  font-size: 34px;
  line-height: 1.45;
}

.identity {
  margin: 16px 0 0;
  font-size: 21px;
  line-height: 1.8;
  color: #303133;
}

.info-block {
  margin-top: 28px;
  font-size: 18px;
  line-height: 2;
  color: #444b52;
}

.info-block p {
  margin: 10px 0;
}

.slogan {
  margin: 30px 0 0;
  font-size: 24px;
  line-height: 1.7;
  color: #545c64;
  font-style: italic;
}

.intro strong,
.info-block strong {
  font-weight: 700;
}
</style>
